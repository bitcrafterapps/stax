package com.bitcraftapps.stax.navigation

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bitcraftapps.stax.data.AppDatabase
import com.bitcraftapps.stax.data.CasinoFoldersViewModel
import com.bitcraftapps.stax.data.CasinoSessionsViewModel
import com.bitcraftapps.stax.data.CasinoSessionsViewModelFactory
import com.bitcraftapps.stax.data.Photo
import com.bitcraftapps.stax.data.PhotoGalleryViewModel
import com.bitcraftapps.stax.data.SessionsViewModel
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.data.billing.LimitResult
import com.bitcraftapps.stax.data.billing.LocalBillingRepository
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.ui.screens.AboutScreen
import com.bitcraftapps.stax.ui.screens.AddSessionDialog
import com.bitcraftapps.stax.ui.screens.CameraScreen
import com.bitcraftapps.stax.ui.screens.CasinoSessionsScreen
import com.bitcraftapps.stax.ui.screens.ChipConfigurationScreen
import com.bitcraftapps.stax.ui.screens.DashboardScreen
import com.bitcraftapps.stax.ui.screens.FindScreen
import com.bitcraftapps.stax.ui.screens.FullScreenImageViewer
import com.bitcraftapps.stax.ui.screens.PaywallScreen
import com.bitcraftapps.stax.ui.screens.PhotoGalleryScreen
import com.bitcraftapps.stax.ui.screens.ReportsScreen
import com.bitcraftapps.stax.ui.screens.NutzGameScreen
import com.bitcraftapps.stax.ui.screens.ScanScreen
import com.bitcraftapps.stax.ui.screens.SessionDetailScreen
import com.bitcraftapps.stax.ui.screens.CardRoomDetailScreen
import com.bitcraftapps.stax.ui.screens.SessionsScreen
import com.bitcraftapps.stax.ui.screens.SplashScreen
import com.bitcraftapps.stax.data.CardRoomRepository
import com.bitcraftapps.stax.data.CardRoomWithDistance
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object Splash : Screen("splash")
    object Photos : Screen("photos", "Photos", Icons.Default.PhotoLibrary)
    object Sessions : Screen("sessions", "Sessions", Icons.AutoMirrored.Filled.ViewList)
    object Find : Screen("find", "Find", Icons.Default.NearMe)
    object Scan : Screen("scan", "Scan", Icons.Default.Camera)
    object About : Screen("about", "About", Icons.Default.Info)
    object Reports : Screen("reports", "Reports")
    object NutzGame : Screen("nutz_game", "Nutz Game")
    object CasinoSessions : Screen("casino_sessions/{casinoName}/{source}") {
        fun createRoute(casinoName: String, source: String = "sessions") =
            "casino_sessions/$casinoName/$source"
    }
    object ChipConfiguration : Screen("chip_configuration", "Chip Configuration")
    object Paywall : Screen("paywall")

    object SessionDetail : Screen("session/{sessionId}") {
        fun createRoute(sessionId: Long) = "session/$sessionId"
    }

    object Camera : Screen("camera/{sessionId}") {
        fun createRoute(sessionId: Long) = "camera/$sessionId"
    }

    object PhotoGallery : Screen("gallery/{sessionId}") {
        fun createRoute(sessionId: Long) = "gallery/$sessionId"
    }

    object FullScreenImageViewer : Screen("fullscreen/{photoIndex}") {
        fun createRoute(photoIndex: Int) = "fullscreen/$photoIndex"
    }

    object CardRoomDetail : Screen("card_room_detail/{encodedAddress}") {
        fun createRoute(address: String) =
            "card_room_detail/${URLEncoder.encode(address, "UTF-8")}"
    }
}

@Composable
fun AppNavigation(photosJson: MutableState<String>) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val entitlementManager = LocalEntitlementManager.current
    val billingRepository = LocalBillingRepository.current
    var showAddSessionDialog by remember { mutableStateOf(false) }

    fun navigateToPaywall() {
        navController.navigate(Screen.Paywall.route)
    }

    val navItems = listOf(Screen.Photos, Screen.Sessions, Screen.Find, Screen.Scan, Screen.About)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            val shouldShowBottomBar = navItems.any { it.route == currentDestination?.route }

            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = NavigationBarDefaults.Elevation
                ) {
                    navItems.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    screen.icon!!,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    screen.title!!,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Screen.Photos.route) {
                FloatingActionButton(
                    onClick = { showAddSessionDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Session")
                }
            }
        }
        // showAddSessionDialog is controlled via FAB; paywall check happens in DashboardScreen/SessionsScreen onConfirm
    ) { innerPadding ->
        if (showAddSessionDialog) {
            val viewModel: CasinoFoldersViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CasinoFoldersViewModel(
                            AppDatabase.getDatabase(application).staxDao(),
                            application
                        ) as T
                    }
                }
            )
            val casinoData by viewModel.casinoData.collectAsState()
            AddSessionDialog(
                casinoData = casinoData,
                homeGames = viewModel.homeGames.collectAsState().value,
                onConfirm = { name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut ->
                    viewModel.addSession(name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut)
                    showAddSessionDialog = false
                },
                onSaveHomeGame = { name, city, state -> viewModel.saveHomeGame(name, city, state) },
                onDismiss = { showAddSessionDialog = false }
            )
        }

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onTimeout = {
                    navController.navigate(Screen.Photos.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Paywall.route) {
                val billingRepo = LocalBillingRepository.current
                PaywallScreen(
                    onDismiss = { navController.popBackStack() },
                    onSubscribe = { productId ->
                        val products = billingRepo.products.value
                        val productDetails = products.firstOrNull { it.productId == productId }
                        if (productDetails != null) {
                            val offerDetails = productDetails.subscriptionOfferDetails
                            val offerToken = offerDetails?.firstOrNull()?.offerToken ?: ""
                            billingRepo.launchPurchaseFlow(context as android.app.Activity, productDetails, offerToken)
                        }
                    },
                    onRestore = {
                        billingRepo.restorePurchases()
                    }
                )
            }
            composable(Screen.Photos.route) {
                val viewModel: CasinoFoldersViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CasinoFoldersViewModel(
                                AppDatabase.getDatabase(application).staxDao(),
                                application
                            ) as T
                        }
                    }
                )
                val casinoFolders by viewModel.casinoFolders.collectAsState()
                val casinoData by viewModel.casinoData.collectAsState()
                val logoMap by viewModel.logoMap.collectAsState()
                val homeGames by viewModel.homeGames.collectAsState()

                DashboardScreen(
                    casinoFolders = casinoFolders,
                    onCasinoClick = { casinoName ->
                        navController.navigate(Screen.CasinoSessions.createRoute(casinoName, "photos"))
                    },
                    casinoData = casinoData,
                    onAddSession = { name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut ->
                        viewModel.addSession(name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut)
                    },
                    homeGames = homeGames,
                    onSaveHomeGame = { name, city, state -> viewModel.saveHomeGame(name, city, state) },
                    logoMap = logoMap,
                    onNavigateToPaywall = ::navigateToPaywall
                )
            }
            composable(Screen.Sessions.route) {
                val viewModel: SessionsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return SessionsViewModel(
                                AppDatabase.getDatabase(application).staxDao(),
                                application
                            ) as T
                        }
                    }
                )
                val sessions by viewModel.sessions.collectAsState()
                val sessionsLogoMap by viewModel.logoMap.collectAsState()
                val homeGames by viewModel.homeGames.collectAsState()
                SessionsScreen(
                    sessions = sessions,
                    onAddSession = { name, casinoName, date, type, game, gameType, stakes, antes, buyIn, cashOut ->
                        viewModel.addSession(name, casinoName, date, type, game, gameType, stakes, antes, buyIn, cashOut)
                    },
                    homeGames = homeGames,
                    onSaveHomeGame = { name, city, state -> viewModel.saveHomeGame(name, city, state) },
                    onCasinoClick = { casinoName ->
                        navController.navigate(Screen.CasinoSessions.createRoute(casinoName, "sessions"))
                    },
                    sessionsViewModel = viewModel,
                    logoMap = sessionsLogoMap,
                    onNavigateToPaywall = ::navigateToPaywall
                )
            }
            composable(Screen.Find.route) {
                FindScreen(
                    onCardRoomClick = { item ->
                        navController.navigate(Screen.CardRoomDetail.createRoute(item.room.address))
                    }
                )
            }

            composable(
                route = Screen.CardRoomDetail.route,
                arguments = listOf(navArgument("encodedAddress") { type = NavType.StringType })
            ) { entry ->
                val encoded = entry.arguments?.getString("encodedAddress") ?: ""
                val address = URLDecoder.decode(encoded, "UTF-8")
                val repo = remember { CardRoomRepository(context) }
                val room = remember(address) { repo.allRooms.firstOrNull { it.address == address } }
                var favorites by remember { mutableStateOf(repo.getFavorites()) }
                var homeCasino by remember { mutableStateOf(repo.getHomeCasino()) }

                if (room != null) {
                    CardRoomDetailScreen(
                        item = CardRoomWithDistance(room, null),
                        isFavorite = room.address in favorites,
                        isHomeCasino = room.address == homeCasino,
                        onToggleFavorite = {
                            val alreadyFavorite = room.address in favorites
                            if (!alreadyFavorite) {
                                val limitResult = entitlementManager.checkLimit(
                                    Feature.FAVORITES,
                                    favoritesCount = favorites.size
                                )
                                if (limitResult is LimitResult.Blocked) {
                                    // Snackbar shown via a side effect — for now skip adding
                                    return@CardRoomDetailScreen
                                }
                            }
                            favorites = repo.toggleFavorite(room.address)
                            homeCasino = repo.getHomeCasino()
                        },
                        onToggleHome = {
                            homeCasino = if (room.address == homeCasino) {
                                repo.setHomeCasino(null)
                            } else {
                                repo.setHomeCasino(room.address)
                            }
                            favorites = repo.getFavorites()
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Scan.route) {
                ScanScreen(onNavigateToPaywall = ::navigateToPaywall)
            }
            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateToChipConfiguration = {
                        navController.navigate(Screen.ChipConfiguration.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
                    },
                    onNavigateToNutzGame = {
                        navController.navigate(Screen.NutzGame.route)
                    },
                    onNavigateToPaywall = ::navigateToPaywall
                )
            }
            composable(Screen.Reports.route) {
                val viewModel: SessionsViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return SessionsViewModel(
                                AppDatabase.getDatabase(application).staxDao(),
                                application
                            ) as T
                        }
                    }
                )
                val sessions by viewModel.sessions.collectAsState()
                val homeGames by viewModel.homeGames.collectAsState()
                ReportsScreen(
                    sessions = sessions,
                    homeGames = homeGames,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.NutzGame.route) {
                NutzGameScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.ChipConfiguration.route) {
                val viewModel: CasinoFoldersViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CasinoFoldersViewModel(
                                AppDatabase.getDatabase(application).staxDao(),
                                application
                            ) as T
                        }
                    }
                )
                val casinoData by viewModel.casinoData.collectAsState()

                ChipConfigurationScreen(
                    casinoData = casinoData,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPaywall = ::navigateToPaywall
                )
            }
            composable(
                route = Screen.CasinoSessions.route,
                arguments = listOf(
                    navArgument("casinoName") { type = NavType.StringType },
                    navArgument("source") { type = NavType.StringType; defaultValue = "sessions" }
                )
            ) { backStackEntry ->
                val casinoName = backStackEntry.arguments?.getString("casinoName") ?: ""
                val source = backStackEntry.arguments?.getString("source") ?: "sessions"
                val viewModel: CasinoSessionsViewModel = viewModel(
                    factory = CasinoSessionsViewModelFactory(
                        AppDatabase.getDatabase(application).staxDao(),
                        casinoName,
                        application
                    )
                )
                val sessions by viewModel.sessions.collectAsState()
                val logoResName by viewModel.logoResName.collectAsState()

                CasinoSessionsScreen(
                    casinoName = casinoName,
                    sessions = sessions,
                    logoResName = logoResName,
                    onSessionClick = { sessionId ->
                        if (source == "photos") {
                            navController.navigate(Screen.PhotoGallery.createRoute(sessionId))
                        } else {
                            navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                        }
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteSession(sessionId)
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.SessionDetail.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                SessionDetailScreen(
                    sessionId = sessionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPhotos = { id -> navController.navigate(Screen.PhotoGallery.createRoute(id)) }
                )
            }
            composable(
                route = Screen.PhotoGallery.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                val viewModel: PhotoGalleryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return PhotoGalleryViewModel(
                                dao = AppDatabase.getDatabase(application).staxDao(),
                                sessionId = sessionId,
                                application = application
                            ) as T
                        }
                    }
                )
                val photos by viewModel.photos.collectAsState()
                val session by viewModel.session.collectAsState()
                photosJson.value = Gson().toJson(photos)

                PhotoGalleryScreen(
                    session = session,
                    photos = photos,
                    onNavigateUp = { navController.navigateUp() },
                    onNavigateToCamera = {
                        navController.navigate(Screen.Camera.createRoute(sessionId))
                    },
                    onNavigateToPaywall = ::navigateToPaywall,
                    onNavigateToSessionDetail = {
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    },
                    onAddPhotoFromGallery = { uri ->
                        viewModel.addPhoto(uri)
                    },
                    onDeletePhoto = { photo -> viewModel.deletePhoto(photo) },
                    onPhotoClick = { photo ->
                        val photoIndex = photos.indexOf(photo)
                        navController.navigate(Screen.FullScreenImageViewer.createRoute(photoIndex))
                    }
                )
            }
            composable(
                route = Screen.Camera.route,
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                val viewModel: PhotoGalleryViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return PhotoGalleryViewModel(
                                dao = AppDatabase.getDatabase(application).staxDao(),
                                sessionId = sessionId,
                                application = application
                            ) as T
                        }
                    }
                )
                CameraScreen(
                    onImageCaptured = { uri ->
                        viewModel.addPhoto(uri)
                        navController.popBackStack()
                    },
                    onError = { exception ->
                        // Handle error
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.FullScreenImageViewer.route,
                arguments = listOf(navArgument("photoIndex") { type = NavType.IntType })
            ) { backStackEntry ->
                val photoIndex = backStackEntry.arguments?.getInt("photoIndex") ?: 0
                val parsedPhotos = Gson().fromJson(photosJson.value, Array<Photo>::class.java)
                val photos = parsedPhotos?.toList() ?: emptyList()
                val photo = photos.getOrNull(photoIndex)

                if (photo != null) {
                    val viewModel: PhotoGalleryViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return PhotoGalleryViewModel(
                                    dao = AppDatabase.getDatabase(application).staxDao(),
                                    sessionId = photo.sessionId,
                                    application = application
                                ) as T
                            }
                        }
                    )
                    val livePhotos by viewModel.photos.collectAsState()
                    val captions by viewModel.captions.collectAsState()
                    val editVersions by viewModel.editVersions.collectAsState()

                    if (livePhotos.isNotEmpty()) {
                        FullScreenImageViewer(
                            photos = livePhotos,
                            initialPhotoIndex = photoIndex,
                            onNavigateUp = { navController.navigateUp() },
                            captionFor = { captions[it.id].orEmpty() },
                            editVersionFor = { editVersions[it.id] ?: 0 },
                            onRatingChanged = { ratedPhoto, rating ->
                                viewModel.updatePhoto(ratedPhoto.copy(rating = rating))
                            },
                            onSavePhotoEdits = { editedPhoto, bitmap, caption ->
                                viewModel.savePhotoEdits(editedPhoto, bitmap, caption)
                            }
                        )
                    }
                }
            }
        }
    }
} 