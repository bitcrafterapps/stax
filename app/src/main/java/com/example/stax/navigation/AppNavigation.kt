package com.example.stax.navigation

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.example.stax.data.AppDatabase
import com.example.stax.data.CasinoFoldersViewModel
import com.example.stax.data.CasinoSessionsViewModel
import com.example.stax.data.CasinoSessionsViewModelFactory
import com.example.stax.data.Photo
import com.example.stax.data.PhotoGalleryViewModel
import com.example.stax.data.SessionsViewModel
import com.example.stax.ui.screens.AboutScreen
import com.example.stax.ui.screens.AddSessionDialog
import com.example.stax.ui.screens.CameraScreen
import com.example.stax.ui.screens.CasinoSessionsScreen
import com.example.stax.ui.screens.DashboardScreen
import com.example.stax.ui.screens.FullScreenImageViewer
import com.example.stax.ui.screens.PhotoGalleryScreen
import com.example.stax.ui.screens.ScanScreen
import com.example.stax.ui.screens.SessionDetailScreen
import com.example.stax.ui.screens.SessionsScreen
import com.example.stax.ui.screens.SplashScreen
import com.google.gson.Gson

sealed class Screen(
    val route: String,
    val title: String? = null,
    val icon: ImageVector? = null
) {
    object Splash : Screen("splash")
    object Photos : Screen("photos", "Photos", Icons.Default.PhotoLibrary)
    object Sessions : Screen("sessions", "Sessions", Icons.Default.List)
    object Scan : Screen("scan", "Scan", Icons.Default.Camera)
    object About : Screen("about", "About", Icons.Default.Info)
    object CasinoSessions : Screen("casino_sessions/{casinoName}") {
        fun createRoute(casinoName: String) = "casino_sessions/$casinoName"
    }

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
}

@Composable
fun AppNavigation(photosJson: MutableState<String>) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    var showAddSessionDialog by remember { mutableStateOf(false) }

    val navItems = listOf(Screen.Photos, Screen.Sessions, Screen.Scan, Screen.About)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            val shouldShowBottomBar = navItems.any { it.route == currentDestination?.route }

            if (shouldShowBottomBar) {
                NavigationBar {
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title!!) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Screen.Photos.route) {
                FloatingActionButton(onClick = { showAddSessionDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Session")
                }
            }
        }
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
                onConfirm = { name, casinoName, sessionType, game, gameType, stakes, antes ->
                    viewModel.addSession(name, casinoName, sessionType, game, gameType, stakes, antes)
                    showAddSessionDialog = false
                },
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

                DashboardScreen(
                    casinoFolders = casinoFolders,
                    onCasinoClick = { casinoName ->
                        navController.navigate(Screen.CasinoSessions.createRoute(casinoName))
                    },
                    casinoData = casinoData,
                    onAddSession = { name, casinoName, sessionType, game, gameType, stakes, antes ->
                        viewModel.addSession(name, casinoName, sessionType, game, gameType, stakes, antes)
                    }
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
                val casinoData by viewModel.casinoData.collectAsState()
                SessionsScreen(
                    sessions = sessions,
                    onAddSession = { name, casinoName, date, type, game, gameType, stakes, antes, buyIn, cashOut ->
                        viewModel.addSession(name, casinoName, date, type, game, gameType, stakes, antes, buyIn, cashOut)
                    },
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                    },
                    sessionsViewModel = viewModel
                )
            }
            composable(Screen.Scan.route) {
                ScanScreen(
                    onOpenCamera = {
                        // Will implement camera functionality later
                    }
                )
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(
                route = Screen.CasinoSessions.route,
                arguments = listOf(navArgument("casinoName") { type = NavType.StringType })
            ) { backStackEntry ->
                val casinoName = backStackEntry.arguments?.getString("casinoName") ?: ""
                val viewModel: CasinoSessionsViewModel = viewModel(
                    factory = CasinoSessionsViewModelFactory(
                        AppDatabase.getDatabase(application).staxDao(),
                        casinoName
                    )
                )
                val sessions by viewModel.sessions.collectAsState()

                CasinoSessionsScreen(
                    casinoName = casinoName,
                    sessions = sessions,
                    onSessionClick = { sessionId ->
                        navController.navigate(Screen.PhotoGallery.createRoute(sessionId))
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
                    onNavigateBack = { navController.popBackStack() }
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
                val photos = Gson().fromJson(photosJson.value, Array<Photo>::class.java).toList()
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

                    if (livePhotos.isNotEmpty()) {
                        FullScreenImageViewer(
                            photos = livePhotos,
                            initialPhotoIndex = photoIndex,
                            onNavigateUp = { navController.navigateUp() },
                            onRatingChanged = { ratedPhoto, rating ->
                                viewModel.updatePhoto(ratedPhoto.copy(rating = rating))
                            }
                        )
                    }
                }
            }
        }
    }
} 