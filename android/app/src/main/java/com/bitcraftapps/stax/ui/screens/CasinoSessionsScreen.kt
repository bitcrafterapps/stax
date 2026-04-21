package com.bitcraftapps.stax.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.res.painterResource
import com.bitcraftapps.stax.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bitcraftapps.stax.data.SessionWithLatestPhoto
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
import java.io.File
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasinoSessionsScreen(
    casinoName: String,
    sessions: List<SessionWithLatestPhoto>,
    onSessionClick: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    logoResName: String? = null
) {
    val context = LocalContext.current
    var longPressedSessionId by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val logoBitmap = remember(logoResName) {
        if (logoResName != null) {
            try {
                val id = context.resources.getIdentifier(logoResName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap() else null
            } catch (_: Exception) { null }
        } else null
    }

    if (showDeleteDialog && longPressedSessionId != null) {
        ConfirmDeleteDialog(
            onConfirm = {
                onDeleteSession(longPressedSessionId!!)
                showDeleteDialog = false
                longPressedSessionId = null
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (logoBitmap != null) {
                                Image(
                                    bitmap = logoBitmap,
                                    contentDescription = "$casinoName logo",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(7.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                casinoName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(sessions) { session ->
                SessionFolder(
                    sessionWithLatest = session,
                    onClick = {
                        if (longPressedSessionId == null) {
                            onSessionClick(session.session.id)
                        } else {
                            longPressedSessionId = null
                        }
                    },
                    onLongClick = {
                        longPressedSessionId = session.session.id
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    },
                    isSelected = longPressedSessionId == session.session.id
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionFolder(
    sessionWithLatest: SessionWithLatestPhoto,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isSelected: Boolean
) {
    val profitLoss = sessionWithLatest.session.cashOutAmount - sessionWithLatest.session.buyInAmount
    val profitLossColor = if (profitLoss >= 0) Color(0xFF7CFF8A) else Color(0xFFFF8A80)
    val profitLossPrefix = if (profitLoss >= 0) "+" else ""
    val currency = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (sessionWithLatest.latestPhotoPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(sessionWithLatest.latestPhotoPath)),
                    contentDescription = "Session Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.25f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.35f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = sessionWithLatest.session.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildString {
                        if (sessionWithLatest.session.game.isNotBlank()) append("${sessionWithLatest.session.game} · ")
                        append("${sessionWithLatest.photoCount} ${if (sessionWithLatest.photoCount == 1) "photo" else "photos"}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            // Game type badge — top-start corner
            val typeIconRes = if (sessionWithLatest.session.type == "Cash") R.drawable.cash else R.drawable.stack
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(84.dp)
                    .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = typeIconRes),
                    contentDescription = sessionWithLatest.session.type,
                    modifier = Modifier.size(54.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "$profitLossPrefix${currency.format(profitLoss)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = profitLossColor
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Session",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 46.dp, end = 8.dp)
                        .clickable(onClick = onDeleteClick)
                )
            }
        }
    }
} 