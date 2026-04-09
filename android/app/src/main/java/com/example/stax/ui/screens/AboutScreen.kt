package com.example.stax.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stax.R

@Composable
fun AboutScreen(
    onNavigateToChipConfiguration: () -> Unit
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
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
        Image(
            painter = painterResource(id = R.drawable.ic_stax_logo),
            contentDescription = "App logo",
            modifier = Modifier.size(200.dp)
        )
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
        Spacer(modifier = Modifier.height(32.dp))
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
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "API keys are stored on this device only. Add a key in settings to use cloud chip estimation.",
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
