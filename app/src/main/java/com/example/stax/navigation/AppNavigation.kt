package com.example.stax.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stax.data.AppDatabase
import com.example.stax.data.DashboardViewModel
import com.example.stax.data.Photo
import com.example.stax.data.PhotoGalleryViewModel
import com.example.stax.ui.screens.AddSessionDialog
import com.example.stax.ui.screens.DashboardScreen
import com.example.stax.ui.screens.FullScreenImageViewer
import com.example.stax.ui.screens.PhotoGalleryScreen
import com.example.stax.ui.screens.SplashScreen
import com.google.gson.Gson

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
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

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Dashboard.route) {
            val viewModel: DashboardViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DashboardViewModel(AppDatabase.getDatabase(application).staxDao()) as T
                    }
                }
            )
            val sessions by viewModel.sessions.collectAsState()

            DashboardScreen(
                sessions = sessions,
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.PhotoGallery.createRoute(sessionId))
                },
                onAddSession = { showAddSessionDialog = true },
                onDeleteSession = { sessionId ->
                    viewModel.deleteSession(sessionId)
                }
            )

            if (showAddSessionDialog) {
                AddSessionDialog(
                    onConfirm = { casinoName, sessionType, gameType ->
                        viewModel.addSession(casinoName, sessionType, gameType)
                        showAddSessionDialog = false
                    },
                    onDismiss = { showAddSessionDialog = false }
                )
            }
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
            photosJson.value = Gson().toJson(photos)

            PhotoGalleryScreen(
                photos = photos,
                onNavigateUp = { navController.navigateUp() },
                onAddPhoto = { uri -> viewModel.addPhoto(uri) },
                onDeletePhoto = { photo -> viewModel.deletePhoto(photo) },
                onPhotoClick = { photo ->
                    val photoIndex = photos.indexOf(photo)
                    navController.navigate(Screen.FullScreenImageViewer.createRoute(photoIndex))
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

                FullScreenImageViewer(
                    photo = photo,
                    onNavigateUp = { navController.navigateUp() },
                    onRatingChanged = { rating ->
                        viewModel.updatePhoto(photo.copy(rating = rating))
                    }
                )
            }
        }
    }
} 