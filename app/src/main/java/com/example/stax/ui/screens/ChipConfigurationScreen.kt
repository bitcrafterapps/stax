package com.example.stax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.stax.data.ChipConfig
import com.example.stax.data.ChipConfigRepository
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipConfigurationScreen(
    casinoData: Map<String, List<String>>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { ChipConfigRepository(context) }
    var selectedState by remember(casinoData) { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var expandedState by remember { mutableStateOf(false) }
    var selectedCasino by remember(selectedState) { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var expandedCasino by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("Cash") }
    var showChipConfigDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chip Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    modifier = Modifier.fillMaxWidth().menuAnchor()
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
                    modifier = Modifier.fillMaxWidth().menuAnchor()
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { type = "Cash" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "Cash") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("Cash") }
                Button(
                    onClick = { type = "Tourney" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "Tourney") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) { Text("Tourney") }
                IconButton(onClick = { showChipConfigDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Chip Values")
                }
            }
        }
    }

    if (showChipConfigDialog) {
        val casinoName = selectedCasino ?: "Default"
        ChipConfigDialog(
            casinoName = casinoName,
            gameType = type,
            initialConfigs = repository.loadChipConfigs(casinoName, type),
            onDismiss = { showChipConfigDialog = false },
            onSave = { updatedConfigs ->
                repository.saveChipConfigs(casinoName, type, updatedConfigs)
                showChipConfigDialog = false
            }
        )
    }
}

@Composable
fun ChipConfigDialog(
    casinoName: String,
    gameType: String,
    initialConfigs: List<ChipConfig>,
    onDismiss: () -> Unit,
    onSave: (List<ChipConfig>) -> Unit
) {
    val chipConfigs = remember { mutableStateListOf(*initialConfigs.toTypedArray()) }
    var showColorPicker by remember { mutableStateOf(false) }
    var chipIndexToEdit by remember { mutableStateOf(-1) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(casinoName, style = MaterialTheme.typography.headlineSmall)
                Text(gameType, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    itemsIndexed(chipConfigs) { index, chip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(chip.color)
                                    .clickable {
                                        chipIndexToEdit = index
                                        showColorPicker = true
                                    }
                            )
                            OutlinedTextField(
                                value = chip.value,
                                onValueChange = { newValue ->
                                    chipConfigs[index] = chip.copy(value = newValue)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val newId = (chipConfigs.maxOfOrNull { it.id } ?: 0) + 1
                                chipConfigs.add(index + 1, ChipConfig(id = newId, color = Color.LightGray, value = "", colorName = "new"))
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Row Below")
                            }
                            IconButton(
                                onClick = { chipConfigs.removeAt(index) },
                                enabled = chipConfigs.size > 1
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Row")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(onClick = { onSave(chipConfigs.toList()) }) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showColorPicker && chipIndexToEdit != -1) {
        ColorPickerDialog(
            onDismissRequest = { showColorPicker = false },
            onConfirmation = { color ->
                chipConfigs[chipIndexToEdit] = chipConfigs[chipIndexToEdit].copy(color = color)
                showColorPicker = false
            }
        )
    }
}

@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Color) -> Unit
) {
    val controller = rememberColorPickerController()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Color") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    controller = controller
                )
                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmation(controller.selectedColor.value) }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) { Text("Cancel") }
        }
    )
} 