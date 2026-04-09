package com.example.stax.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.stax.R
import com.example.stax.ui.theme.StaxHeaderGradient
import com.example.stax.data.CasinoFolder
import com.example.stax.ui.composables.DropdownSelector
import com.example.stax.ui.composables.StaxEmptyState
import com.example.stax.ui.composables.StaxScreenHeader
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    casinoFolders: List<CasinoFolder>,
    onCasinoClick: (String) -> Unit,
    casinoData: Map<String, List<String>>,
    onAddSession: (String, String, String, String, String, String, String) -> Unit,
) {
    var showAddSessionDialog by remember { mutableStateOf(false) }

    if (showAddSessionDialog) {
        AddSessionDialog(
            casinoData = casinoData,
            onConfirm = { name, casinoName, sessionType, game, gameType, stakes, antes ->
                onAddSession(name, casinoName, sessionType, game, gameType, stakes, antes)
                showAddSessionDialog = false
            },
            onDismiss = { showAddSessionDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(StaxHeaderGradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StaxScreenHeader(
                title = "Casino/Card Rooms",
                subtitle = "Browse sessions by venue"
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (casinoFolders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                StaxEmptyState(
                    title = "No sessions yet",
                    message = "Tap the + button to create your first session and start building your gallery."
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(start = 3.dp, end = 3.dp, top = 2.dp, bottom = 80.dp)
            ) {
                items(casinoFolders) { casinoFolder ->
                    CasinoFolderItem(
                        casinoFolder = casinoFolder,
                        onClick = { onCasinoClick(casinoFolder.casinoName) }
                    )
                }
            }
        }
    }
}

@Composable
fun CasinoFolderItem(
    casinoFolder: CasinoFolder,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (casinoFolder.latestPhotoPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = File(casinoFolder.latestPhotoPath)),
                    contentDescription = "Casino Photo",
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
                    tint = Color.White.copy(alpha = 0.3f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 200f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = casinoFolder.casinoName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${casinoFolder.sessionCount} sessions",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    casinoData: Map<String, List<String>>,
    onConfirm: (String, String, String, String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var sessionName by remember { mutableStateOf("") }
    var selectedState by remember(casinoData) { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var expandedState by remember { mutableStateOf(false) }
    var selectedCasino by remember(selectedState) { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var expandedCasino by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("Cash") }
    var game by remember { mutableStateOf("") }
    var gameType by remember { mutableStateOf("NLH") }
    var stakes by remember { mutableStateOf("1/2") }
    var antes by remember { mutableStateOf("None") }

    val gameTypes = listOf("NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi")
    val stakesList = listOf("1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000")
    val antesList = listOf("None", "10", "20", "40", "50", "100", "200", "400", "1000")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New session",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = sessionName,
                    onValueChange = { sessionName = it },
                    label = { Text("Session Name") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
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
                        value = selectedCasino ?: "",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == "Cash",
                        onClick = { type = "Cash" },
                        label = { Text("Cash") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == "Tourney",
                        onClick = { type = "Tourney" },
                        label = { Text("Tourney") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it },
                    label = { Text("Game") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
                DropdownSelector(label = "Game Type", options = gameTypes, selectedOption = gameType, onOptionSelected = { gameType = it })
                if (type == "Cash") {
                    DropdownSelector(label = "Stakes", options = stakesList, selectedOption = stakes, onOptionSelected = { stakes = it })
                    DropdownSelector(label = "Antes", options = antesList, selectedOption = antes, onOptionSelected = { antes = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCasino.isNotBlank()) {
                        onConfirm(sessionName, selectedCasino, type, game, gameType, stakes, antes)
                    }
                },
                enabled = selectedCasino.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Deletion") },
        text = { Text("Are you sure you want to delete this session and all its photos?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 