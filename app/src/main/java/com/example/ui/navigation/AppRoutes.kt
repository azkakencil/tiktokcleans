package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object DashboardRoute

@Serializable
object LikeCleanerRoute

@Serializable
object FavoritesCleanerRoute

@Serializable
object RepostCleanerRoute

@Serializable
data class SmartAnalysisRoute(val categoryName: String = "ALL")

@Serializable
object FinalConfirmRoute

@Serializable
object ProcessingRoute

@Serializable
object HistoryRoute

@Serializable
data class HistoryDetailRoute(val sessionId: Long)

@Serializable
object SettingsRoute
