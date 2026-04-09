package com.example.stax.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stax.data.AppDatabase
import com.example.stax.data.SessionDetailViewModel
import com.example.stax.data.SessionDetailViewModelFactory
import com.example.stax.ui.composables.DropdownSelector
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.stax.ui.theme.StaxHeaderGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: SessionDetailViewModel = viewModel(
        factory = SessionDetailViewModelFactory(AppDatabase.getDatabase(context).staxDao(), sessionId, application)
    )
    val session by viewModel.session.collectAsState()
    val casinoData by viewModel.casinoData.collectAsState()
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

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = { Text("Session Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!isEditMode) {
                            IconButton(onClick = { isEditMode = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Session")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            if (isEditMode) {
                FloatingActionButton(onClick = {
                    viewModel.updateSession(
                        name = name,
                        casinoName = casinoName,
                        date = date,
                        timeIn = timeIn,
                        timeOut = timeOut,
                        type = type,
                        game = game,
                        gameType = gameType,
                        stakes = stakes,
                        antes = antes,
                        buyInAmount = buyInAmount.toDoubleOrNull() ?: 0.0,
                        cashOutAmount = cashOutAmount.toDoubleOrNull() ?: 0.0,
                        notes = notes
                    )
                    isEditMode = false
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save Session")
                }
            }
        }
    ) { paddingValues ->
        if (session == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val profitLoss = (cashOutAmount.toDoubleOrNull() ?: 0.0) - (buyInAmount.toDoubleOrNull() ?: 0.0)
                val profitLossFormatted = NumberFormat.getCurrencyInstance(Locale.US).format(profitLoss)
                val profitColor = if (profitLoss >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

                Text(
                    "Profit/Loss: $profitLossFormatted",
                    style = MaterialTheme.typography.headlineSmall,
                    color = profitColor,
                    fontWeight = FontWeight.Bold
                )

                if (isEditMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Session Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
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
                            states.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        selectedState = state
                                        casinoName = casinoData[state]?.firstOrNull() ?: ""
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
                            value = casinoName,
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
                            casinoData[selectedState]?.forEach { casino ->
                                DropdownMenuItem(
                                    text = { Text(casino) },
                                    onClick = {
                                        casinoName = casino
                                        expandedCasino = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TimePicker(label = "Time In", selectedTime = timeIn, onTimeSelected = { timeIn = it })
                    TimePicker(label = "Time Out", selectedTime = timeOut, onTimeSelected = { timeOut = it })
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { type = "Cash" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "Cash") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Cash")
                        }
                        Button(
                            onClick = { type = "Tourney" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == "Tourney") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Tourney")
                        }
                    }
                    OutlinedTextField(
                        value = game,
                        onValueChange = { game = it },
                        label = { Text("Game") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownSelector(label = "Game Type", options = gameTypes, selectedOption = gameType, onOptionSelected = { gameType = it })
                    if (type == "Cash") {
                        DropdownSelector(label = "Stakes", options = stakesList, selectedOption = stakes, onOptionSelected = { stakes = it })
                        DropdownSelector(label = "Antes", options = antesList, selectedOption = antes, onOptionSelected = { antes = it })
                    }
                    OutlinedTextField(
                        value = buyInAmount,
                        onValueChange = { value ->
                            buyInAmount = value.filter { it.isDigit() }
                        },
                        label = { Text("Buy-in Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cashOutAmount,
                        onValueChange = { value ->
                            cashOutAmount = value.filter { it.isDigit() }
                        },
                        label = { Text("Cash-out Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Key Hands / Notes") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                } else {
                    DisplayField("Session Name", name)
                    DisplayField("Casino", casinoName)
                    DisplayField("Date", date)
                    DisplayField("Time In", timeIn)
                    DisplayField("Time Out", timeOut)
                    DisplayField("Type", type)
                    DisplayField("Game", game)
                    DisplayField("Game Type", gameType)
                    if (type == "Cash") {
                        DisplayField("Stakes", stakes)
                        DisplayField("Antes", antes)
                    }
                    DisplayField("Buy-in Amount", NumberFormat.getCurrencyInstance(Locale.US).format(buyInAmount.toDoubleOrNull() ?: 0.0))
                    DisplayField("Cash-out Amount", NumberFormat.getCurrencyInstance(Locale.US).format(cashOutAmount.toDoubleOrNull() ?: 0.0))
                    DisplayField("Notes", notes)
                }
            }
        }
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

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(String.format("%02d:%02d", selectedHour, selectedMinute))
        }, hour, minute, true
    )

    OutlinedTextField(
        value = selectedTime,
        onValueChange = { },
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = "Select Time"
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { timePickerDialog.show() }
    )
} 