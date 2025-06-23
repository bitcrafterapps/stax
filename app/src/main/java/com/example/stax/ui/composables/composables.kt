package com.example.stax.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RatingBar(
    modifier: Modifier = Modifier,
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    maxRating: Int = 5,
    starSize: Dp = 24.dp
) {
    Row(modifier = modifier) {
        for (i in 1..maxRating) {
            val iconModifier = Modifier.size(starSize)
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color.Yellow else Color.Gray,
                modifier = if (onRatingChanged != null) {
                    iconModifier.clickable { onRatingChanged(i) }
                } else {
                    iconModifier
                }
            )
        }
    }
} 