package com.bitcraftapps.stax.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitcraftapps.stax.R
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.data.billing.SubscriptionState
import com.bitcraftapps.stax.ui.theme.StaxPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin

@Composable
fun AboutScreen(
    onNavigateToChipConfiguration: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToNutzGame: () -> Unit,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var stackedChips by remember { mutableStateOf(0) }
    val entitlementManager = LocalEntitlementManager.current
    val subscriptionState by entitlementManager.subscriptionState.collectAsState()
    var stackRunId by remember { mutableStateOf(0) }
    val versionName = remember {
        try {
            val pm = context.packageManager
            val pkg = context.packageName
            val vn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0)).versionName
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0).versionName
            }
            vn ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(292.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            for (index in stackedChips downTo 1) {
                val visible by animateFloatAsState(
                    targetValue = if (index <= stackedChips) 1f else 0f,
                    animationSpec = tween(durationMillis = 120),
                    label = "chipStackAlpha$index"
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_stax_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .offset(y = (-18 * index).dp)
                        .alpha(visible)
                        .clip(CircleShape)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "App logo",
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .graphicsLayer {
                        shape = CircleShape
                        clip = true
                    }
                    .clickable {
                        stackRunId += 1
                        val runId = stackRunId
                        stackedChips = 0
                        ChipClickSoundPlayer.play()
                        scope.launch {
                            repeat(9) { index ->
                                delay(55)
                                if (runId != stackRunId) return@launch
                                stackedChips = index + 1
                            }
                            delay(550)
                            if (runId == stackRunId) {
                                stackedChips = 0
                            }
                        }
                    }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Stack it. Snap it. Track it.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Version $versionName",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        // STAX Premium section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "STAX Premium",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                when (val state = subscriptionState) {
                    is SubscriptionState.Free -> {
                        Text(
                            "Free Plan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onNavigateToPaywall,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StaxPrimary)
                        ) {
                            Text("Upgrade to Premium")
                        }
                    }
                    is SubscriptionState.Premium -> {
                        if (state.isInTrial) {
                            val daysLeft = entitlementManager.getTrialDaysRemaining()
                            Text(
                                "Trial — $daysLeft days remaining",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = onNavigateToPaywall,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = StaxPrimary)
                            ) {
                                Text("Upgrade Now")
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = StaxPrimary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        "Premium ✓",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StaxPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=com.bitcraftapps.stax")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Subscription")
                            }
                        }
                    }
                    is SubscriptionState.Expired -> {
                        Text(
                            "Subscription Expired",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = onNavigateToPaywall,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StaxPrimary)
                        ) {
                            Text("Resubscribe")
                        }
                    }
                }

                // Debug toggle (temporary — remove once gates verified)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Debug: Toggle Premium",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val isPremiumNow = subscriptionState is SubscriptionState.Premium
                    Switch(
                        checked = isPremiumNow,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                entitlementManager.setPremium(isInTrial = false, expiryMs = Long.MAX_VALUE)
                            } else {
                                entitlementManager.setFree()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("OpenAI settings")
            }
            Button(
                onClick = onNavigateToChipConfiguration,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chip configuration")
            }
            Button(
                onClick = onNavigateToReports,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reports")
            }
            Button(
                onClick = onNavigateToNutzGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nutz Game")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "API keys are stored on this device only. Add a key in settings to use cloud chip estimation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "When cloud estimation is enabled, scan photos are sent directly to OpenAI using your key and are not routed through a Stax server.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            onSave = { apiKey ->
                saveApiKey(context, apiKey)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(getApiKey(context) ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "OpenAI",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    "Add your API key to enable cloud-based chip totals from the Scan screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Secret key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(apiKey.trim()) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getApiKey(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("openai_api_key", null)
}

private fun saveApiKey(context: Context, apiKey: String) {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putString("openai_api_key", apiKey)
        apply()
    }
}

private object ChipClickSoundPlayer {
    private const val SAMPLE_RATE = 22050
    private const val DURATION_MS = 210L
    private val soundBytes: ByteArray by lazy { synthesizeChipClicks() }

    fun play() {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(max(soundBytes.size, AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        track.play()
        Thread {
            try {
                track.write(soundBytes, 0, soundBytes.size)
                Thread.sleep(DURATION_MS + 80)
            } finally {
                track.stop()
                track.release()
            }
        }.start()
    }

    private fun synthesizeChipClicks(): ByteArray {
        val totalSamples = SAMPLE_RATE * DURATION_MS.toInt() / 1000
        val data = ByteArray(totalSamples * 2)
        val clickStarts = listOf(0.0, 0.045, 0.09, 0.135)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE.toDouble()
            var sampleValue = 0.0

            clickStarts.forEachIndexed { index, start ->
                val dt = t - start
                if (dt >= 0.0 && dt <= 0.026) {
                    val env = exp(-dt * 118.0)
                    val attack = exp(-dt * 420.0)
                    val noise = pseudoNoise(i + (index * 4099))
                    val knock = 0.55 * sin(2.0 * PI * 540.0 * dt)
                    val body = 0.26 * sin(2.0 * PI * 920.0 * dt)
                    val tick = 0.08 * sin(2.0 * PI * 1750.0 * dt)
                    val secondTapDelay = (dt - 0.004).coerceAtLeast(0.0)
                    val secondTap = if (dt >= 0.004) exp(-secondTapDelay * 520.0) * 0.12 * pseudoNoise(i + 991 * (index + 1)) else 0.0
                    sampleValue += env * (0.88 * noise + knock + body) + attack * tick + secondTap
                }
            }

            val clipped = sampleValue.coerceIn(-1.0, 1.0)
            val pcm = (clipped * Short.MAX_VALUE).toInt().toShort()
            data[i * 2] = (pcm.toInt() and 0xFF).toByte()
            data[i * 2 + 1] = ((pcm.toInt() shr 8) and 0xFF).toByte()
        }

        return data
    }

    private fun pseudoNoise(seed: Int): Double {
        var x = seed.toLong()
        x = (x xor (x shl 13))
        x = (x xor (x shr 17))
        x = (x xor (x shl 5))
        return ((x and 0xFFFF).toDouble() / 32768.0) - 1.0
    }
}
