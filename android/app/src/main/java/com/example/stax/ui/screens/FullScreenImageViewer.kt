package com.example.stax.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.stax.R
import com.example.stax.data.Photo
import com.example.stax.ui.composables.RatingBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    photos: List<Photo>,
    initialPhotoIndex: Int,
    onNavigateUp: () -> Unit,
    onRatingChanged: (Photo, Int) -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = initialPhotoIndex
    ) {
        photos.size
    }

    // Track zoom state so pager swiping can be disabled while zoomed in
    var isZoomed by remember { mutableStateOf(false) }
    var showSharePanel by remember { mutableStateOf(false) }

    // Reset zoom flag when the page changes
    LaunchedEffect(pagerState.currentPage) {
        isZoomed = false
        showSharePanel = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A0C),
                        Color(0xFF111114),
                        Color(0xFF0A0A0C)
                    )
                )
            )
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val photo = photos[page]
            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                val newScale = (scale * zoomChange).coerceIn(1f, 6f)
                offset = if (newScale > 1f) offset + panChange else Offset.Zero
                scale = newScale
                isZoomed = scale > 1.05f
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                    isZoomed = false
                                } else {
                                    scale = 2.5f
                                    isZoomed = true
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(photo.imagePath)),
                    contentDescription = "Full Screen Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    )
                )
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 12.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Stax Logo",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Stax",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pagerState.pageCount > 1) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            IconButton(onClick = { showSharePanel = !showSharePanel }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White
                )
            }
        }

        if (pagerState.pageCount > 0) {
            val currentPhoto = photos[pagerState.currentPage]

            // Bottom scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )

            // Dismiss overlay — MUST come before the share panel in z-order
            // so the panel renders on top and its buttons remain tappable
            if (showSharePanel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showSharePanel = false }
                )
            }

            // Share panel — slides up from bottom (drawn above the dismiss overlay)
            AnimatedVisibility(
                visible = showSharePanel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color(0xFF1A1A1E).copy(alpha = 0.97f),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Share to",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ShareTarget(
                                iconRes = R.drawable.ic_instagram,
                                label = "Instagram",
                                onClick = {
                                    shareImageToApp(context, currentPhoto.imagePath, "com.instagram.android")
                                    showSharePanel = false
                                }
                            )
                            ShareTarget(
                                iconRes = R.drawable.ic_facebook,
                                label = "Facebook",
                                onClick = {
                                    shareImageToApp(context, currentPhoto.imagePath, "com.facebook.katana")
                                    showSharePanel = false
                                }
                            )
                            ShareTarget(
                                iconRes = R.drawable.ic_x,
                                label = "X",
                                onClick = {
                                    shareImageToApp(context, currentPhoto.imagePath, "com.twitter.android")
                                    showSharePanel = false
                                }
                            )
                            ShareTarget(
                                iconRes = android.R.drawable.ic_menu_share,
                                label = "More",
                                onClick = {
                                    shareImageSystem(context, currentPhoto.imagePath)
                                    showSharePanel = false
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Rating bar — hide when share panel is open
            if (!showSharePanel) {
                RatingBar(
                    rating = currentPhoto.rating,
                    onRatingChanged = { newRating ->
                        onRatingChanged(currentPhoto, newRating)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                )
            }
        }

    }
}

@Composable
private fun ShareTarget(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}

private fun getImageContentUri(context: Context, imagePath: String) =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        File(imagePath)
    )

private fun shareImageToApp(context: Context, imagePath: String, packageName: String) {
    val uri = getImageContentUri(context, imagePath)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Tracked with STAX 🃏")
        setPackage(packageName)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // App not installed — fall back to system chooser
        shareImageSystem(context, imagePath)
    }
}

private fun shareImageSystem(context: Context, imagePath: String) {
    val uri = getImageContentUri(context, imagePath)
    val intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Tracked with STAX 🃏")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        },
        "Share photo"
    )
    context.startActivity(intent)
} 