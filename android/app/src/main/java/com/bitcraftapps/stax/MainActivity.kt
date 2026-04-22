package com.bitcraftapps.stax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.bitcraftapps.stax.data.billing.BillingRepository
import com.bitcraftapps.stax.data.billing.EntitlementManager
import com.bitcraftapps.stax.data.billing.LocalBillingRepository
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.navigation.AppNavigation
import com.bitcraftapps.stax.ui.theme.StaxTheme

class MainActivity : ComponentActivity() {

    private lateinit var entitlementManager: EntitlementManager
    private lateinit var billingRepository: BillingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        entitlementManager = EntitlementManager(applicationContext)
        billingRepository = BillingRepository(applicationContext, entitlementManager)
        billingRepository.startConnection()

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
                    CompositionLocalProvider(
                        LocalEntitlementManager provides entitlementManager,
                        LocalBillingRepository provides billingRepository
                    ) {
                        AppNavigation(photosJson)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        billingRepository.queryExistingPurchases()
    }

    override fun onDestroy() {
        super.onDestroy()
        billingRepository.endConnection()
    }
}
