package com.bitcraftapps.stax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.bitcraftapps.stax.navigation.AppNavigation
import com.bitcraftapps.stax.ui.theme.StaxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StaxTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.bg_poker_table_final),
                        contentDescription = "App Background",
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.BottomCenter,
                        contentScale = ContentScale.Crop
                    )
                    val photosJson = rememberSaveable { mutableStateOf("") }
                    AppNavigation(photosJson)
                }
            }
        }
    }
}