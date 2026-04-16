package com.example.stax.ui.screens

import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stax.R
import com.example.stax.data.AppDatabase
import com.example.stax.data.SessionDetailViewModel
import com.example.stax.data.SessionDetailViewModelFactory
import com.example.stax.ui.composables.DropdownSelector
import com.example.stax.ui.theme.StaxHeaderGradient
import com.example.stax.ui.theme.StaxLoss
import com.example.stax.ui.theme.StaxProfit
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions

private val cardShape = RoundedCornerShape(20.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToPhotos: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(AppDatabase.getDatabase(context).staxDao(), sessionId, application)
    )
    val session by viewModel.session.collectAsState()
    val casinoData by viewModel.casinoData.collectAsState()
    val logoMap by viewModel.logoMap.collectAsState()
    val hands by viewModel.hands.collectAsState(initial = emptyList())
    var isEditMode by remember { mutableStateOf(false) }

    var name by remember(session) { mutableStateOf(session?.name ?: "") }
    var casinoName by remember(session) { mutableStateOf(session?.casinoName ?: "") }

    val states = casinoData.keys.toList()
    var selectedState by remember(session, states) {
        mutableStateOf(states.find { state ->
            casinoData[state]?.contains(casinoName) == true
        } ?: states.firstOrNull() ?: "")
    }

    var expandedState by remember { mutableStateOf(false) }
    var expandedCasino by remember { mutableStateOf(false) }

    var date by remember(session) { mutableStateOf(session?.date ?: "") }
    var timeIn by remember(session) { mutableStateOf(session?.timeIn ?: "") }
    var timeOut by remember(session) { mutableStateOf(session?.timeOut ?: "") }
    var type by remember(session) { mutableStateOf(session?.type ?: "Cash") }
    var game by remember(session) { mutableStateOf(session?.game ?: "") }
    var gameType by remember(session) { mutableStateOf(session?.gameType ?: "NLH") }
    var stakes by remember(session) { mutableStateOf(session?.stakes ?: "1/2") }
    var antes by remember(session) { mutableStateOf(session?.antes ?: "None") }
    var buyInAmount by remember(session) { mutableStateOf(session?.buyInAmount?.toInt()?.toString() ?: "0") }
    var cashOutAmount by remember(session) { mutableStateOf(session?.cashOutAmount?.toInt()?.toString() ?: "0") }
    var notes by remember(session) { mutableStateOf(session?.notes ?: "") }

    val gameTypes = listOf("NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi")
    val stakesList = listOf("1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000")
    val antesList = listOf("None", "10", "20", "40", "50", "100", "200", "400", "1000")
    val currency = NumberFormat.getCurrencyInstance(Locale.US)

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = { Text(if (isEditMode) "Edit Session" else "Session Details", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { if (isEditMode) isEditMode = false else onNavigateBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!isEditMode) {
                            IconButton(onClick = { isEditMode = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        } else {
                            TextButton(onClick = {
                                viewModel.updateSession(
                                    name = name, casinoName = casinoName, date = date,
                                    timeIn = timeIn, timeOut = timeOut, type = type,
                                    game = game, gameType = gameType, stakes = stakes,
                                    antes = antes,
                                    buyInAmount = buyInAmount.toDoubleOrNull() ?: 0.0,
                                    cashOutAmount = cashOutAmount.toDoubleOrNull() ?: 0.0,
                                    notes = notes
                                )
                                isEditMode = false
                            }) {
                                Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        if (session == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (isEditMode) {
            // ── EDIT MODE ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Session Name") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words), modifier = Modifier.fillMaxWidth())
                ExposedDropdownMenuBox(expanded = expandedState, onExpandedChange = { expandedState = !expandedState }) {
                    OutlinedTextField(value = selectedState, onValueChange = {}, readOnly = true, label = { Text("State / Region") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedState) },
                        modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expandedState, onDismissRequest = { expandedState = false }) {
                        states.forEach { state ->
                            DropdownMenuItem(text = { Text(state) }, onClick = {
                                selectedState = state; casinoName = casinoData[state]?.firstOrNull() ?: ""; expandedState = false
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedCasino, onExpandedChange = { expandedCasino = !expandedCasino }) {
                    OutlinedTextField(value = casinoName, onValueChange = {}, readOnly = true, label = { Text("Casino") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCasino) },
                        modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = expandedCasino, onDismissRequest = { expandedCasino = false }) {
                        casinoData[selectedState]?.forEach { casino ->
                            DropdownMenuItem(text = { Text(casino) }, onClick = { casinoName = casino; expandedCasino = false })
                        }
                    }
                }
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, modifier = Modifier.fillMaxWidth())
                TimePicker(label = "Time In", selectedTime = timeIn, onTimeSelected = { timeIn = it })
                TimePicker(label = "Time Out", selectedTime = timeOut, onTimeSelected = { timeOut = it })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { type = "Cash" }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "Cash") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("Cash") }
                    Button(onClick = { type = "Tourney" }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = if (type == "Tourney") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)) { Text("Tourney") }
                }
                DropdownSelector(label = "Game Type", options = gameTypes, selectedOption = gameType, onOptionSelected = { gameType = it })
                if (type == "Cash") {
                    DropdownSelector(label = "Stakes", options = stakesList, selectedOption = stakes, onOptionSelected = { stakes = it })
                    DropdownSelector(label = "Antes", options = antesList, selectedOption = antes, onOptionSelected = { antes = it })
                }
                OutlinedTextField(value = buyInAmount, onValueChange = { buyInAmount = it.filter(Char::isDigit) },
                    label = { Text("Buy-in Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = cashOutAmount, onValueChange = { cashOutAmount = it.filter(Char::isDigit) },
                    label = { Text("Cash-out Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Key Hands / Notes") },
                    modifier = Modifier.fillMaxWidth().height(130.dp))
                Spacer(Modifier.height(24.dp))
            }
        } else {
            // ── VIEW MODE ─────────────────────────────────────────────────────
            val profitLoss = (cashOutAmount.toDoubleOrNull() ?: 0.0) - (buyInAmount.toDoubleOrNull() ?: 0.0)
            val profitColor = if (profitLoss >= 0) StaxProfit else StaxLoss
            val profitPrefix = if (profitLoss >= 0) "+" else ""

            val logoBitmap = remember(casinoName, logoMap) {
                val resName = logoMap[casinoName]
                if (resName != null) {
                    try {
                        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap() else null
                    } catch (_: Exception) { null }
                } else null
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── HERO CARD ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Casino identity row
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (logoBitmap != null) {
                                Image(bitmap = logoBitmap, contentDescription = "$casinoName logo",
                                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                            } else {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Casino, contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                                }
                            }
                            Column {
                                Text(casinoName, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text(name, style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.60f), maxLines = 1)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Big P&L
                        Text(
                            text = "$profitPrefix${currency.format(profitLoss)}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = profitColor
                        )
                        Text("Profit / Loss", style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.55f))

                        Spacer(Modifier.height(16.dp))

                        // Buy-in / Cash-out stat chips
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatChip(label = "Buy-in", value = currency.format(buyInAmount.toDoubleOrNull() ?: 0.0), modifier = Modifier.weight(1f))
                            StatChip(label = "Cash-out", value = currency.format(cashOutAmount.toDoubleOrNull() ?: 0.0), modifier = Modifier.weight(1f))
                            StatChip(
                                label = if (type == "Cash") "${stakes} · ${gameType}" else "Tournament",
                                value = if (type == "Cash") "Cash" else "Tourney",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── SESSION INFO CARD ──────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionLabel("Session Info")
                    Spacer(Modifier.height(8.dp))
                    Card(shape = cardShape, colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)),
                        elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            IconDetailRow(icon = Icons.Default.CalendarMonth, label = "Date", value = date)
                            if (timeIn.isNotBlank() || timeOut.isNotBlank()) {
                                IconDetailRow(icon = Icons.Default.AccessTime, label = "Time",
                                    value = buildString {
                                        if (timeIn.isNotBlank()) append("In $timeIn")
                                        if (timeIn.isNotBlank() && timeOut.isNotBlank()) append("  →  ")
                                        if (timeOut.isNotBlank()) append("Out $timeOut")
                                    })
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            IconDetailRow(icon = Icons.Default.Style, label = "Type",
                                value = type,
                                valueContent = {
                                    TypeBadge(type)
                                })
                            IconDetailRow(icon = Icons.Default.Casino, label = "Game", value = gameType)
                            if (type == "Cash") {
                                IconDetailRow(icon = Icons.Default.AttachMoney, label = "Stakes", value = stakes)
                                if (antes != "None") {
                                    IconDetailRow(icon = Icons.Default.MonetizationOn, label = "Antes", value = "$$antes")
                                }
                            }
                        }
                    }
                }

                // ── NOTES CARD ─────────────────────────────────────────────
                if (notes.isNotBlank()) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SectionLabel("Notes")
                        Spacer(Modifier.height(8.dp))
                        Card(shape = cardShape, colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)),
                            elevation = CardDefaults.cardElevation(0.dp)) {
                            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
                                Text(notes, style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f), lineHeight = 22.sp)
                            }
                        }
                    }
                }

                // ── PHOTOS BUTTON ──────────────────────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionLabel("Photos")
                    Spacer(Modifier.height(8.dp))
                    val iconRes = if (type == "Cash") R.drawable.cash else R.drawable.stack
                    Button(
                        onClick = { onNavigateToPhotos(sessionId) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View Photos", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Hand History ─────────────────────────────────────
                    SectionLabel("Hands")
                    Spacer(Modifier.height(8.dp))
                    HandHistorySection(
                        hands = hands,
                        onAddHand = { r1, s1, r2, s2, pos, result, notes, villains ->
                            viewModel.addHand(r1, s1, r2, s2, pos, result, notes, villains)
                        },
                        onToggleStar = { viewModel.toggleStarHand(it) },
                        onDeleteHand = { viewModel.deleteHand(it) }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.55f), maxLines = 1)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun IconDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueContent: (@Composable () -> Unit)? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.width(64.dp))
        if (valueContent != null) {
            valueContent()
        } else {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                color = Color.White)
        }
    }
}

@Composable
private fun TypeBadge(type: String) {
    val isCash = type == "Cash"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isCash) Color(0xFF1A4D2E) else Color(0xFF2D1A4D))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(type, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
            color = if (isCash) Color(0xFF4ADE80) else Color(0xFFA78BFA))
    }
}

@Composable
fun DisplayField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun TimePicker(label: String, selectedTime: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(context, { _, h, m ->
        onTimeSelected(String.format("%02d:%02d", h, m))
    }, hour, minute, true)

    OutlinedTextField(
        value = selectedTime,
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = "Select Time") },
        modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }
    )
}
