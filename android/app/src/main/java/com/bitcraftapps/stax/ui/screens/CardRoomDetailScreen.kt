package com.bitcraftapps.stax.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import com.bitcraftapps.stax.R
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitcraftapps.stax.data.CardRoomWithDistance
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient

private val HomePurple      = Color(0xFF7B1FA2)
private val HomePurpleLight = Color(0xFFCE93D8)
private val HomePurpleBg    = Color(0xFF6A1B9A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardRoomDetailScreen(
    item: CardRoomWithDistance,
    isFavorite: Boolean,
    isHomeCasino: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val room = item.room

    val logoBitmap = remember(room.logo) {
        if (room.logo != null) {
            try {
                val resName = "logo_" + room.logo.removeSuffix(".png").replace('-', '_')
                val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap()
                else null
            } catch (_: Exception) { null }
        } else null
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = {
                        Text(
                            room.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Hero card ──────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (logoBitmap != null) {
                        Image(
                            bitmap = logoBitmap,
                            contentDescription = "${room.name} logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_stax_logo),
                            contentDescription = "Stax logo",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                    }

                    Text(
                        text = room.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${room.city}, ${room.state}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isHomeCasino) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HomePurple.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = "HOME CASINO",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HomePurpleLight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // ── Info card ──────────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    DetailInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Address",
                        value = room.address
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DetailInfoRow(
                        icon = Icons.Default.Place,
                        label = "City / State",
                        value = "${room.city}, ${room.state}"
                    )
                    if (item.distanceMiles != null && item.distanceMiles > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        DetailInfoRow(
                            icon = Icons.Default.NearMe,
                            label = "Distance",
                            value = formatDetailDistance(item.distanceMiles)
                        )
                    }
                }
            }

            // ── Directions button ──────────────────────────────────────────
            Button(
                onClick = { openCardRoomDirections(context, room.latitude, room.longitude) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Directions", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }

            // ── Favorite + Home row ────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onToggleFavorite,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFavorite) MaterialTheme.colorScheme.error
                                         else MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                        contentColor = if (isFavorite) Color.White
                                       else MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isFavorite) "Favorited" else "Favorite", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onToggleHome,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHomeCasino) HomePurpleBg
                                         else HomePurple.copy(alpha = 0.22f),
                        contentColor = if (isHomeCasino) Color.White else HomePurpleLight
                    )
                ) {
                    Icon(
                        if (isHomeCasino) Icons.Filled.Home else Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isHomeCasino) "Home Casino" else "Set as Home", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDetailDistance(miles: Double): String =
    when {
        miles < 0.1 -> "Nearby"
        miles < 10  -> String.format("%.1f miles away", miles)
        else        -> String.format("%.0f miles away", miles)
    }

private fun openCardRoomDirections(context: Context, latitude: Double, longitude: Double) {
    val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}
