package com.example.stax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.stax.navigation.AppNavigation
import com.example.stax.ui.theme.StaxAmbientGradient
import com.example.stax.ui.theme.StaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StaxTheme {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.stax.R.drawable.bg_poker_table_final),
                        contentDescription = "App Background",
                        modifier = Modifier.fillMaxSize(),
                        alignment = androidx.compose.ui.Alignment.BottomCenter,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    val photosJson = rememberSaveable { mutableStateOf("") }
                    AppNavigation(photosJson)
                }
            }
        }
    }
}