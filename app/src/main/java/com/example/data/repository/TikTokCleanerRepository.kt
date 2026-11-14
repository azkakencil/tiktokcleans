package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.DataStorage
import com.example.data.local.prefs.SecureTokenStorage
import com.example.data.remote.OEmbedMetadata
import com.example.data.remote.TikTokAuthService
import com.example.data.remote.TikTokDataExportParser
import com.example.data.remote.TikTokOEmbedService
import com.example.data.remote.TikTokOwnVideo
import com.example.domain.model.ActivityCategory
import com.example.domain.model.ApiCapabilityNotice
import com.example.domain.model.AppLanguage
import com.example.domain.model.CleaningProcessStatus
import com.example.domain.model.CleaningSessionItemDetail
import com.example.domain.model.CleaningSessionSummary
import com.example.domain.model.OAuthResult
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.TikTokUserProfile
import com.example.domain.model.VideoItemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TikTokCleanerRepository(
    private val context: Context,
    private val dataStorage: DataStorage,
    private val tokenStorage: SecureTokenStorage,
    private val authService: TikTokAuthService,
    private val oEmbedService: TikTokOEmbedService,
    private val exportParser: TikTokDataExportParser
) {
    val apiCapabilities = ApiCapabilityNotice()

    val isDarkMode: Flow<Boolean> = tokenStorage.isDarkModeFlow
    val language: Flow<AppLanguage> = tokenStorage.languageFlow
    val hideHistoryThumbs: Flow<Boolean> = tokenStorage.hideHistoryThumbsFlow
    val userProfile: Flow<TikTokUserProfile?> = tokenStorage.userProfileFlow

    val allActivityItems: Flow<List<TikTokActivityItem>> = dataStorage.observeAllActivities()
    val categoryCounts: Flow<Map<ActivityCategory, Int>> = dataStorage.observeCategoryCounts()
    val cleaningSessions: Flow<List<CleaningSessionSummary>> = dataStorage.observeCleaningSessions()
    val totalCleanedCount: Flow<Int> = dataStorage.observeTotalCleanedCount()

    fun observeSessionById(sessionId: Long): Flow<CleaningSessionSummary?> =
        dataStorage.observeSessionById(sessionId)

    fun observeSessionItems(sessionId: Long): Flow<List<CleaningSessionItemDetail>> =
        dataStorage.observeSessionItems(sessionId)

    fun isTikTokClientConfigured(): Boolean = authService.isClientKeyConfigured()

    suspend fun setDarkMode(enabled: Boolean) = tokenStorage.setDarkMode(enabled)
    suspend fun setLanguage(language: AppLanguage) = tokenStorage.setLanguage(language)
    suspend fun setHideHistoryThumbnails(hide: Boolean) = tokenStorage.setHideHistoryThumbnails(hide)

    suspend fun prepareTikTokOAuthUri(): Uri {
        val (verifier, challenge) = authService.createPkcePair()
        val state = authService.generateState()
        tokenStorage.saveOAuthPendingParams(state, verifier)
        return authService.buildAuthorizationUri(state, challenge)
    }

    suspend fun handleOAuthCallback(callbackUri: Uri): OAuthResult {
        val errorParam = callbackUri.getQueryParameter("error")
        if (!errorParam.isNullOrBlank()) {
            val desc = callbackUri.getQueryParameter("error_description") ?: errorParam
            return OAuthResult(success = false, errorMessage = desc)
        }
        val code = callbackUri.getQueryParameter("code")
        val state = callbackUri.getQueryParameter("state")
        val (expectedState, verifier) = tokenStorage.getAndClearOAuthPendingParams()

        if (code.isNullOrBlank() || verifier.isNullOrBlank()) {
            return OAuthResult(success = false, errorMessage = "Missing OAuth authorization code")
        }
        if (expectedState != null && state != expectedState) {
            return OAuthResult(success = false, errorMessage = "OAuth state mismatch")
        }

        val tokenResult = authService.exchangeCodeForTokens(code, verifier)
        val (accessToken, refreshToken, expiresIn) = tokenResult.getOrElse { err ->
            return OAuthResult(success = false, errorMessage = err.localizedMessage ?: "Token exchange failed")
        }

        tokenStorage.saveTokens(accessToken, refreshToken, expiresIn)
        val userResult = authService.fetchUserInfo(accessToken)
        if (userResult.success && userResult.profile != null) {
            tokenStorage.saveProfile(userResult.profile)
        }
        return userResult
    }

    suspend fun refreshAuthenticatedProfileAndOwnVideos(): Pair<OAuthResult?, List<TikTokOwnVideo>> {
        val token = tokenStorage.getAccessToken() ?: return null to emptyList()
        val profileResult = authService.fetchUserInfo(token)
        if (profileResult.success && profileResult.profile != null) {
            tokenStorage.saveProfile(profileResult.profile)
        }
        val videos = authService.fetchOwnVideos(token).getOrDefault(emptyList())
        return profileResult to videos
    }

    suspend fun enterOfficialDataInspectorMode() {
        tokenStorage.saveProfile(
            TikTokUserProfile(
                username = "Data Inspector",
                displayName = "Official Data Inspector",
                connectedAtEpochMs = System.currentTimeMillis(),
                isDataExportMode = true
            )
        )
    }

    suspend fun importDataExportFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: throw IllegalStateException("Unable to read selected file")

            val parsed = exportParser.parseOfficialExportJson(raw).getOrThrow()
            if (parsed.isNotEmpty()) {
                dataStorage.cacheActivities(parsed)
            }
            parsed.size
        }
    }

    suspend fun addLinksForCategory(rawLinks: String, category: ActivityCategory): Int {
        val items = exportParser.parsePastedLinks(rawLinks, category)
        if (items.isNotEmpty()) {
            dataStorage.cacheActivities(items)
        }
        return items.size
    }

    suspend fun analyzeCategoryThumbnails(
        category: ActivityCategory,
        forceRefresh: Boolean = false,
        onItemResolved: suspend (resolvedCount: Int, totalCount: Int, current: TikTokActivityItem) -> Unit = { _, _, _ -> }
    ): List<TikTokActivityItem> = coroutineScope {
        val targets = dataStorage.getItemsNeedingMetadataResolution(
            category = category,
            forceRefresh = forceRefresh
        )
        if (targets.isEmpty()) return@coroutineScope emptyList()

        var completed = 0
        val total = targets.size
        val resolvedItems = targets.map { item ->
            async {
                val meta: OEmbedMetadata = oEmbedService.fetchVideoMetadata(item.videoUrl)
                val nextStatus = when {
                    meta.thumbnailUrl != null -> VideoItemStatus.LOADED
                    else -> VideoItemStatus.THUMBNAIL_UNAVAILABLE
                }
                val resolvedItem = item.copy(
                    thumbnailUrl = meta.thumbnailUrl ?: item.thumbnailUrl,
                    creatorUsername = meta.creatorUsername ?: item.creatorUsername,
                    description = meta.title ?: item.description,
                    status = nextStatus,
                    errorReason = meta.errorReason
                )
                dataStorage.updateCachedItem(resolvedItem)
                completed++
                onItemResolved(completed, total, resolvedItem)
                resolvedItem
            }
        }.awaitAll()
        resolvedItems
    }

    suspend fun getActivityPage(
        category: ActivityCategory,
        searchQuery: String = "",
        sortNewest: Boolean = true,
        limit: Int = 60,
        offset: Int = 0
    ): List<TikTokActivityItem> = dataStorage.getActivityPage(
        category = category,
        searchQuery = searchQuery,
        sortNewest = sortNewest,
        limit = limit,
        offset = offset
    )

    suspend fun setItemSelected(id: String, selected: Boolean) {
        dataStorage.setItemSelected(id, selected)
    }

    suspend fun setAllSelected(category: ActivityCategory, selected: Boolean) {
        dataStorage.setAllSelected(category, selected)
    }

    suspend fun markItemVerifiedCleaned(id: String) {
        dataStorage.markItemVerifiedCleaned(id)
    }

    suspend fun markItemRequiresManualAction(id: String) {
        dataStorage.markItemRequiresManualAction(
            id = id,
            reason = "TikTok API v2 has no automated mutation endpoint; complete in TikTok app."
        )
    }

    suspend fun recordCleaningSession(
        category: ActivityCategory,
        totalAnalyzed: Int,
        items: List<TikTokActivityItem>,
        status: CleaningProcessStatus
    ): Long = dataStorage.recordCleaningSession(
        category = category,
        totalAnalyzed = totalAnalyzed,
        items = items,
        status = status
    )

    suspend fun logout() {
        tokenStorage.clearSession()
    }

    suspend fun disconnectAccount() {
        val token = tokenStorage.getAccessToken()
        if (!token.isNullOrBlank()) {
            authService.revokeToken(token)
        }
        tokenStorage.clearSession()
    }

    suspend fun deleteAllLocalData() {
        dataStorage.clearAllCachedData()
    }
}
