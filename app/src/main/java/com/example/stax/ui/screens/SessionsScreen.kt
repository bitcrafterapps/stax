package com.example.stax.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.stax.R
import com.example.stax.data.Session
import com.example.stax.data.SessionsViewModel
import com.example.stax.ui.composables.DropdownSelector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    sessions: List<Session>,
    onAddSession: (String, String, String, String, String, String, String, String, Double, Double) -> Unit,
    onSessionClick: (Long) -> Unit,
    sessionsViewModel: SessionsViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val casinoData by sessionsViewModel.casinoData.collectAsState()
    var selectedState by remember(casinoData) { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var expandedState by remember { mutableStateOf(false) }
    var selectedCasino by remember(selectedState) { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var expandedCasino by remember { mutableStateOf(false) }

    var sessionName by remember { mutableStateOf("") }
    var sessionDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var sessionType by remember { mutableStateOf("Cash") }
    var sessionGame by remember { mutableStateOf("") }
    var gameType by remember { mutableStateOf("NLH") }
    var stakes by remember { mutableStateOf("1/2") }
    var antes by remember { mutableStateOf("None") }
    var buyInAmount by remember { mutableStateOf("") }
    var cashOutAmount by remember { mutableStateOf("") }

    val gameTypes = listOf("NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi")
    val stakesList = listOf("1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000")
    val antesList = listOf("None", "10", "20", "40", "50", "100", "200", "400", "1000")

    var selectedFilter by remember { mutableStateOf("All") }
    val filteredSessions = when (selectedFilter) {
        "Cash" -> sessions.filter { it.type == "Cash" }
        "Tourney" -> sessions.filter { it.type == "Tourney" }
        else -> sessions
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, "Add Session")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            FilterButtons(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            WinLossSummary(sessions = filteredSessions)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSessions) { session ->
                    SessionItem(
                        session = session,
                        onClick = { onSessionClick(session.id) }
                    )
                }
            }
        }

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Session", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = sessionName,
                            onValueChange = { sessionName = it },
                            label = { Text("Session Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
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
                        OutlinedTextField(value = sessionDate, onValueChange = { sessionDate = it }, label = { Text("Date (yyyy-MM-dd)") })
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { sessionType = "Cash" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sessionType == "Cash") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Cash")
                            }
                            Button(
                                onClick = { sessionType = "Tourney" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sessionType == "Tourney") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("Tourney")
                            }
                        }
                        OutlinedTextField(value = sessionGame, onValueChange = { sessionGame = it }, label = { Text("Game") })
                        DropdownSelector(label = "Game Type", options = gameTypes, selectedOption = gameType, onOptionSelected = { gameType = it })
                        if (sessionType == "Cash") {
                            DropdownSelector(label = "Stakes", options = stakesList, selectedOption = stakes, onOptionSelected = { stakes = it })
                            DropdownSelector(label = "Antes", options = antesList, selectedOption = antes, onOptionSelected = { antes = it })
                        }
                        OutlinedTextField(value = buyInAmount, onValueChange = { buyInAmount = it }, label = { Text("Buy-in Amount") })
                        OutlinedTextField(value = cashOutAmount, onValueChange = { cashOutAmount = it }, label = { Text("Cash-out Amount") })
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                onAddSession(
                                    sessionName,
                                    selectedCasino ?: "",
                                    sessionDate,
                                    sessionType,
                                    sessionGame,
                                    gameType,
                                    stakes,
                                    antes,
                                    buyInAmount.toDoubleOrNull() ?: 0.0,
                                    cashOutAmount.toDoubleOrNull() ?: 0.0
                                )
                                showDialog = false
                            }) {
                                Text("Add")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButtons(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf("All", "Cash", "Tourney")
        filters.forEach { filter ->
            Button(
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(filter)
            }
        }
    }
}

@Composable
fun WinLossSummary(sessions: List<Session>) {
    val totalBuyIn = sessions.sumOf { it.buyInAmount }
    val totalCashOut = sessions.sumOf { it.cashOutAmount }
    val totalProfitLoss = totalCashOut - totalBuyIn
    val profitLossColor = if (totalProfitLoss >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    val profitLossPrefix = if (totalProfitLoss >= 0) "+" else ""
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Session Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Sessions:", fontWeight = FontWeight.SemiBold)
                Text("${sessions.size}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Buy-in:", fontWeight = FontWeight.SemiBold)
                Text(numberFormat.format(totalBuyIn))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Cash-out:", fontWeight = FontWeight.SemiBold)
                Text(numberFormat.format(totalCashOut))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Profit/Loss:", fontWeight = FontWeight.Bold)
                Text(
                    text = "$profitLossPrefix${numberFormat.format(totalProfitLoss)}",
                    color = profitLossColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SessionItem(
    session: Session,
    onClick: () -> Unit
) {
    val profitLoss = session.cashOutAmount - session.buyInAmount
    val profitLossColor = if (profitLoss >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    val profitLossPrefix = if (profitLoss >= 0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconRes = if (session.type == "Cash") R.drawable.cash else R.drawable.stack
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = session.type,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = session.casinoName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(text = session.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Text(
                text = "$profitLossPrefix${NumberFormat.getCurrencyInstance(Locale.US).format(profitLoss)}",
                color = profitLossColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}