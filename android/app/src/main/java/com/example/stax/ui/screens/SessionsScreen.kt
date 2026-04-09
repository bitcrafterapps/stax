package com.example.stax.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stax.R
import com.example.stax.data.Session
import com.example.stax.data.SessionsViewModel
import com.example.stax.ui.composables.DropdownSelector
import com.example.stax.ui.theme.StaxHeaderGradient
import com.example.stax.ui.theme.StaxLoss
import com.example.stax.ui.theme.StaxProfit
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
    var showSheet by remember { mutableStateOf(false) }
    val casinoData by sessionsViewModel.casinoData.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredSessions = when (selectedFilter) {
        "Cash" -> sessions.filter { it.type == "Cash" }
        "Tourney" -> sessions.filter { it.type == "Tourney" }
        else -> sessions
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add session")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StaxHeaderGradient)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Image(
                    painter = painterResource(id = com.example.stax.R.drawable.ic_stax_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(88.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sessions",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "All play, buy-ins, and results",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                FilterButtons(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
                WinLossSummary(sessions = filteredSessions)
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredSessions) { session ->
                        SessionItem(
                            session = session,
                            onClick = { onSessionClick(session.id) }
                        )
                    }
                }
            }
        }

        if (showSheet) {
            AddSessionSheet(
                casinoData = casinoData,
                onDismiss = { showSheet = false },
                onConfirm = { name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut ->
                    onAddSession(name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut)
                    showSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionSheet(
    casinoData: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, Double, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val dateDisplay = remember { SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date()) }
    val dateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedState by remember(casinoData) { mutableStateOf(casinoData.keys.firstOrNull() ?: "") }
    var selectedCasino by remember { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var sessionName by remember { mutableStateOf("") }
    var userEditedName by remember { mutableStateOf(false) }

    var sessionType by remember { mutableStateOf("Cash") }
    var gameType by remember { mutableStateOf("NLH") }
    var stakes by remember { mutableStateOf("1/2") }
    var antes by remember { mutableStateOf("None") }
    var buyIn by remember { mutableStateOf("") }

    val gameTypes = listOf("NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi")
    val stakesList = listOf("1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000")
    val antesList = listOf("None", "10", "20", "40", "50", "100", "200", "400", "1000")

    // Auto-fill session name from casino selection
    LaunchedEffect(selectedCasino) {
        if (!userEditedName && selectedCasino.isNotEmpty()) {
            sessionName = "$selectedCasino · $dateDisplay"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            // Header
            Text(
                "New Session",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                dateDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            // ─ CASINO ────────────────────────────────────
            SessionSectionLabel("Casino")
            Spacer(Modifier.height(10.dp))
            DropdownSelector(
                label = "State / Region",
                options = casinoData.keys.toList(),
                selectedOption = selectedState,
                onOptionSelected = {
                    selectedState = it
                    selectedCasino = casinoData[it]?.firstOrNull() ?: ""
                }
            )
            Spacer(Modifier.height(8.dp))
            DropdownSelector(
                label = "Casino / Card Room",
                options = casinoData[selectedState] ?: emptyList(),
                selectedOption = selectedCasino,
                onOptionSelected = { selectedCasino = it }
            )
            Spacer(Modifier.height(24.dp))

            // ─ NAME ──────────────────────────────────────
            SessionSectionLabel("Session Name")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = sessionName,
                onValueChange = {
                    sessionName = it
                    userEditedName = true
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                placeholder = { Text("e.g. Commerce · Apr 7") }
            )
            Spacer(Modifier.height(24.dp))

            // ─ TYPE ──────────────────────────────────────
            SessionSectionLabel("Session Type")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SessionTypeCard(
                    label = "Cash Game",
                    emoji = "💵",
                    selected = sessionType == "Cash",
                    modifier = Modifier.weight(1f),
                    onClick = { sessionType = "Cash" }
                )
                SessionTypeCard(
                    label = "Tournament",
                    emoji = "🏆",
                    selected = sessionType == "Tourney",
                    modifier = Modifier.weight(1f),
                    onClick = { sessionType = "Tourney" }
                )
            }
            Spacer(Modifier.height(24.dp))

            // ─ GAME ──────────────────────────────────────
            SessionSectionLabel("Game")
            Spacer(Modifier.height(10.dp))
            DropdownSelector(
                label = "Game Type",
                options = gameTypes,
                selectedOption = gameType,
                onOptionSelected = { gameType = it }
            )
            if (sessionType == "Cash") {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownSelector(label = "Stakes", options = stakesList, selectedOption = stakes, onOptionSelected = { stakes = it })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownSelector(label = "Antes", options = antesList, selectedOption = antes, onOptionSelected = { antes = it })
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // ─ BUY-IN ────────────────────────────────────
            SessionSectionLabel("Buy-in (optional)")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = buyIn,
                onValueChange = { buyIn = it.filter { c -> c.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        "$",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            )
            Spacer(Modifier.height(36.dp))

            // ─ CREATE ────────────────────────────────────
            Button(
                onClick = {
                    if (selectedCasino.isNotBlank()) {
                        val name = sessionName.ifBlank { "$selectedCasino · $dateDisplay" }
                        onConfirm(name, selectedCasino, dateKey, sessionType, "", gameType, stakes, antes, buyIn.toDoubleOrNull() ?: 0.0, 0.0)
                    }
                },
                enabled = selectedCasino.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Create Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SessionSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
fun SessionTypeCard(
    label: String,
    emoji: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (selected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
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
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WinLossSummary(sessions: List<Session>) {
    val totalBuyIn = sessions.sumOf { it.buyInAmount }
    val totalCashOut = sessions.sumOf { it.cashOutAmount }
    val totalProfitLoss = totalCashOut - totalBuyIn
    val profitLossColor = if (totalProfitLoss >= 0) StaxProfit else StaxLoss
    val profitLossPrefix = if (totalProfitLoss >= 0) "+" else ""
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${sessions.size} sessions", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("In ${numberFormat.format(totalBuyIn)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Out ${numberFormat.format(totalCashOut)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$profitLossPrefix${numberFormat.format(totalProfitLoss)}",
                    style = MaterialTheme.typography.labelLarge,
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
    val profitLossColor = if (profitLoss >= 0) StaxProfit else StaxLoss
    val profitLossPrefix = if (profitLoss >= 0) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
        )
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
                Text(
                    text = session.casinoName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
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
