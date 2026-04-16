package com.example.stax.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.stax.data.HomeGameVenue
import com.example.stax.data.Session
import com.example.stax.ui.composables.DropdownSelector
import com.example.stax.ui.composables.StaxEmptyState
import com.example.stax.ui.theme.StaxHeaderGradient
import com.example.stax.ui.theme.StaxLoss
import com.example.stax.ui.theme.StaxProfit
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class ReportDateRange(val label: String) {
    All("All"),
    Last30("30D"),
    Last90("90D"),
    YTD("YTD")
}

private data class ReportVenueRollup(
    val venue: String,
    val sessionCount: Int,
    val totalBuyIn: Double,
    val totalCashOut: Double
) {
    val totalPL: Double get() = totalCashOut - totalBuyIn
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    sessions: List<Session>,
    homeGames: List<HomeGameVenue>,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateRange by remember { mutableStateOf(ReportDateRange.All) }
    var selectedVenueType by remember { mutableStateOf("All") }
    var selectedSessionType by remember { mutableStateOf("All") }
    var selectedVenue by remember { mutableStateOf("All venues") }
    var selectedGameType by remember { mutableStateOf("All games") }

    val venueOptions = remember(sessions) {
        listOf("All venues") + sessions.map { it.casinoName }.distinct().sorted()
    }
    val gameOptions = remember(sessions) {
        listOf("All games") + sessions.map { it.gameType }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filteredSessions = remember(
        sessions,
        homeGames,
        searchQuery,
        selectedDateRange,
        selectedVenueType,
        selectedSessionType,
        selectedVenue,
        selectedGameType
    ) {
        sessions.filter { session ->
            val matchesSearch = searchQuery.isBlank() || listOf(
                session.name,
                session.casinoName,
                session.gameType,
                session.stakes,
                session.antes
            ).any { it.contains(searchQuery, ignoreCase = true) }

            val venueType = session.reportVenueType(homeGames)
            val matchesVenueType = selectedVenueType == "All" || venueType == selectedVenueType
            val matchesSessionType = selectedSessionType == "All" || session.type == selectedSessionType
            val matchesVenue = selectedVenue == "All venues" || session.casinoName == selectedVenue
            val matchesGameType = selectedGameType == "All games" || session.gameType == selectedGameType
            val matchesDateRange = session.isInDateRange(selectedDateRange)

            matchesSearch && matchesVenueType && matchesSessionType && matchesVenue && matchesGameType && matchesDateRange
        }.sortedByDescending { parseSessionDate(it.date)?.time ?: 0L }
    }

    val totalBuyIn = filteredSessions.sumOf { it.buyInAmount }
    val totalCashOut = filteredSessions.sumOf { it.cashOutAmount }
    val totalPL = totalCashOut - totalBuyIn
    val avgPL = if (filteredSessions.isEmpty()) 0.0 else totalPL / filteredSessions.size
    val winRate = if (filteredSessions.isEmpty()) 0 else ((filteredSessions.count { (it.cashOutAmount - it.buyInAmount) >= 0 } * 100.0) / filteredSessions.size).toInt()
    val venueRollups = filteredSessions.groupBy { it.casinoName }
        .map { (venue, venueSessions) ->
            ReportVenueRollup(
                venue = venue,
                sessionCount = venueSessions.size,
                totalBuyIn = venueSessions.sumOf { it.buyInAmount },
                totalCashOut = venueSessions.sumOf { it.cashOutAmount }
            )
        }
        .sortedByDescending { it.totalPL }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(StaxHeaderGradient)) {
                TopAppBar(
                    title = { Text("Reports", color = Color.White, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ReportHeaderCard(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedDateRange = selectedDateRange,
                    onDateRangeSelected = { selectedDateRange = it },
                    selectedVenueType = selectedVenueType,
                    onVenueTypeSelected = { selectedVenueType = it },
                    selectedSessionType = selectedSessionType,
                    onSessionTypeSelected = { selectedSessionType = it },
                    venueOptions = venueOptions,
                    selectedVenue = selectedVenue,
                    onVenueSelected = { selectedVenue = it },
                    gameOptions = gameOptions,
                    selectedGameType = selectedGameType,
                    onGameTypeSelected = { selectedGameType = it }
                )
            }
            item {
                ReportSummaryGrid(
                    sessionCount = filteredSessions.size,
                    totalBuyIn = totalBuyIn,
                    totalCashOut = totalCashOut,
                    totalPL = totalPL,
                    avgPL = avgPL,
                    winRate = winRate
                )
            }

            if (filteredSessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        StaxEmptyState(
                            title = "No matching sessions",
                            message = "Adjust your filters or search to see more activity."
                        )
                    }
                }
            } else {
                item { ReportSectionLabel("By Venue") }
                items(venueRollups) { rollup ->
                    VenueRollupCard(rollup = rollup)
                }
                item { ReportSectionLabel("Session Activity") }
                items(filteredSessions) { session ->
                    ReportSessionRow(session = session, venueType = session.reportVenueType(homeGames))
                }
            }
        }
    }
}

@Composable
private fun ReportHeaderCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedDateRange: ReportDateRange,
    onDateRangeSelected: (ReportDateRange) -> Unit,
    selectedVenueType: String,
    onVenueTypeSelected: (String) -> Unit,
    selectedSessionType: String,
    onSessionTypeSelected: (String) -> Unit,
    venueOptions: List<String>,
    selectedVenue: String,
    onVenueSelected: (String) -> Unit,
    gameOptions: List<String>,
    selectedGameType: String,
    onGameTypeSelected: (String) -> Unit
) {
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search sessions") },
                placeholder = { Text("Venue, name, stakes, game type") }
            )

            ReportSectionLabel("Date Range")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                ReportDateRange.entries.forEach { range ->
                    FilterChip(
                        selected = selectedDateRange == range,
                        onClick = { onDateRangeSelected(range) },
                        label = { Text(range.label) }
                    )
                }
            }

            ReportSectionLabel("Filters")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("All", "Casino", "Home Game").forEach { option ->
                    FilterChip(
                        selected = selectedVenueType == option,
                        onClick = { onVenueTypeSelected(option) },
                        label = { Text(option) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("All", "Cash", "Tourney").forEach { option ->
                    FilterChip(
                        selected = selectedSessionType == option,
                        onClick = { onSessionTypeSelected(option) },
                        label = { Text(option) }
                    )
                }
            }
            DropdownSelector(
                label = "Venue",
                options = venueOptions,
                selectedOption = selectedVenue,
                onOptionSelected = onVenueSelected
            )
            DropdownSelector(
                label = "Game Type",
                options = gameOptions,
                selectedOption = selectedGameType,
                onOptionSelected = onGameTypeSelected
            )
        }
    }
}

@Composable
private fun ReportSummaryGrid(
    sessionCount: Int,
    totalBuyIn: Double,
    totalCashOut: Double,
    totalPL: Double,
    avgPL: Double,
    winRate: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryMetricCard("Sessions", sessionCount.toString(), Modifier.weight(1f))
            SummaryMetricCard("Win Rate", "$winRate%", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryMetricCard("Buy-In", currency(totalBuyIn), Modifier.weight(1f))
            SummaryMetricCard("Cash Out", currency(totalCashOut), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryMetricCard("P&L", signedCurrency(totalPL), Modifier.weight(1f), accent = if (totalPL >= 0) StaxProfit else StaxLoss)
            SummaryMetricCard("Avg / Session", signedCurrency(avgPL), Modifier.weight(1f), accent = if (avgPL >= 0) StaxProfit else StaxLoss)
        }
    }
}

@Composable
private fun SummaryMetricCard(label: String, value: String, modifier: Modifier = Modifier, accent: Color = Color.White) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VenueRollupCard(rollup: ReportVenueRollup) {
    val plColor = if (rollup.totalPL >= 0) StaxProfit else StaxLoss
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(rollup.venue, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("${rollup.sessionCount} sessions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(signedCurrency(rollup.totalPL), color = plColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("In ${currency(rollup.totalBuyIn)} • Out ${currency(rollup.totalCashOut)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReportSessionRow(session: Session, venueType: String) {
    val profitLoss = session.cashOutAmount - session.buyInAmount
    val plColor = if (profitLoss >= 0) StaxProfit else StaxLoss
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(session.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(session.casinoName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(signedCurrency(profitLoss), color = plColor, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${session.date} • $venueType • ${session.type} • ${session.gameType.ifBlank { "Game N/A" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReportSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

private fun Session.reportVenueType(homeGames: List<HomeGameVenue>): String {
    return when {
        game.equals("Home Game", ignoreCase = true) -> "Home Game"
        homeGames.any { it.name.equals(casinoName, ignoreCase = true) } -> "Home Game"
        else -> "Casino"
    }
}

private fun Session.isInDateRange(range: ReportDateRange): Boolean {
    if (range == ReportDateRange.All) return true
    val sessionDate = parseSessionDate(date) ?: return true
    val cal = Calendar.getInstance().apply { time = Date() }
    val today = cal.time
    val start = when (range) {
        ReportDateRange.All -> return true
        ReportDateRange.Last30 -> Calendar.getInstance().apply { time = today; add(Calendar.DAY_OF_YEAR, -30) }.time
        ReportDateRange.Last90 -> Calendar.getInstance().apply { time = today; add(Calendar.DAY_OF_YEAR, -90) }.time
        ReportDateRange.YTD -> Calendar.getInstance().apply {
            time = today
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    return !sessionDate.before(start)
}

private fun parseSessionDate(value: String): Date? = try {
    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(value)
} catch (_: Exception) {
    null
}

private fun currency(value: Double): String =
    NumberFormat.getCurrencyInstance(Locale.US).format(value)

private fun signedCurrency(value: Double): String =
    (if (value >= 0) "+" else "") + currency(value)
