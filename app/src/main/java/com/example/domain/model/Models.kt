package com.example.domain.model

import kotlinx.serialization.Serializable

enum class ActivityCategory {
    LIKES,
    FAVORITES,
    REPOSTS,
    ALL
}

enum class VideoItemStatus {
    PENDING_ANALYSIS,
    LOADED,
    THUMBNAIL_UNAVAILABLE,
    REQUIRES_MANUAL_ACTION,
    VERIFIED_CLEANED,
    FAILED
}

enum class SortOrder {
    NEWEST,
    OLDEST
}

enum class DateFilter {
    ALL,
    LAST_7_DAYS,
    LAST_30_DAYS,
    OLDER
}

enum class AppLanguage {
    EN,
    ID
}

enum class CleaningProcessStatus {
    COMPLETED,
    REQUIRES_MANUAL_ACTION,
    CANCELLED,
    PARTIAL
}

@Serializable
data class TikTokUserProfile(
    val openId: String = "",
    val unionId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val bioDescription: String = "",
    val profileDeepLink: String = "",
    val isVerified: Boolean = false,
    val followerCount: Long = 0L,
    val followingCount: Long = 0L,
    val likesCount: Long = 0L,
    val videoCount: Long = 0L,
    val connectedAtEpochMs: Long = 0L,
    val isDataExportMode: Boolean = false
)

data class TikTokActivityItem(
    val id: String,
    val videoUrl: String,
    val category: ActivityCategory,
    val creatorUsername: String,
    val description: String,
    val activityDateLabel: String,
    val activityTimestampMs: Long,
    val thumbnailUrl: String?,
    val status: VideoItemStatus,
    val isSelected: Boolean = false,
    val sourceLabel: String = "",
    val errorReason: String? = null
)

data class AnalysisSummary(
    val category: ActivityCategory,
    val totalFound: Int,
    val totalSelected: Int,
    val readyToProcess: Int,
    val failedToLoad: Int,
    val requiresManualAction: Int,
    val likesSelected: Int,
    val favoritesSelected: Int,
    val repostsSelected: Int
)

data class CleaningSessionSummary(
    val id: Long,
    val createdAtMs: Long,
    val category: ActivityCategory,
    val totalAnalyzed: Int,
    val totalSelected: Int,
    val totalSucceeded: Int,
    val totalManualAction: Int,
    val totalFailed: Int,
    val status: CleaningProcessStatus
)

data class CleaningSessionItemDetail(
    val id: Long,
    val sessionId: Long,
    val videoUrl: String,
    val category: ActivityCategory,
    val creatorUsername: String,
    val description: String,
    val activityDateLabel: String,
    val thumbnailUrl: String?,
    val finalStatus: VideoItemStatus,
    val note: String?
)

data class OAuthResult(
    val success: Boolean,
    val profile: TikTokUserProfile? = null,
    val errorMessage: String? = null
)

data class ApiCapabilityNotice(
    val canReadLikesViaApi: Boolean = false,
    val canReadFavoritesViaApi: Boolean = false,
    val canReadRepostsViaApi: Boolean = false,
    val canMutateUnlikeViaApi: Boolean = false,
    val canMutateRemoveFavoriteViaApi: Boolean = false,
    val canMutateUndoRepostViaApi: Boolean = false,
    val canReadOwnVideosViaApi: Boolean = true,
    val canReadProfileViaApi: Boolean = true
)
