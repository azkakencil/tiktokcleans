package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class CategoryCountTuple(
    val category: String,
    val count: Int
)

@Dao
interface ActivityItemDao {
    @Query("SELECT * FROM activity_items ORDER BY activityTimestampMs DESC")
    fun observeAll(): Flow<List<ActivityItemEntity>>

    @Query("SELECT * FROM activity_items WHERE category = :category ORDER BY activityTimestampMs DESC")
    fun observeByCategory(category: String): Flow<List<ActivityItemEntity>>

    @Query("SELECT category, COUNT(*) AS count FROM activity_items GROUP BY category")
    fun observeCategoryCounts(): Flow<List<CategoryCountTuple>>

    @Query(
        """
        SELECT * FROM activity_items
        WHERE (:category = 'ALL' OR category = :category)
          AND (
            :query = ''
            OR creatorUsername LIKE '%' || :query || '%'
            OR description LIKE '%' || :query || '%'
            OR videoUrl LIKE '%' || :query || '%'
          )
        ORDER BY
          CASE WHEN :sortNewest = 1 THEN activityTimestampMs END DESC,
          CASE WHEN :sortNewest = 0 THEN activityTimestampMs END ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryPage(
        category: String,
        query: String,
        sortNewest: Boolean,
        limit: Int,
        offset: Int
    ): List<ActivityItemEntity>

    @Query(
        """
        SELECT * FROM activity_items
        WHERE (:category = 'ALL' OR category = :category)
          AND status != 'VERIFIED_CLEANED'
          AND (:forceRefresh = 1 OR status = 'PENDING_ANALYSIS' OR thumbnailUrl IS NULL OR cachedAtMs < :staleBeforeMs)
        ORDER BY activityTimestampMs DESC
        """
    )
    suspend fun getItemsNeedingMetadataResolution(
        category: String,
        forceRefresh: Boolean,
        staleBeforeMs: Long
    ): List<ActivityItemEntity>

    @Query("SELECT * FROM activity_items WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ActivityItemEntity>

    @Query("SELECT * FROM activity_items")
    suspend fun getAllSnapshot(): List<ActivityItemEntity>

    @Query("SELECT COUNT(*) FROM activity_items")
    suspend fun countAll(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ActivityItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ActivityItemEntity)

    @Query("UPDATE activity_items SET isSelected = :selected WHERE id = :id")
    suspend fun setSelected(id: String, selected: Boolean)

    @Query("UPDATE activity_items SET isSelected = :selected WHERE id IN (:ids)")
    suspend fun setSelectedBatch(ids: List<String>, selected: Boolean)

    @Query("UPDATE activity_items SET isSelected = :selected WHERE category = :category")
    suspend fun setSelectedByCategory(category: String, selected: Boolean)

    @Query("UPDATE activity_items SET isSelected = :selected")
    suspend fun setAllSelected(selected: Boolean)

    @Query("UPDATE activity_items SET status = :status, isSelected = 0, cachedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun updateStatusAndUnselect(id: String, status: String, updatedAtMs: Long = System.currentTimeMillis())

    @Query("UPDATE activity_items SET status = :status, errorReason = :reason, cachedAtMs = :updatedAtMs WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, reason: String?, updatedAtMs: Long = System.currentTimeMillis())

    @Query("DELETE FROM activity_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM activity_items")
    suspend fun clearAll()
}

@Dao
interface CleaningHistoryDao {
    @Query("SELECT * FROM cleaning_sessions ORDER BY createdAtMs DESC")
    fun observeSessions(): Flow<List<CleaningSessionEntity>>

    @Query("SELECT COALESCE(SUM(totalSucceeded), 0) FROM cleaning_sessions")
    fun observeTotalCleanedCount(): Flow<Int>

    @Query("SELECT * FROM cleaning_sessions WHERE id = :sessionId LIMIT 1")
    fun observeSessionById(sessionId: Long): Flow<CleaningSessionEntity?>

    @Query("SELECT * FROM cleaning_session_items WHERE sessionId = :sessionId ORDER BY id ASC")
    fun observeItemsForSession(sessionId: Long): Flow<List<CleaningSessionItemEntity>>

    @Insert
    suspend fun insertSession(session: CleaningSessionEntity): Long

    @Insert
    suspend fun insertSessionItems(items: List<CleaningSessionItemEntity>)

    @Transaction
    suspend fun recordSessionWithItems(
        session: CleaningSessionEntity,
        items: List<CleaningSessionItemEntity>
    ): Long {
        val sessionId = insertSession(session)
        insertSessionItems(items.map { it.copy(sessionId = sessionId) })
        return sessionId
    }

    @Query("DELETE FROM cleaning_sessions")
    suspend fun clearAll()
}
