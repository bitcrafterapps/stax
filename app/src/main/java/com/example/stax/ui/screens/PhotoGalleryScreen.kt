package com.example.stax.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.stax.data.Photo
import com.example.stax.data.Session
import com.example.stax.ui.composables.RatingBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoGalleryScreen(
    session: Session?,
    photos: List<Photo>,
    onNavigateUp: () -> Unit,
    onAddPhoto: (Uri) -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    onPhotoClick: (Photo) -> Unit
) {
    val context = LocalContext.current
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<@JvmSuppressWildcards Uri> ->
            uris.forEach(onAddPhoto)
        }
    )

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                imageUri?.let { onAddPhoto(it) }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session?.name ?: "Gallery", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { multiplePhotoPickerLauncher.launch("image/*") }) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Open Gallery",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        val newImageUri = context.createImageFile().let {
                            FileProvider.getUriForFile(context, "${context.packageName}.provider", it)
                        }
                        imageUri = newImageUri
                        cameraLauncher.launch(newImageUri)
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = "Add Photo")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No photos yet. Add one!",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(innerPadding)
            ) {
                items(photos) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(0.75f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        photoToDelete = photo
                                    },
                                    onTap = {
                                        onPhotoClick(photo)
                                    }
                                )
                            }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = File(photo.imagePath)),
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.8f)
                                        )
                                    )
                                )
                        )
                        RatingBar(
                            rating = photo.rating,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            starSize = 16.dp
                        )
                        if (photoToDelete == photo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                IconButton(onClick = {
                                    onDeletePhoto(photo)
                                    photoToDelete = null
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete Photo",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (photoToDelete != null) {
            AlertDialog(
                onDismissRequest = { photoToDelete = null },
                title = { Text("Delete Photo") },
                text = { Text("Are you sure you want to delete this photo?") },
                confirmButton = {
                    Button(onClick = {
                        photoToDelete?.let { onDeletePhoto(it) }
                        photoToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(onClick = { photoToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
} 