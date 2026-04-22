package com.bitcraftapps.stax.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
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
import com.bitcraftapps.stax.data.Photo
import com.bitcraftapps.stax.data.Session
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.data.billing.LimitResult
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.ui.composables.RatingBar
import com.bitcraftapps.stax.ui.composables.UpgradeDialog
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
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
    onNavigateToCamera: () -> Unit,
    onNavigateToSessionDetail: () -> Unit,
    onAddPhotoFromGallery: (Uri) -> Unit,
    onDeletePhoto: (Photo) -> Unit,
    onPhotoClick: (Photo) -> Unit,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val entitlementManager = LocalEntitlementManager.current
    var photoToDelete by remember { mutableStateOf<Photo?>(null) }
    var showPhotoLimitDialog by remember { mutableStateOf(false) }
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<@JvmSuppressWildcards Uri> ->
            uris.forEach(onAddPhotoFromGallery)
        }
    )
    val localPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = LocalMultipleContents(),
        onResult = { uris: List<@JvmSuppressWildcards Uri> ->
            uris.forEach(onAddPhotoFromGallery)
        }
    )

    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val cameraPermissionState = rememberPermissionState(
        permission = Manifest.permission.CAMERA
    )

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = {
                        Text(
                            text = session?.name ?: "Gallery",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
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
                        IconButton(onClick = {
                            val limitResult = entitlementManager.checkLimit(
                                Feature.PHOTO_ADD,
                                sessionPhotoCount = photos.size
                            )
                            if (limitResult is LimitResult.Blocked) {
                                showPhotoLimitDialog = true
                            } else if (cameraPermissionState.status.isGranted) {
                                onNavigateToCamera()
                            } else {
                                cameraPermissionState.launchPermissionRequest()
                            }
                        }) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Add Photo",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            val limitResult = entitlementManager.checkLimit(
                                Feature.PHOTO_ADD,
                                sessionPhotoCount = photos.size
                            )
                            if (limitResult is LimitResult.Blocked) {
                                showPhotoLimitDialog = true
                            } else {
                                showPhotoSourceDialog = true
                            }
                        }) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = "Open Gallery",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onNavigateToSessionDetail) {
                            Icon(
                                Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "Session Details",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {},
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (photos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 40.dp),
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
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(photos) { photo ->
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                    .height(80.dp)
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
                                    .padding(10.dp),
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

        if (showPhotoSourceDialog) {
            Dialog(onDismissRequest = { showPhotoSourceDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add Photos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Choose a photo source",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PhotoSourceOption(
                            icon = Icons.Default.PhoneAndroid,
                            title = "Local Device",
                            subtitle = "Pick from photos stored on this phone",
                            onClick = {
                                showPhotoSourceDialog = false
                                localPhotoPickerLauncher.launch("image/*")
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        PhotoSourceOption(
                            icon = Icons.Default.CloudDownload,
                            title = "Cloud / Google Photos",
                            subtitle = "Pick from Google Photos or other cloud sources",
                            onClick = {
                                showPhotoSourceDialog = false
                                multiplePhotoPickerLauncher.launch("image/*")
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showPhotoSourceDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    if (showPhotoLimitDialog) {
        UpgradeDialog(
            feature = Feature.PHOTO_ADD,
            onUpgrade = {
                showPhotoLimitDialog = false
                onNavigateToPaywall()
            },
            onDismiss = { showPhotoLimitDialog = false }
        )
    }
}

@Composable
private fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private class LocalMultipleContents : ActivityResultContract<String, List<@JvmSuppressWildcards Uri>>() {
    override fun createIntent(context: Context, input: String): Intent =
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = input
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (resultCode != Activity.RESULT_OK || intent == null) return emptyList()
        val clip = intent.clipData
        return if (clip != null) {
            (0 until clip.itemCount).map { clip.getItemAt(it).uri }
        } else {
            listOfNotNull(intent.data)
        }
    }
} 