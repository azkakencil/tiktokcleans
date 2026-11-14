package com.example.data.local

import androidx.room.withTransaction
import com.example.data.local.db.ActivityItemEntity
import com.example.data.local.db.CleaningSessionEntity
import com.example.data.local.db.CleaningSessionItemEntity
import com.example.data.local.db.TikTokCleanerDatabase
import com.example.domain.model.ActivityCategory
import com.example.domain.model.CleaningProcessStatus
import com.example.domain.model.CleaningSessionItemDetail
import com.example.domain.model.CleaningSessionSummary
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.VideoItemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Room-backed local cache helper designed for large TikTok activity lists (Likes, Favorites, Reposts).
 *
 * Performance characteristics:
 * - Uses chunked transactions (`UPSERT_BATCH_SIZE = 250`) to prevent SQLite parameter overflow and
 *   keep UI frame times responsive when importing thousands of exported activity records.
 * - Preserves already-resolved oEmbed thumbnails/descriptions across re-imports so previously cached
 *   videos are not unnecessarily re-fetched over the network.
 * - Uses SQLite-indexed queries and paged reads (`getActivityPage`) for scalable list access.
 */
class DataStorage(private val database: TikTokCleanerDatabase) {

    private val activityDao = database.activityItemDao()
    private val historyDao = database.cleaningHistoryDao()

    companion object {
        const val UPSERT_BATCH_SIZE = 250
        const val SQL_IN_CHUNK_SIZE = 400
        const val DEFAULT_CACHE_TTL_MS = 24L * 60 * 60 * 1000 // 24 hours
    }

    fun observeAllActivities(): Flow<List<TikTokActivityItem>> =
        activityDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeActivitiesByCategory(category: ActivityCategory): Flow<List<TikTokActivityItem>> =
        if (category == ActivityCategory.ALL) {
            observeAllActivities()
        } else {
            activityDao.observeByCategory(category.name).map { entities -> entities.map { it.toDomain() } }
        }

    fun observeCategoryCounts(): Flow<Map<ActivityCategory, Int>> =
        activityDao.observeCategoryCounts().map { tuples ->
            tuples.associate { tuple ->
                val category = runCatching { ActivityCategory.valueOf(tuple.category) }
                    .getOrDefault(ActivityCategory.LIKES)
                category to tuple.count
            }
        }

    fun observeCleaningSessions(): Flow<List<CleaningSessionSummary>> =
        historyDao.observeSessions().map { list -> list.map { it.toDomain() } }

    fun observeTotalCleanedCount(): Flow<Int> = historyDao.observeTotalCleanedCount()

    fun observeSessionById(sessionId: Long): Flow<CleaningSessionSummary?> =
        historyDao.observeSessionById(sessionId).map { it?.toDomain() }

    fun observeSessionItems(sessionId: Long): Flow<List<CleaningSessionItemDetail>> =
        historyDao.observeItemsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    /**
     * Caches a potentially large list of TikTok activity items inside a single Room transaction
     * in chunked batches, preserving already-resolved oEmbed metadata (thumbnails, descriptions,
     * selection state, or cleaned status) if the record already exists in the local cache.
     */
    suspend fun cacheActivities(items: List<TikTokActivityItem>) = withContext(Dispatchers.IO) {
        if (items.isEmpty()) return@withContext

        database.withTransaction {
            val existingById = mutableMapOf<String, ActivityItemEntity>()
            items.map { it.id }.distinct().chunked(SQL_IN_CHUNK_SIZE).forEach { idChunk ->
                activityDao.getByIds(idChunk).forEach { existing ->
                    existingById[existing.id] = existing
                }
            }

            val mergedEntities = items.map { incoming ->
                val existing = existingById[incoming.id]
                if (existing != null && existing.thumbnailUrl != null && incoming.thumbnailUrl == null) {
                    // Preserve cached oEmbed metadata & cleaned/selection state on re-import
                    incoming.copy(
                        thumbnailUrl = existing.thumbnailUrl,
                        creatorUsername = existing.creatorUsername.ifBlank { incoming.creatorUsername },
                        description = existing.description.ifBlank { incoming.description },
                        status = runCatching { VideoItemStatus.valueOf(existing.status) }
                            .getOrDefault(incoming.status),
                        isSelected = existing.isSelected,
                        errorReason = existing.errorReason
                    ).toEntity(cachedAtMs = existing.cachedAtMs)
                } else {
                    incoming.toEntity(cachedAtMs = System.currentTimeMillis())
                }
            }

            mergedEntities.chunked(UPSERT_BATCH_SIZE).forEach { batch ->
                activityDao.upsertAll(batch)
            }
        }
    }

    suspend fun updateCachedItem(item: TikTokActivityItem) = withContext(Dispatchers.IO) {
        activityDao.upsert(item.toEntity(cachedAtMs = System.currentTimeMillis()))
    }

    suspend fun getItemsNeedingMetadataResolution(
        category: ActivityCategory,
        forceRefresh: Boolean = false,
        cacheTtlMs: Long = DEFAULT_CACHE_TTL_MS
    ): List<TikTokActivityItem> = withContext(Dispatchers.IO) {
        val staleBeforeMs = System.currentTimeMillis() - cacheTtlMs
        activityDao.getItemsNeedingMetadataResolution(
            category = category.name,
            forceRefresh = forceRefresh,
            staleBeforeMs = staleBeforeMs
        ).map { it.toDomain() }
    }

    suspend fun getActivityPage(
        category: ActivityCategory,
        searchQuery: String = "",
        sortNewest: Boolean = true,
        limit: Int = 60,
        offset: Int = 0
    ): List<TikTokActivityItem> = withContext(Dispatchers.IO) {
        activityDao.queryPage(
            category = category.name,
            query = searchQuery.trim(),
            sortNewest = sortNewest,
            limit = limit,
            offset = offset
        ).map { it.toDomain() }
    }

    suspend fun getCachedItemCount(): Int = withContext(Dispatchers.IO) {
        activityDao.countAll()
    }

    suspend fun setItemSelected(id: String, selected: Boolean) = withContext(Dispatchers.IO) {
        activityDao.setSelected(id, selected)
    }

    suspend fun setBatchSelected(ids: List<String>, selected: Boolean) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        database.withTransaction {
            ids.chunked(SQL_IN_CHUNK_SIZE).forEach { chunk ->
                activityDao.setSelectedBatch(chunk, selected)
            }
        }
    }

    suspend fun setAllSelected(category: ActivityCategory, selected: Boolean) = withContext(Dispatchers.IO) {
        if (category == ActivityCategory.ALL) {
            activityDao.setAllSelected(selected)
        } else {
            activityDao.setSelectedByCategory(category.name, selected)
        }
    }

    suspend fun markItemVerifiedCleaned(id: String) = withContext(Dispatchers.IO) {
        activityDao.updateStatusAndUnselect(
            id = id,
            status = VideoItemStatus.VERIFIED_CLEANED.name
        )
    }

    suspend fun markItemRequiresManualAction(id: String, reason: String?) = withContext(Dispatchers.IO) {
        activityDao.updateStatus(
            id = id,
            status = VideoItemStatus.REQUIRES_MANUAL_ACTION.name,
            reason = reason
        )
    }

    suspend fun recordCleaningSession(
        category: ActivityCategory,
        totalAnalyzed: Int,
        items: List<TikTokActivityItem>,
        status: CleaningProcessStatus
    ): Long = withContext(Dispatchers.IO) {
        val succeeded = items.count { it.status == VideoItemStatus.VERIFIED_CLEANED }
        val manual = items.count { it.status == VideoItemStatus.REQUIRES_MANUAL_ACTION }
        val failed = items.count {
            it.status == VideoItemStatus.FAILED || it.status == VideoItemStatus.THUMBNAIL_UNAVAILABLE
        }
        val session = CleaningSessionEntity(
            createdAtMs = System.currentTimeMillis(),
            category = category.name,
            totalAnalyzed = totalAnalyzed,
            totalSelected = items.size,
            totalSucceeded = succeeded,
            totalManualAction = manual,
            totalFailed = failed,
            status = status.name
        )
        val itemEntities = items.map { item ->
            CleaningSessionItemEntity(
                sessionId = 0L,
                videoUrl = item.videoUrl,
                category = item.category.name,
                creatorUsername = item.creatorUsername,
                description = item.description,
                activityDateLabel = item.activityDateLabel,
                thumbnailUrl = item.thumbnailUrl,
                finalStatus = item.status.name,
                note = item.errorReason
            )
        }
        historyDao.recordSessionWithItems(session, itemEntities)
    }

    suspend fun clearAllCachedData() = withContext(Dispatchers.IO) {
        database.withTransaction {
            activityDao.clearAll()
            historyDao.clearAll()
        }
    }
}

private fun ActivityItemEntity.toDomain(): TikTokActivityItem = TikTokActivityItem(
    id = id,
    videoUrl = videoUrl,
    category = runCatching { ActivityCategory.valueOf(category) }.getOrDefault(ActivityCategory.LIKES),
    creatorUsername = creatorUsername,
    description = description,
    activityDateLabel = activityDateLabel,
    activityTimestampMs = activityTimestampMs,
    thumbnailUrl = thumbnailUrl,
    status = runCatching { VideoItemStatus.valueOf(status) }.getOrDefault(VideoItemStatus.PENDING_ANALYSIS),
    isSelected = isSelected,
    sourceLabel = sourceLabel,
    errorReason = errorReason
)

private fun TikTokActivityItem.toEntity(cachedAtMs: Long): ActivityItemEntity = ActivityItemEntity(
    id = id,
    videoUrl = videoUrl,
    category = category.name,
    creatorUsername = creatorUsername,
    description = description,
    activityDateLabel = activityDateLabel,
    activityTimestampMs = activityTimestampMs,
    thumbnailUrl = thumbnailUrl,
    status = status.name,
    isSelected = isSelected,
    sourceLabel = sourceLabel,
    errorReason = errorReason,
    cachedAtMs = cachedAtMs
)

private fun CleaningSessionEntity.toDomain(): CleaningSessionSummary = CleaningSessionSummary(
    id = id,
    createdAtMs = createdAtMs,
    category = runCatching { ActivityCategory.valueOf(category) }.getOrDefault(ActivityCategory.ALL),
    totalAnalyzed = totalAnalyzed,
    totalSelected = totalSelected,
    totalSucceeded = totalSucceeded,
    totalManualAction = totalManualAction,
    totalFailed = totalFailed,
    status = runCatching { CleaningProcessStatus.valueOf(status) }.getOrDefault(CleaningProcessStatus.REQUIRES_MANUAL_ACTION)
)

private fun CleaningSessionItemEntity.toDomain(): CleaningSessionItemDetail = CleaningSessionItemDetail(
    id = id,
    sessionId = sessionId,
    videoUrl = videoUrl,
    category = runCatching { ActivityCategory.valueOf(category) }.getOrDefault(ActivityCategory.LIKES),
    creatorUsername = creatorUsername,
    description = description,
    activityDateLabel = activityDateLabel,
    thumbnailUrl = thumbnailUrl,
    finalStatus = runCatching { VideoItemStatus.valueOf(finalStatus) }.getOrDefault(VideoItemStatus.REQUIRES_MANUAL_ACTION),
    note = note
)
