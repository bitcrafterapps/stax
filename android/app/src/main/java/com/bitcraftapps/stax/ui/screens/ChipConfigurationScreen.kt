package com.bitcraftapps.stax.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import com.bitcraftapps.stax.data.billing.MAX_FREE_CHIP_CONFIG_CASINOS
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bitcraftapps.stax.data.ChipConfig
import com.bitcraftapps.stax.data.ChipConfigRepository
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.ui.composables.UpgradeBanner
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipConfigurationScreen(
    casinoData: Map<String, List<String>>,
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { ChipConfigRepository(context) }
    val entitlementManager = LocalEntitlementManager.current
    val isPremium by entitlementManager.isPremium.collectAsState()
    var selectedState by remember(casinoData) { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var expandedState by remember { mutableStateOf(false) }
    var selectedCasino by remember(selectedState) { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var expandedCasino by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("Cash") }
    var showChipConfigDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var chipConfigVersion by remember { mutableStateOf(0) }

    // Compute all unique casinos (flattened) to determine index of selected one
    val allCasinos = remember(casinoData) { casinoData.values.flatten().distinct() }
    val casinoIndex = remember(selectedCasino, allCasinos) { allCasinos.indexOf(selectedCasino).coerceAtLeast(0) }
    val isLockedForFree = !isPremium && casinoIndex >= MAX_FREE_CHIP_CONFIG_CASINOS

    val casinoName = selectedCasino.ifBlank { "Default" }
    val chipConfigs = remember(casinoName, type, chipConfigVersion) {
        repository.loadChipConfigs(casinoName, type)
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = { Text("Chip Configuration") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

            Box {
                ChipGrid(chipConfigs, showDollar = type == "Cash")
                if (isLockedForFree) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLockedForFree) {
                UpgradeBanner(
                    message = "Upgrade to configure all casinos",
                    onUpgrade = onNavigateToPaywall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { if (!isLockedForFree) showChipConfigDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLockedForFree
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Chips")
                }

                Button(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Defaults")
                }
            }
        }
    }

    if (showChipConfigDialog) {
        ChipConfigDialog(
            casinoName = casinoName,
            gameType = type,
            initialConfigs = chipConfigs,
            onDismiss = { showChipConfigDialog = false },
            onSave = { updatedConfigs ->
                repository.saveChipConfigs(casinoName, type, updatedConfigs)
                chipConfigVersion++
                showChipConfigDialog = false
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset to Defaults?") },
            text = {
                Text("This will replace the chip configuration for \"$casinoName\" ($type) with all standard denominations ($1 – $100,000). Your customizations will be lost.")
            },
            confirmButton = {
                Button(onClick = {
                    repository.resetToDefaults(casinoName, type)
                    chipConfigVersion++
                    showResetConfirmDialog = false
                }) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ChipGrid(chipConfigs: List<ChipConfig>, showDollar: Boolean = true) {
    val columns = 4
    val rows = chipConfigs.chunked(columns)

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            rows.forEach { rowChips ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowChips.forEach { chip ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(72.dp)
                        ) {
                            CasinoChip(
                                color = chip.color,
                                valueText = formatChipValue(chip.value, showDollar),
                                size = 56.dp
                            )
                        }
                    }
                    repeat(columns - rowChips.size) {
                        Spacer(modifier = Modifier.width(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CasinoChip(
    color: Color,
    valueText: String,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val isDark = isColorDark(color)
    val onChipColor = if (isDark) Color.White else Color.Black.copy(alpha = 0.85f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = this.size.minDimension / 2f
            val c = Offset(r, r)

            // 1. Outer dark drop-shadow ring
            drawCircle(Color.Black.copy(alpha = 0.55f), r, c)

            // 2. White rim background ring
            val rimR = r - 1.5.dp.toPx()
            drawCircle(Color.White, rimR, c)

            // 3. Chip body – fills inside, leaving outer white band visible
            val bodyR = rimR - 6.dp.toPx()
            drawCircle(color, bodyR, c)

            // 4. Edge spots: alternate colored arcs on the white rim band
            val spotStroke = 6.dp.toPx()
            val spotR = rimR - spotStroke / 2f
            val numSpots = 8
            val sweepPerSpot = 360f / numSpots
            for (i in 0 until numSpots step 2) {
                drawArc(
                    color = color,
                    startAngle = i * sweepPerSpot - 90f + 4f,
                    sweepAngle = sweepPerSpot - 8f,
                    useCenter = false,
                    style = Stroke(width = spotStroke),
                    topLeft = Offset(c.x - spotR, c.y - spotR),
                    size = Size(spotR * 2, spotR * 2)
                )
            }

            // 5. Inner fine white ring on the body surface
            val innerRingR = bodyR - 4.dp.toPx()
            drawCircle(
                Color.White.copy(alpha = 0.65f),
                innerRingR,
                c,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 6. Subtle inner body fill to add depth
            drawCircle(Color.Black.copy(alpha = 0.08f), innerRingR - 1.dp.toPx(), c)
        }

        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = onChipColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}

private fun formatChipValue(value: String, showDollar: Boolean = true): String {
    val num = value.toLongOrNull() ?: return value
    val prefix = if (showDollar) "$" else ""
    return when {
        num >= 1_000_000 -> "${prefix}${num / 1_000_000}M"
        num >= 1_000 -> "${prefix}${num / 1_000}K"
        else -> "$prefix$num"
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
                                    .clickable {
                                        chipIndexToEdit = index
                                        showColorPicker = true
                                    }
                            ) {
                                CasinoChip(
                                    color = chip.color,
                                    valueText = formatChipValue(chip.value.ifBlank { "?" }, showDollar = gameType == "Cash"),
                                    size = 44.dp
                                )
                            }
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