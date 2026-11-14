package com.example.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.TikTokOwnVideo
import com.example.data.repository.TikTokCleanerRepository
import com.example.domain.model.ActivityCategory
import com.example.domain.model.AnalysisSummary
import com.example.domain.model.AppLanguage
import com.example.domain.model.CleaningProcessStatus
import com.example.domain.model.CleaningSessionItemDetail
import com.example.domain.model.CleaningSessionSummary
import com.example.domain.model.DateFilter
import com.example.domain.model.SortOrder
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.TikTokUserProfile
import com.example.domain.model.VideoItemStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalysisFilterState(
    val category: ActivityCategory = ActivityCategory.ALL,
    val searchQuery: String = "",
    val creatorFilter: String? = null,
    val dateFilter: DateFilter = DateFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEWEST
)

data class AnalysisRunState(
    val isAnalyzing: Boolean = false,
    val resolvedCount: Int = 0,
    val totalCount: Int = 0,
    val currentTitle: String = ""
)

data class ProcessingUiState(
    val isProcessing: Boolean = false,
    val isCancelled: Boolean = false,
    val isFinished: Boolean = false,
    val category: ActivityCategory = ActivityCategory.ALL,
    val totalAnalyzedAtStart: Int = 0,
    val queue: List<TikTokActivityItem> = emptyList(),
    val currentIndex: Int = -1,
    val savedSessionId: Long? = null
) {
    val totalToProcess: Int get() = queue.size
    val processedCount: Int
        get() = queue.count {
            it.status == VideoItemStatus.REQUIRES_MANUAL_ACTION ||
                it.status == VideoItemStatus.VERIFIED_CLEANED ||
                it.status == VideoItemStatus.FAILED ||
                it.status == VideoItemStatus.THUMBNAIL_UNAVAILABLE
        }
    val succeededCount: Int get() = queue.count { it.status == VideoItemStatus.VERIFIED_CLEANED }
    val manualActionCount: Int get() = queue.count { it.status == VideoItemStatus.REQUIRES_MANUAL_ACTION }
    val failedCount: Int
        get() = queue.count {
            it.status == VideoItemStatus.FAILED || it.status == VideoItemStatus.THUMBNAIL_UNAVAILABLE
        }
    val remainingCount: Int get() = (totalToProcess - processedCount).coerceAtLeast(0)
    val currentItem: TikTokActivityItem? get() = queue.getOrNull(currentIndex)
    val progressFraction: Float
        get() = if (totalToProcess == 0) 0f else processedCount.toFloat() / totalToProcess.toFloat()
}

sealed interface MainUiEvent {
    data class ShowToast(val message: String, val isError: Boolean = false) : MainUiEvent
    data class LaunchOAuthBrowser(val uri: Uri) : MainUiEvent
    data class NavigateToSessionReport(val sessionId: Long) : MainUiEvent
}

class MainViewModel(
    private val repository: TikTokCleanerRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val language: StateFlow<AppLanguage> = repository.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.EN)

    val hideHistoryThumbs: StateFlow<Boolean> = repository.hideHistoryThumbs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userProfile: StateFlow<TikTokUserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allItems: StateFlow<List<TikTokActivityItem>> = repository.allActivityItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cleaningSessions: StateFlow<List<CleaningSessionSummary>> = repository.cleaningSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCleanedCount: StateFlow<Int> = repository.totalCleanedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _ownVideos = MutableStateFlow<List<TikTokOwnVideo>>(emptyList())
    val ownVideos: StateFlow<List<TikTokOwnVideo>> = _ownVideos.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _filterState = MutableStateFlow(AnalysisFilterState())
    val filterState: StateFlow<AnalysisFilterState> = _filterState.asStateFlow()

    private val _analysisRunState = MutableStateFlow(AnalysisRunState())
    val analysisRunState: StateFlow<AnalysisRunState> = _analysisRunState.asStateFlow()

    private val _processingState = MutableStateFlow(ProcessingUiState())
    val processingState: StateFlow<ProcessingUiState> = _processingState.asStateFlow()

    private val _events = MutableSharedFlow<MainUiEvent>()
    val events: SharedFlow<MainUiEvent> = _events.asSharedFlow()

    private var analysisJob: Job? = null
    private var processingJob: Job? = null

    val isTikTokClientConfigured: Boolean
        get() = repository.isTikTokClientConfigured()

    val filteredAnalysisItems: StateFlow<List<TikTokActivityItem>> = combine(
        allItems,
        _filterState
    ) { items, filter ->
        val now = System.currentTimeMillis()
        val sevenDaysMs = 7L * 24 * 60 * 60 * 1000
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        items
            .filter { item ->
                filter.category == ActivityCategory.ALL || item.category == filter.category
            }
            .filter { item ->
                filter.creatorFilter == null || item.creatorUsername.equals(filter.creatorFilter, ignoreCase = true)
            }
            .filter { item ->
                val q = filter.searchQuery.trim()
                if (q.isEmpty()) true
                else item.creatorUsername.contains(q, ignoreCase = true) ||
                    item.description.contains(q, ignoreCase = true) ||
                    item.videoUrl.contains(q, ignoreCase = true)
            }
            .filter { item ->
                when (filter.dateFilter) {
                    DateFilter.ALL -> true
                    DateFilter.LAST_7_DAYS -> item.activityTimestampMs > 0 && (now - item.activityTimestampMs) <= sevenDaysMs
                    DateFilter.LAST_30_DAYS -> item.activityTimestampMs > 0 && (now - item.activityTimestampMs) <= thirtyDaysMs
                    DateFilter.OLDER -> item.activityTimestampMs == 0L || (now - item.activityTimestampMs) > thirtyDaysMs
                }
            }
            .let { list ->
                when (filter.sortOrder) {
                    SortOrder.NEWEST -> list.sortedByDescending { it.activityTimestampMs }
                    SortOrder.OLDEST -> list.sortedBy { it.activityTimestampMs }
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analysisSummary: StateFlow<AnalysisSummary> = combine(
        allItems,
        _filterState
    ) { items, filter ->
        val scoped = items.filter {
            filter.category == ActivityCategory.ALL || it.category == filter.category
        }
        val selected = scoped.filter { it.isSelected }
        val loadedReady = scoped.count { it.status == VideoItemStatus.LOADED }
        val failedLoad = scoped.count {
            it.status == VideoItemStatus.THUMBNAIL_UNAVAILABLE || it.status == VideoItemStatus.FAILED
        }
        AnalysisSummary(
            category = filter.category,
            totalFound = scoped.size,
            totalSelected = selected.size,
            readyToProcess = loadedReady,
            failedToLoad = failedLoad,
            requiresManualAction = scoped.size,
            likesSelected = selected.count { it.category == ActivityCategory.LIKES },
            favoritesSelected = selected.count { it.category == ActivityCategory.FAVORITES },
            repostsSelected = selected.count { it.category == ActivityCategory.REPOSTS }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AnalysisSummary(ActivityCategory.ALL, 0, 0, 0, 0, 0, 0, 0, 0)
    )

    init {
        viewModelScope.launch {
            val profile = repository.refreshAuthenticatedProfileAndOwnVideos()
            _ownVideos.value = profile.second
        }
    }

    fun startTikTokOAuth() {
        viewModelScope.launch {
            if (!isTikTokClientConfigured) {
                _events.emit(
                    MainUiEvent.ShowToast(
                        "TIKTOK_CLIENT_KEY is not configured in Secrets. Use Official Data Inspector or add your TikTok Client Key.",
                        isError = true
                    )
                )
                return@launch
            }
            val uri = repository.prepareTikTokOAuthUri()
            _events.emit(MainUiEvent.LaunchOAuthBrowser(uri))
        }
    }

    fun handleOAuthRedirect(uri: Uri) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = repository.handleOAuthCallback(uri)
            _isAuthLoading.value = false
            if (result.success) {
                val refreshed = repository.refreshAuthenticatedProfileAndOwnVideos()
                _ownVideos.value = refreshed.second
                _events.emit(MainUiEvent.ShowToast("Connected as @${result.profile?.username ?: "TikTok"}"))
            } else {
                _events.emit(
                    MainUiEvent.ShowToast(
                        result.errorMessage ?: "TikTok OAuth failed",
                        isError = true
                    )
                )
            }
        }
    }

    fun continueWithOfficialDataInspector() {
        viewModelScope.launch {
            repository.enterOfficialDataInspectorMode()
        }
    }

    fun importDataExportFile(uri: Uri) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = repository.importDataExportFromUri(uri)
            _isAuthLoading.value = false
            result.onSuccess { count ->
                if (count > 0) {
                    if (userProfile.value == null) {
                        repository.enterOfficialDataInspectorMode()
                    }
                    _events.emit(MainUiEvent.ShowToast("Imported $count TikTok activity items. Starting thumbnail verification..."))
                    runSmartAnalysis(ActivityCategory.ALL)
                } else {
                    _events.emit(
                        MainUiEvent.ShowToast(
                            "No Likes, Favorites, or Reposts found in this JSON file.",
                            isError = true
                        )
                    )
                }
            }.onFailure { err ->
                _events.emit(
                    MainUiEvent.ShowToast(
                        err.localizedMessage ?: "Failed to read TikTok JSON export",
                        isError = true
                    )
                )
            }
        }
    }

    fun addVideoLinks(rawLinks: String, category: ActivityCategory) {
        viewModelScope.launch {
            val added = repository.addLinksForCategory(rawLinks, category)
            if (added > 0) {
                if (userProfile.value == null) {
                    repository.enterOfficialDataInspectorMode()
                }
                _events.emit(MainUiEvent.ShowToast("Added $added video URL(s). Fetching real thumbnails..."))
                runSmartAnalysis(category)
            } else {
                _events.emit(
                    MainUiEvent.ShowToast(
                        "Please enter valid TikTok video URLs (tiktok.com).",
                        isError = true
                    )
                )
            }
        }
    }

    fun setAnalysisCategory(category: ActivityCategory) {
        _filterState.update { it.copy(category = category, creatorFilter = null) }
    }

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updateCreatorFilter(creator: String?) {
        _filterState.update { it.copy(creatorFilter = creator) }
    }

    fun updateDateFilter(dateFilter: DateFilter) {
        _filterState.update { it.copy(dateFilter = dateFilter) }
    }

    fun updateSortOrder(sortOrder: SortOrder) {
        _filterState.update { it.copy(sortOrder = sortOrder) }
    }

    fun toggleItemSelected(id: String, selected: Boolean) {
        viewModelScope.launch {
            repository.setItemSelected(id, selected)
        }
    }

    fun selectAllInCategory(category: ActivityCategory, selected: Boolean) {
        viewModelScope.launch {
            repository.setAllSelected(category, selected)
        }
    }

    fun runSmartAnalysis(category: ActivityCategory, forceRefresh: Boolean = false) {
        analysisJob?.cancel()
        _filterState.update { it.copy(category = category) }
        analysisJob = viewModelScope.launch {
            val pendingCount = allItems.value.count {
                (category == ActivityCategory.ALL || it.category == category) &&
                    it.status != VideoItemStatus.VERIFIED_CLEANED &&
                    (forceRefresh || it.status == VideoItemStatus.PENDING_ANALYSIS || it.thumbnailUrl == null)
            }
            if (pendingCount == 0) {
                _analysisRunState.value = AnalysisRunState(isAnalyzing = false)
                return@launch
            }
            _analysisRunState.value = AnalysisRunState(
                isAnalyzing = true,
                resolvedCount = 0,
                totalCount = pendingCount
            )
            repository.analyzeCategoryThumbnails(category, forceRefresh = forceRefresh) { resolved, total, current ->
                _analysisRunState.value = AnalysisRunState(
                    isAnalyzing = true,
                    resolvedCount = resolved,
                    totalCount = total,
                    currentTitle = "@${current.creatorUsername}"
                )
            }
            _analysisRunState.value = AnalysisRunState(
                isAnalyzing = false,
                resolvedCount = pendingCount,
                totalCount = pendingCount
            )
        }
    }

    fun startCleaningProcess() {
        val summary = analysisSummary.value
        val selectedItems = allItems.value.filter {
            (summary.category == ActivityCategory.ALL || it.category == summary.category) && it.isSelected
        }
        if (selectedItems.isEmpty()) return

        processingJob?.cancel()
        _processingState.value = ProcessingUiState(
            isProcessing = true,
            isCancelled = false,
            isFinished = false,
            category = summary.category,
            totalAnalyzedAtStart = summary.totalFound,
            queue = selectedItems,
            currentIndex = 0
        )

        processingJob = viewModelScope.launch {
            val workingList = selectedItems.toMutableList()
            for (index in workingList.indices) {
                if (_processingState.value.isCancelled) break

                _processingState.update { it.copy(currentIndex = index) }
                val item = workingList[index]

                // Respectful API pacing & real capability enforcement:
                // Official TikTok API v2 does not provide write endpoints for Unlike / Unfavorite / Undo Repost.
                delay(380L)

                val nextStatus = if (item.status == VideoItemStatus.THUMBNAIL_UNAVAILABLE) {
                    VideoItemStatus.THUMBNAIL_UNAVAILABLE
                } else {
                    repository.markItemRequiresManualAction(item.id)
                    VideoItemStatus.REQUIRES_MANUAL_ACTION
                }
                workingList[index] = item.copy(
                    status = nextStatus,
                    errorReason = if (nextStatus == VideoItemStatus.REQUIRES_MANUAL_ACTION) {
                        "TikTok API v2 has no automated mutation endpoint; complete in TikTok app."
                    } else {
                        item.errorReason
                    }
                )
                _processingState.update { it.copy(queue = workingList.toList()) }
            }

            val wasCancelled = _processingState.value.isCancelled
            val finalStatus = if (wasCancelled) {
                CleaningProcessStatus.CANCELLED
            } else {
                CleaningProcessStatus.REQUIRES_MANUAL_ACTION
            }

            val sessionId = repository.recordCleaningSession(
                category = summary.category,
                totalAnalyzed = summary.totalFound,
                items = workingList,
                status = finalStatus
            )

            _processingState.update {
                it.copy(
                    isProcessing = false,
                    isFinished = true,
                    savedSessionId = sessionId
                )
            }
        }
    }

    fun markQueueItemVerifiedCleaned(itemId: String) {
        viewModelScope.launch {
            repository.markItemVerifiedCleaned(itemId)
            val updatedQueue = _processingState.value.queue.map { item ->
                if (item.id == itemId) {
                    item.copy(status = VideoItemStatus.VERIFIED_CLEANED, isSelected = false)
                } else {
                    item
                }
            }
            _processingState.update { it.copy(queue = updatedQueue) }
        }
    }

    fun cancelCleaningProcess() {
        viewModelScope.launch {
            _processingState.update { it.copy(isCancelled = true) }
        }
    }

    fun finalizeUpdatedReportToHistory() {
        viewModelScope.launch {
            val state = _processingState.value
            if (state.queue.isEmpty()) return@launch
            val succeeded = state.succeededCount
            val total = state.queue.size
            val status = when {
                state.isCancelled -> CleaningProcessStatus.CANCELLED
                succeeded == total -> CleaningProcessStatus.COMPLETED
                succeeded > 0 -> CleaningProcessStatus.PARTIAL
                else -> CleaningProcessStatus.REQUIRES_MANUAL_ACTION
            }
            val sessionId = repository.recordCleaningSession(
                category = state.category,
                totalAnalyzed = state.totalAnalyzedAtStart,
                items = state.queue,
                status = status
            )
            _events.emit(MainUiEvent.NavigateToSessionReport(sessionId))
        }
    }

    fun observeSessionById(sessionId: Long): Flow<CleaningSessionSummary?> =
        repository.observeSessionById(sessionId)

    fun observeSessionItems(sessionId: Long): Flow<List<CleaningSessionItemDetail>> =
        repository.observeSessionItems(sessionId)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { repository.setLanguage(language) }
    }

    fun setHideHistoryThumbnails(hide: Boolean) {
        viewModelScope.launch { repository.setHideHistoryThumbnails(hide) }
    }

    fun deleteLocalData() {
        viewModelScope.launch {
            repository.deleteAllLocalData()
            _events.emit(MainUiEvent.ShowToast("Local activity & history data deleted."))
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _ownVideos.value = emptyList()
        }
    }

    fun disconnectAccount() {
        viewModelScope.launch {
            repository.disconnectAccount()
            _ownVideos.value = emptyList()
            _events.emit(MainUiEvent.ShowToast("TikTok account disconnected and OAuth tokens revoked."))
        }
    }

    class Factory(private val repository: TikTokCleanerRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
