package com.example.stax.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.stax.data.Photo
import com.example.stax.ui.composables.RatingBar
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(
    photos: List<Photo>,
    initialPhotoIndex: Int,
    onNavigateUp: () -> Unit,
    onRatingChanged: (Photo, Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialPhotoIndex,
        initialPageOffsetFraction = 0f
    ) {
        photos.size
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val photo = photos[page]
                Image(
                    painter = rememberAsyncImagePainter(model = File(photo.imagePath)),
                    contentDescription = "Full Screen Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            if (pagerState.pageCount > 0) {
                val currentPhoto = photos[pagerState.currentPage]
                RatingBar(
                    rating = currentPhoto.rating,
                    onRatingChanged = { newRating ->
                        onRatingChanged(currentPhoto, newRating)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
} 