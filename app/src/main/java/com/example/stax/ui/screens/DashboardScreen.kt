package com.example.stax.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.stax.R
import com.example.stax.data.DashboardViewModel
import com.example.stax.data.SessionWithLatestPhoto
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    sessions: List<SessionWithLatestPhoto>,
    onSessionClick: (Long) -> Unit,
    onDeleteSession: (Long) -> Unit,
    casinoData: Map<String, List<String>>,
    onAddSession: (String, String, String) -> Unit,
) {
    var longPressedSessionId by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && longPressedSessionId != null) {
        ConfirmDeleteDialog(
            onConfirm = {
                onDeleteSession(longPressedSessionId!!)
                showDeleteDialog = false
                longPressedSessionId = null
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }

    if (showAddSessionDialog) {
        AddSessionDialog(
            casinoData = casinoData,
            onConfirm = { casinoName, sessionType, gameType ->
                onAddSession(casinoName, sessionType, gameType)
                showAddSessionDialog = false
            },
            onDismiss = { showAddSessionDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Chip Porn",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.3f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 300f
                        )
                    )
                    .align(Alignment.BottomCenter)
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (sessionWithLatest.session.type == "Cash") Icons.Default.AttachMoney else Icons.Default.Star,
                    contentDescription = "Session Type",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = sessionWithLatest.session.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${sessionWithLatest.session.game} - ${sessionWithLatest.photoCount} photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Session",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clickable(onClick = onDeleteClick)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    casinoData: Map<String, List<String>>,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedState by remember { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var expandedState by remember { mutableStateOf(false) }
    var selectedCasino by remember { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var expandedCasino by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("Cash") }
    var game by remember { mutableStateOf("NLH") }

    if (casinoData.isEmpty()) {
        // Handle empty casino data case if necessary
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add New Session") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // State/Region Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedState,
                    onExpandedChange = { expandedState = !expandedState }
                ) {
                    OutlinedTextField(
                        value = selectedState,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("State/Region") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedState,
                        onDismissRequest = { expandedState = false }
                    ) {
                        casinoData.keys.forEach { state ->
                            DropdownMenuItem(
                                text = { Text(state) },
                                onClick = {
                                    selectedState = state
                                    selectedCasino = casinoData[state]?.firstOrNull() ?: ""
                                    expandedState = false
                                }
                            )
                        }
                    }
                }

                // Casino Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCasino,
                    onExpandedChange = { expandedCasino = !expandedCasino }
                ) {
                    OutlinedTextField(
                        value = selectedCasino,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Casino") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCasino) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCasino,
                        onDismissRequest = { expandedCasino = false }
                    ) {
                        casinoData[selectedState]?.forEach { casinoItem ->
                            DropdownMenuItem(
                                text = { Text(casinoItem) },
                                onClick = {
                                    selectedCasino = casinoItem
                                    expandedCasino = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (e.g., Cash, Tournament)") }
                )
                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("Game (e.g., NLH, PLO)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCasino.isNotBlank()) {
                        onConfirm(selectedCasino, type, game)
                    }
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete") },
        text = { Text("Are you sure you want to delete this item?") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 