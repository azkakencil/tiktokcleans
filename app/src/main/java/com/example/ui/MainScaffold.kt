package com.example.ui

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.domain.model.ActivityCategory
import com.example.domain.model.TikTokUserProfile
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PasteLinksDialog
import com.example.ui.navigation.DashboardRoute
import com.example.ui.navigation.FavoritesCleanerRoute
import com.example.ui.navigation.FinalConfirmRoute
import com.example.ui.navigation.HistoryDetailRoute
import com.example.ui.navigation.HistoryRoute
import com.example.ui.navigation.LikeCleanerRoute
import com.example.ui.navigation.ProcessingRoute
import com.example.ui.navigation.RepostCleanerRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.navigation.SmartAnalysisRoute
import com.example.ui.screens.ActivityCleanerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinalConfirmScreen
import com.example.ui.screens.HistoryDetailScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProcessingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmartAnalysisScreen
import com.example.ui.theme.LocalGlassColors
import kotlinx.coroutines.launch

private data class SidebarMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val testTag: String
)

@Composable
fun TikTokCleanerRoot(
    viewModel: MainViewModel,
    onLaunchOAuthUri: (Uri) -> Unit,
    onPickDataJson: () -> Unit,
    onOpenVideoUrl: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val strings = remember(language) { stringsFor(language) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showPasteLinksDialog by remember { mutableStateOf(false) }
    var pasteLinksInitialCategory by remember { mutableStateOf(ActivityCategory.LIKES) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MainUiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is MainUiEvent.LaunchOAuthBrowser -> onLaunchOAuthUri(event.uri)
                is MainUiEvent.NavigateToSessionReport -> Unit
            }
        }
    }

    if (userProfile == null) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                LoginScreen(
                    strings = strings,
                    isClientConfigured = viewModel.isTikTokClientConfigured,
                    isAuthLoading = isAuthLoading,
                    onContinueWithTikTok = viewModel::startTikTokOAuth,
                    onImportDataExport = onPickDataJson,
                    onPasteVideoLinks = {
                        pasteLinksInitialCategory = ActivityCategory.LIKES
                        showPasteLinksDialog = true
                    },
                    onContinueInspector = viewModel::continueWithOfficialDataInspector
                )
            }
        }
    } else {
        AuthenticatedShell(
            viewModel = viewModel,
            strings = strings,
            userProfile = userProfile,
            snackbarHostState = snackbarHostState,
            onPickDataJson = onPickDataJson,
            onOpenPasteLinks = { cat ->
                pasteLinksInitialCategory = cat
                showPasteLinksDialog = true
            },
            onOpenVideoUrl = onOpenVideoUrl
        )
    }

    if (showPasteLinksDialog) {
        PasteLinksDialog(
            initialCategory = pasteLinksInitialCategory,
            strings = strings,
            onDismiss = { showPasteLinksDialog = false },
            onSubmit = { raw, cat ->
                showPasteLinksDialog = false
                viewModel.addVideoLinks(raw, cat)
            }
        )
    }
}

@Composable
private fun AuthenticatedShell(
    viewModel: MainViewModel,
    strings: AppStrings,
    userProfile: TikTokUserProfile?,
    snackbarHostState: SnackbarHostState,
    onPickDataJson: () -> Unit,
    onOpenPasteLinks: (ActivityCategory) -> Unit,
    onOpenVideoUrl: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items by viewModel.allItems.collectAsStateWithLifecycle()
    val ownVideos by viewModel.ownVideos.collectAsStateWithLifecycle()
    val sessions by viewModel.cleaningSessions.collectAsStateWithLifecycle()
    val totalCleaned by viewModel.totalCleanedCount.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val runState by viewModel.analysisRunState.collectAsStateWithLifecycle()
    val filteredItems by viewModel.filteredAnalysisItems.collectAsStateWithLifecycle()
    val analysisSummary by viewModel.analysisSummary.collectAsStateWithLifecycle()
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val hideHistoryThumbs by viewModel.hideHistoryThumbs.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is MainUiEvent.NavigateToSessionReport) {
                navController.navigate(HistoryDetailRoute(event.sessionId)) {
                    popUpTo(DashboardRoute) { inclusive = false }
                }
            }
        }
    }

    val sidebarItems = remember(strings) {
        listOf(
            SidebarMenuItem(strings.dashboard, Icons.Rounded.Dashboard, DashboardRoute, "nav_dashboard"),
            SidebarMenuItem(strings.likeCleaner, Icons.Rounded.Favorite, LikeCleanerRoute, "nav_likes"),
            SidebarMenuItem(strings.favoritesCleaner, Icons.Rounded.Bookmark, FavoritesCleanerRoute, "nav_favorites"),
            SidebarMenuItem(strings.repostCleaner, Icons.Rounded.Repeat, RepostCleanerRoute, "nav_reposts"),
            SidebarMenuItem(strings.smartDeleteAnalysis, Icons.Rounded.AutoAwesome, SmartAnalysisRoute("ALL"), "nav_analysis"),
            SidebarMenuItem(strings.cleaningHistory, Icons.Rounded.History, HistoryRoute, "nav_history"),
            SidebarMenuItem(strings.settings, Icons.Rounded.Settings, SettingsRoute, "nav_settings")
        )
    }

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(DashboardRoute) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun launchAnalysis(category: ActivityCategory) {
        viewModel.runSmartAnalysis(category)
        navController.navigate(SmartAnalysisRoute(category.name)) {
            launchSingleTop = true
        }
    }

    LiquidGlassBackground {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 720.dp

            if (isExpandedScreen) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Persistent Liquid Glass Sidebar for Tablets / Foldables / Desktop
                    SidebarContent(
                        strings = strings,
                        userProfile = userProfile,
                        menuItems = sidebarItems,
                        isSelected = { item -> isRouteSelected(currentDest, item.route) },
                        onItemClick = { item -> navigateTopLevel(item.route) },
                        onLogout = viewModel::logout,
                        modifier = Modifier
                            .width(268.dp)
                            .fillMaxHeight()
                            .statusBarsPadding()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(12.dp)
                    )

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .statusBarsPadding(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.widthIn(max = 980.dp).fillMaxSize()) {
                                MainNavHost(
                                    navController = navController,
                                    viewModel = viewModel,
                                    strings = strings,
                                    userProfile = userProfile,
                                    items = items,
                                    ownVideos = ownVideos,
                                    sessions = sessions,
                                    totalCleaned = totalCleaned,
                                    filterState = filterState,
                                    runState = runState,
                                    filteredItems = filteredItems,
                                    analysisSummary = analysisSummary,
                                    processingState = processingState,
                                    isDarkMode = isDarkMode,
                                    language = language,
                                    hideHistoryThumbs = hideHistoryThumbs,
                                    onLaunchAnalysis = ::launchAnalysis,
                                    onPickDataJson = onPickDataJson,
                                    onOpenPasteLinks = onOpenPasteLinks,
                                    onOpenVideoUrl = onOpenVideoUrl
                                )
                            }
                        }
                    }
                }
            } else {
                // Compact Mobile Layout: Modal Sidebar Drawer + Top Header + Liquid Glass Bottom Navigation
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            drawerContainerColor = glass.secondarySurface,
                            drawerContentColor = glass.textPrimary
                        ) {
                            SidebarContent(
                                strings = strings,
                                userProfile = userProfile,
                                menuItems = sidebarItems,
                                isSelected = { item -> isRouteSelected(currentDest, item.route) },
                                onItemClick = { item ->
                                    scope.launch { drawerState.close() }
                                    navigateTopLevel(item.route)
                                },
                                onLogout = {
                                    scope.launch { drawerState.close() }
                                    viewModel.logout()
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(12.dp)
                            )
                        }
                    }
                ) {
                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            Surface(
                                color = glass.glassFill,
                                border = BorderStroke(1.dp, glass.glassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        IconButton(
                                            onClick = { scope.launch { drawerState.open() } },
                                            modifier = Modifier.testTag("open_drawer_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Menu,
                                                contentDescription = "Menu",
                                                tint = glass.textPrimary
                                            )
                                        }
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_art_1794662452823),
                                            contentDescription = strings.appName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Text(
                                            text = strings.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = glass.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    GlassStatusBadge(
                                        text = if (userProfile?.isDataExportMode == false) "OAuth" else "Inspector",
                                        color = if (userProfile?.isDataExportMode == false) glass.success else glass.accent
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            Surface(
                                color = glass.secondarySurface.copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, glass.glassBorder),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    val bottomItems = listOf(
                                        SidebarMenuItem(strings.dashboard, Icons.Rounded.Dashboard, DashboardRoute, "bottom_dashboard"),
                                        SidebarMenuItem("Likes", Icons.Rounded.Favorite, LikeCleanerRoute, "bottom_likes"),
                                        SidebarMenuItem("Analyze", Icons.Rounded.AutoAwesome, SmartAnalysisRoute("ALL"), "bottom_analyze"),
                                        SidebarMenuItem("History", Icons.Rounded.History, HistoryRoute, "bottom_history"),
                                        SidebarMenuItem(strings.settings, Icons.Rounded.Settings, SettingsRoute, "bottom_settings")
                                    )
                                    bottomItems.forEach { item ->
                                        val selected = isRouteSelected(currentDest, item.route)
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTopLevel(item.route) },
                                            icon = {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.label,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            modifier = Modifier.testTag(item.testTag),
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = glass.primary,
                                                selectedTextColor = glass.primary,
                                                indicatorColor = glass.primary.copy(alpha = 0.2f),
                                                unselectedIconColor = glass.textSecondary,
                                                unselectedTextColor = glass.textSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            MainNavHost(
                                navController = navController,
                                viewModel = viewModel,
                                strings = strings,
                                userProfile = userProfile,
                                items = items,
                                ownVideos = ownVideos,
                                sessions = sessions,
                                totalCleaned = totalCleaned,
                                filterState = filterState,
                                runState = runState,
                                filteredItems = filteredItems,
                                analysisSummary = analysisSummary,
                                processingState = processingState,
                                isDarkMode = isDarkMode,
                                language = language,
                                hideHistoryThumbs = hideHistoryThumbs,
                                onLaunchAnalysis = ::launchAnalysis,
                                onPickDataJson = onPickDataJson,
                                onOpenPasteLinks = onOpenPasteLinks,
                                onOpenVideoUrl = onOpenVideoUrl
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SidebarContent(
    strings: AppStrings,
    userProfile: TikTokUserProfile?,
    menuItems: List<SidebarMenuItem>,
    isSelected: (SidebarMenuItem) -> Boolean,
    onItemClick: (SidebarMenuItem) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 26.dp,
        elevated = true,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_art_1794662452823),
                        contentDescription = strings.appName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                }

                // Connected User Profile Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = userProfile?.avatarUrl,
                                contentDescription = "User avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, glass.primary, CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_art_1794662452823),
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (userProfile?.isDataExportMode == false) "@${userProfile.username}" else strings.dataExportModeStatus,
                                style = MaterialTheme.typography.labelLarge,
                                color = glass.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (userProfile?.isDataExportMode == false) strings.connectedStatus else strings.officialApiBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (userProfile?.isDataExportMode == false) glass.success else glass.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                menuItems.forEach { item ->
                    val selected = isSelected(item)
                    NavigationDrawerItem(
                        selected = selected,
                        onClick = { onItemClick(item) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.testTag(item.testTag),
                        shape = RoundedCornerShape(16.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = glass.primary.copy(alpha = 0.22f),
                            selectedIconColor = glass.primary,
                            selectedTextColor = glass.textPrimary,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = glass.textSecondary,
                            unselectedTextColor = glass.textSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            GlassOutlinedButton(
                text = strings.logout,
                icon = Icons.AutoMirrored.Rounded.Logout,
                tint = glass.danger,
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun isRouteSelected(dest: androidx.navigation.NavDestination?, route: Any): Boolean {
    if (dest == null) return false
    return when (route) {
        is DashboardRoute -> dest.hasRoute<DashboardRoute>()
        is LikeCleanerRoute -> dest.hasRoute<LikeCleanerRoute>()
        is FavoritesCleanerRoute -> dest.hasRoute<FavoritesCleanerRoute>()
        is RepostCleanerRoute -> dest.hasRoute<RepostCleanerRoute>()
        is SmartAnalysisRoute -> dest.hasRoute<SmartAnalysisRoute>()
        is HistoryRoute -> dest.hasRoute<HistoryRoute>() || dest.hasRoute<HistoryDetailRoute>()
        is SettingsRoute -> dest.hasRoute<SettingsRoute>()
        else -> false
    }
}

@Composable
private fun MainNavHost(
    navController: androidx.navigation.NavHostController,
    viewModel: MainViewModel,
    strings: AppStrings,
    userProfile: TikTokUserProfile?,
    items: List<com.example.domain.model.TikTokActivityItem>,
    ownVideos: List<com.example.data.remote.TikTokOwnVideo>,
    sessions: List<com.example.domain.model.CleaningSessionSummary>,
    totalCleaned: Int,
    filterState: AnalysisFilterState,
    runState: AnalysisRunState,
    filteredItems: List<com.example.domain.model.TikTokActivityItem>,
    analysisSummary: com.example.domain.model.AnalysisSummary,
    processingState: ProcessingUiState,
    isDarkMode: Boolean,
    language: com.example.domain.model.AppLanguage,
    hideHistoryThumbs: Boolean,
    onLaunchAnalysis: (ActivityCategory) -> Unit,
    onPickDataJson: () -> Unit,
    onOpenPasteLinks: (ActivityCategory) -> Unit,
    onOpenVideoUrl: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoute
    ) {
        composable<DashboardRoute> {
            DashboardScreen(
                strings = strings,
                userProfile = userProfile,
                items = items,
                ownVideos = ownVideos,
                totalCleanedCount = totalCleaned,
                recentSessions = sessions,
                onQuickClean = { cat -> onLaunchAnalysis(cat) },
                onImportDataExport = onPickDataJson,
                onPasteLinks = { onOpenPasteLinks(ActivityCategory.LIKES) },
                onOpenHistory = { navController.navigate(HistoryRoute) },
                onOpenSessionDetail = { id -> navController.navigate(HistoryDetailRoute(id)) },
                onOpenVideoUrl = onOpenVideoUrl
            )
        }

        composable<LikeCleanerRoute> {
            ActivityCleanerScreen(
                category = ActivityCategory.LIKES,
                strings = strings,
                items = items,
                onToggleSelect = viewModel::toggleItemSelected,
                onSelectAll = { selected -> viewModel.selectAllInCategory(ActivityCategory.LIKES, selected) },
                onImportDataExport = onPickDataJson,
                onPasteLinks = { onOpenPasteLinks(ActivityCategory.LIKES) },
                onRunSmartAnalysis = { onLaunchAnalysis(ActivityCategory.LIKES) },
                onOpenInTikTok = onOpenVideoUrl
            )
        }

        composable<FavoritesCleanerRoute> {
            ActivityCleanerScreen(
                category = ActivityCategory.FAVORITES,
                strings = strings,
                items = items,
                onToggleSelect = viewModel::toggleItemSelected,
                onSelectAll = { selected -> viewModel.selectAllInCategory(ActivityCategory.FAVORITES, selected) },
                onImportDataExport = onPickDataJson,
                onPasteLinks = { onOpenPasteLinks(ActivityCategory.FAVORITES) },
                onRunSmartAnalysis = { onLaunchAnalysis(ActivityCategory.FAVORITES) },
                onOpenInTikTok = onOpenVideoUrl
            )
        }

        composable<RepostCleanerRoute> {
            ActivityCleanerScreen(
                category = ActivityCategory.REPOSTS,
                strings = strings,
                items = items,
                onToggleSelect = viewModel::toggleItemSelected,
                onSelectAll = { selected -> viewModel.selectAllInCategory(ActivityCategory.REPOSTS, selected) },
                onImportDataExport = onPickDataJson,
                onPasteLinks = { onOpenPasteLinks(ActivityCategory.REPOSTS) },
                onRunSmartAnalysis = { onLaunchAnalysis(ActivityCategory.REPOSTS) },
                onOpenInTikTok = onOpenVideoUrl
            )
        }

        composable<SmartAnalysisRoute> { backStack ->
            val route = backStack.toRoute<SmartAnalysisRoute>()
            val initialCat = runCatching { ActivityCategory.valueOf(route.categoryName) }
                .getOrDefault(ActivityCategory.ALL)

            LaunchedEffect(route.categoryName) {
                viewModel.setAnalysisCategory(initialCat)
            }

            val creators = remember(items) {
                items.map { it.creatorUsername }.filter { it.isNotBlank() }.distinct().sorted()
            }

            SmartAnalysisScreen(
                strings = strings,
                summary = analysisSummary,
                filterState = filterState,
                runState = runState,
                items = filteredItems,
                allCreators = creators,
                onCategoryChange = viewModel::setAnalysisCategory,
                onSearchChange = viewModel::updateSearchQuery,
                onCreatorFilterChange = viewModel::updateCreatorFilter,
                onDateFilterChange = viewModel::updateDateFilter,
                onSortChange = viewModel::updateSortOrder,
                onToggleSelect = viewModel::toggleItemSelected,
                onSelectAll = { viewModel.selectAllInCategory(filterState.category, true) },
                onDeselectAll = { viewModel.selectAllInCategory(filterState.category, false) },
                onReanalyzeThumbnails = { viewModel.runSmartAnalysis(filterState.category, forceRefresh = true) },
                onProceedToConfirmation = { navController.navigate(FinalConfirmRoute) },
                onImportDataExport = onPickDataJson,
                onPasteLinks = { onOpenPasteLinks(filterState.category) },
                onOpenInTikTok = onOpenVideoUrl
            )
        }

        composable<FinalConfirmRoute> {
            val selectedItems = remember(items, filterState.category) {
                items.filter {
                    (filterState.category == ActivityCategory.ALL || it.category == filterState.category) &&
                        it.isSelected
                }
            }
            FinalConfirmScreen(
                strings = strings,
                summary = analysisSummary,
                selectedItems = selectedItems,
                onCancel = { navController.popBackStack() },
                onConfirmDelete = {
                    viewModel.startCleaningProcess()
                    navController.navigate(ProcessingRoute) {
                        popUpTo(FinalConfirmRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ProcessingRoute> {
            ProcessingScreen(
                strings = strings,
                state = processingState,
                onCancelProcess = viewModel::cancelCleaningProcess,
                onOpenInTikTok = onOpenVideoUrl,
                onMarkItemDone = viewModel::markQueueItemVerifiedCleaned,
                onSaveAndFinish = viewModel::finalizeUpdatedReportToHistory
            )
        }

        composable<HistoryRoute> {
            HistoryScreen(
                strings = strings,
                sessions = sessions,
                onViewDetails = { id -> navController.navigate(HistoryDetailRoute(id)) },
                onStartAnalysis = { onLaunchAnalysis(ActivityCategory.ALL) }
            )
        }

        composable<HistoryDetailRoute> { backStack ->
            val route = backStack.toRoute<HistoryDetailRoute>()
            val session by viewModel.observeSessionById(route.sessionId).collectAsStateWithLifecycle(null)
            val sessionItems by viewModel.observeSessionItems(route.sessionId)
                .collectAsStateWithLifecycle(emptyList())

            HistoryDetailScreen(
                strings = strings,
                session = session,
                items = sessionItems,
                hideThumbnails = hideHistoryThumbs,
                onBack = { navController.popBackStack() },
                onOpenInTikTok = onOpenVideoUrl
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                strings = strings,
                isDarkMode = isDarkMode,
                language = language,
                hideHistoryThumbnails = hideHistoryThumbs,
                userProfile = userProfile,
                onToggleDarkMode = viewModel::setDarkMode,
                onLanguageChange = viewModel::setLanguage,
                onToggleHideHistoryThumbs = viewModel::setHideHistoryThumbnails,
                onConnectTikTok = viewModel::startTikTokOAuth,
                onDeleteLocalData = viewModel::deleteLocalData,
                onDisconnectAccount = viewModel::disconnectAccount,
                onLogout = viewModel::logout
            )
        }
    }
}
