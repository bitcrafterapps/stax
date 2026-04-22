package com.bitcraftapps.stax.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Guide", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HelpSection(
                emoji = "📸",
                title = "Photos (Home)",
                items = listOf(
                    "Tap the **+** button to create a new poker session.",
                    "Sessions are grouped by casino or venue in a photo-grid view.",
                    "Tap any casino tile to browse sessions and photos from that venue.",
                    "Each session stores its buy-in, cash-out, and all chip-stack photos.",
                    "Free accounts can track up to **3 sessions**. Upgrade for unlimited."
                )
            )

            HelpSection(
                emoji = "📋",
                title = "Sessions",
                items = listOf(
                    "View all sessions in a list, grouped by casino.",
                    "Filter between Cash, Tournament, or All session types.",
                    "See overall profit/loss, total buy-in, and cash-out at a glance.",
                    "Tap any casino group to drill into individual session details.",
                    "Tap the **+** button to add a new session from this screen.",
                    "Session dates, stakes, antes, and game type are stored for each entry."
                )
            )

            HelpSection(
                emoji = "🔍",
                title = "Find Card Rooms",
                items = listOf(
                    "Search for poker rooms and casinos near your current location.",
                    "**Near Me** mode uses GPS to find rooms within a chosen radius.",
                    "Collapse or expand the radius selector to save screen space.",
                    "Use the **preset chips** (100 mi, 500 mi, 1000 mi, 1000+) for quick jumps, or drag the fine-control slider for 10–100 mile precision.",
                    "**By State** mode lists all rooms in a chosen US state.",
                    "**Favorites** shows rooms you've saved as favorites.",
                    "Tap the ♥ icon on any room's detail page to save it as a favorite.",
                    "Set a **Home Casino** (🏠) so it always floats to the top of your lists.",
                    "Tap **Directions** to open Google Maps navigation to any room.",
                    "Free accounts can save up to **3 favorites**. Upgrade for unlimited."
                )
            )

            HelpSection(
                emoji = "📷",
                title = "Chip-Stack Photos",
                items = listOf(
                    "From any session's photo gallery, tap the camera icon to take a new chip-stack photo.",
                    "Tap the gallery icon to import an existing photo from your device.",
                    "Swipe through your photos in full-screen view.",
                    "Long-press or tap the trash icon on a photo to delete it.",
                    "Add captions to photos in the full-screen viewer.",
                    "Rate photos with a star rating for easy reference later.",
                    "Free accounts can add up to **10 photos per session**. Upgrade for unlimited."
                )
            )

            HelpSection(
                emoji = "🤖",
                title = "Chip Scanning (Premium)",
                items = listOf(
                    "Chip Scanning is a **STAX Premium** feature.",
                    "Point your camera at a chip stack and tap **Scan** to count the total value instantly.",
                    "On-device AI (offline) uses your configured chip colors and values to estimate the stack.",
                    "**Cloud estimate** sends the camera frame to OpenAI using your saved API key for a second opinion.",
                    "Add your OpenAI API key under **About → OpenAI settings**.",
                    "Switch between **Cash** and **Tournament** modes to use the correct chip denominations.",
                    "Use **Train** mode to capture labelled chip photos and improve on-device accuracy over time.",
                    "Chip scanning works fully offline — only the OpenAI cloud option requires internet."
                )
            )

            HelpSection(
                emoji = "🎰",
                title = "Chip Configuration",
                items = listOf(
                    "Access via **About → Chip configuration**.",
                    "Configure the color and value of each chip denomination for any casino.",
                    "Separate configurations for **Cash** and **Tournament** chip sets.",
                    "The on-device AI scanner uses these configurations to calculate stack totals.",
                    "Free accounts can configure chips for **1 casino**. Upgrade to configure all."
                )
            )

            HelpSection(
                emoji = "📊",
                title = "Reports",
                items = listOf(
                    "Access via **About → Reports**.",
                    "View aggregate stats across all your sessions: total sessions, total buy-in, net profit/loss.",
                    "Break down results by session type (Cash vs Tournament).",
                    "See your best and worst sessions at a glance.",
                    "Filter by date range to analyse specific periods of play."
                )
            )

            HelpSection(
                emoji = "🏠",
                title = "Home Games",
                items = listOf(
                    "When creating a session, switch to **Home Game** mode to log a private game.",
                    "Enter a name, city, and state for the home game venue.",
                    "Home games you've entered are saved and appear in a **Saved Home Games** dropdown for future sessions.",
                    "Home game sessions appear alongside casino sessions in all views."
                )
            )

            HelpSection(
                emoji = "🎮",
                title = "Nutz Game",
                items = listOf(
                    "Access via **About → Nutz Game**.",
                    "A quick heads-up no-limit hold'em mini-game you can play on the go.",
                    "Practice poker decisions and hand reading between real sessions."
                )
            )

            HelpSection(
                emoji = "⭐",
                title = "STAX Premium",
                items = listOf(
                    "Upgrade to Premium from **About** or any feature gate prompt.",
                    "**Unlimited sessions** — no 3-session cap.",
                    "**Unlimited photos** per session — no 10-photo cap.",
                    "**Unlimited favorites** — no 3-favorite cap.",
                    "**Full chip configuration** — configure chips for every casino.",
                    "**Chip scanning** — on-device AI + optional OpenAI cloud estimation.",
                    "A free trial is available when you subscribe for the first time.",
                    "Manage or cancel your subscription at any time from the Google Play Store."
                )
            )

            HelpSection(
                emoji = "🔑",
                title = "OpenAI Settings",
                items = listOf(
                    "Access via **About → OpenAI settings**.",
                    "Enter your OpenAI API key to enable cloud chip estimation in the Scan screen.",
                    "Your key is stored **only on this device** using encrypted storage — it is never sent to Stax servers.",
                    "When cloud estimation is enabled, the scan image is sent directly from your device to OpenAI.",
                    "You can get an API key at platform.openai.com."
                )
            )

            HelpSection(
                emoji = "💡",
                title = "Tips & Tricks",
                items = listOf(
                    "Tap the **STAX logo** on the About screen for a surprise.",
                    "The state/region dropdown in new-session dialogs auto-detects your location.",
                    "Long-press a session in the Sessions list to access quick-delete.",
                    "The Find screen collapses the radius picker by default — tap it to expand.",
                    "Session names are auto-filled from the venue name and date, but you can customise them."
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HelpSection(
    emoji: String,
    title: String,
    items: List<String>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(emoji, style = MaterialTheme.typography.titleLarge)
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            items.forEachIndexed { idx, item ->
                HelpBullet(text = item)
                if (idx < items.lastIndex) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun HelpBullet(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        val annotated = buildAnnotatedString {
            val parts = text.split("**")
            parts.forEachIndexed { idx, part ->
                if (idx % 2 == 1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(part) }
                } else {
                    append(part)
                }
            }
        }
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
