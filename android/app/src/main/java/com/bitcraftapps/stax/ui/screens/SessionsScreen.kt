package com.bitcraftapps.stax.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bitcraftapps.stax.R
import com.bitcraftapps.stax.data.CardRoomRepository
import com.bitcraftapps.stax.data.HomeGameVenue
import com.bitcraftapps.stax.data.Session
import com.bitcraftapps.stax.data.SessionsViewModel
import com.bitcraftapps.stax.ui.composables.DropdownSelector
import com.bitcraftapps.stax.ui.composables.StaxEmptyState
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
import com.bitcraftapps.stax.ui.theme.StaxLoss
import com.bitcraftapps.stax.ui.theme.StaxProfit
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Groups sessions by casino for display
data class CasinoGroup(
    val casinoName: String,
    val sessions: List<Session>
) {
    val sessionCount: Int get() = sessions.size
    val cashCount: Int get() = sessions.count { it.type == "Cash" }
    val tourneyCount: Int get() = sessions.count { it.type == "Tourney" }
    val totalBuyIn: Double get() = sessions.sumOf { it.buyInAmount }
    val totalCashOut: Double get() = sessions.sumOf { it.cashOutAmount }
    val totalPL: Double get() = totalCashOut - totalBuyIn
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    sessions: List<Session>,
    onAddSession: (String, String, String, String, String, String, String, String, Double, Double) -> Unit,
    homeGames: List<HomeGameVenue> = emptyList(),
    onSaveHomeGame: (String, String, String) -> Unit = { _, _, _ -> },
    onCasinoClick: (String) -> Unit,
    sessionsViewModel: SessionsViewModel,
    logoMap: Map<String, String> = emptyMap()
) {
    var showSheet by remember { mutableStateOf(false) }
    val casinoData by sessionsViewModel.casinoData.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredSessions = when (selectedFilter) {
        "Cash"   -> sessions.filter { it.type == "Cash" }
        "Tourney" -> sessions.filter { it.type == "Tourney" }
        else     -> sessions
    }

    // Group by casino, ordered by most recent session
    val casinoGroups = filteredSessions
        .groupBy { it.casinoName }
        .map { (name, list) -> CasinoGroup(name, list.sortedByDescending { it.date }) }
        .sortedByDescending { group -> group.sessions.maxOfOrNull { it.date } ?: "" }

    val currency = NumberFormat.getCurrencyInstance(Locale.US)

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
            // Header
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
                    modifier = Modifier.size(88.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Sessions", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground)
                    Text("All play, buy-ins, and results", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                // Filter chips
                FilterButtons(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it })
                Spacer(Modifier.height(10.dp))

                // Overall summary bar
                OverallSummary(sessions = filteredSessions, casinoCount = casinoGroups.size)
                Spacer(Modifier.height(10.dp))

                if (casinoGroups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        StaxEmptyState(title = "No sessions", message = "Tap + to create your first session.")
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(casinoGroups) { group ->
                            CasinoGroupRow(
                                group = group,
                                logoResName = logoMap[group.casinoName],
                                onClick = { onCasinoClick(group.casinoName) }
                            )
                        }
                        // Bottom padding so FAB doesn't cover last item
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        if (showSheet) {
            AddSessionSheet(
                casinoData = casinoData,
                homeGames = homeGames,
                onDismiss = { showSheet = false },
                onSaveHomeGame = onSaveHomeGame,
                onConfirm = { name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut ->
                    onAddSession(name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut)
                    showSheet = false
                }
            )
        }
    }
}

// ── Casino group row ──────────────────────────────────────────────────────────

@Composable
fun CasinoGroupRow(
    group: CasinoGroup,
    logoResName: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val plColor = if (group.totalPL >= 0) StaxProfit else StaxLoss
    val plPrefix = if (group.totalPL >= 0) "+" else ""

    val logoBitmap = remember(logoResName) {
        if (logoResName != null) {
            try {
                val id = context.resources.getIdentifier(logoResName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap() else null
            } catch (_: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Logo or fallback icon
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap,
                    contentDescription = "${group.casinoName} logo",
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }

            // Name + type breakdown
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(group.casinoName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val parts = buildList {
                        if (group.cashCount > 0) add("${group.cashCount} Cash")
                        if (group.tourneyCount > 0) add("${group.tourneyCount} Tourney")
                    }
                    Text(parts.joinToString(" · "), style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.55f))
                }
            }

            // P&L + chevron
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("$plPrefix${currency.format(group.totalPL)}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = plColor)
                Text("${group.sessionCount} sessions", style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f))
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null,
                tint = Color.White.copy(alpha = 0.30f), modifier = Modifier.size(14.dp))
        }
    }
}

// ── Overall summary banner ────────────────────────────────────────────────────

@Composable
fun OverallSummary(sessions: List<Session>, casinoCount: Int) {
    val currency = NumberFormat.getCurrencyInstance(Locale.US)
    val totalBuyIn = sessions.sumOf { it.buyInAmount }
    val totalCashOut = sessions.sumOf { it.cashOutAmount }
    val totalPL = totalCashOut - totalBuyIn
    val plColor = if (totalPL >= 0) StaxProfit else StaxLoss
    val plPrefix = if (totalPL >= 0) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$casinoCount ${if (casinoCount == 1) "casino" else "casinos"}",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("In ${currency.format(totalBuyIn)}",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Out ${currency.format(totalCashOut)}",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$plPrefix${currency.format(totalPL)}",
                style = MaterialTheme.typography.labelLarge, color = plColor, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Filter chips ──────────────────────────────────────────────────────────────

@Composable
fun FilterButtons(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("All", "Cash", "Tourney").forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ── Win/Loss summary (kept for CasinoSessions use) ───────────────────────────

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
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${sessions.size} sessions", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("In ${numberFormat.format(totalBuyIn)}", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Out ${numberFormat.format(totalCashOut)}", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$profitLossPrefix${numberFormat.format(totalProfitLoss)}",
                style = MaterialTheme.typography.labelLarge, color = profitLossColor, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Session item (kept for potential reuse) ───────────────────────────────────

@Composable
fun SessionItem(
    session: Session,
    logoResName: String? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val profitLoss = session.cashOutAmount - session.buyInAmount
    val profitLossColor = if (profitLoss >= 0) StaxProfit else StaxLoss
    val profitLossPrefix = if (profitLoss >= 0) "+" else ""

    val logoBitmap = remember(logoResName) {
        if (logoResName != null) {
            try {
                val id = context.resources.getIdentifier(logoResName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap() else null
            } catch (_: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                if (logoBitmap != null) {
                    Image(bitmap = logoBitmap, contentDescription = "${session.casinoName} logo",
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).align(Alignment.TopStart),
                        contentScale = ContentScale.Crop)
                } else {
                    val iconRes = if (session.type == "Cash") R.drawable.cash else R.drawable.stack
                    Image(painter = painterResource(id = iconRes), contentDescription = session.type,
                        modifier = Modifier.size(40.dp).align(Alignment.TopStart))
                }
                val badgeRes = if (session.type == "Cash") R.drawable.cash else R.drawable.stack
                Image(painter = painterResource(id = badgeRes), contentDescription = session.type,
                    modifier = Modifier.size(18.dp).align(Alignment.BottomEnd))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(session.casinoName, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(session.date, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f))
            }
            Text("$profitLossPrefix${NumberFormat.getCurrencyInstance(Locale.US).format(profitLoss)}",
                color = profitLossColor, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium)
        }
    }
}

// ── Section label / type card helpers ────────────────────────────────────────

@Composable
fun SessionSectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionTypeCard(label: String, emoji: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainer
    Card(modifier = modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Add session bottom sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionSheet(
    casinoData: Map<String, List<String>>,
    homeGames: List<HomeGameVenue>,
    onDismiss: () -> Unit,
    onSaveHomeGame: (String, String, String) -> Unit,
    onConfirm: (String, String, String, String, String, String, String, String, Double, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dateDisplay = remember { SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date()) }
    val dateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val stateOptions = remember(casinoData) { casinoData.keys.toList().sorted() }

    var venueMode by remember { mutableStateOf("Casino") }
    var selectedState by remember(casinoData) { mutableStateOf(stateOptions.firstOrNull() ?: "") }
    var selectedCasino by remember { mutableStateOf(casinoData[selectedState]?.firstOrNull() ?: "") }
    var selectedStateTouched by remember { mutableStateOf(false) }
    var selectedHomeGameLabel by remember { mutableStateOf("New home game") }
    var homeGameName by remember { mutableStateOf("") }
    var homeGameCity by remember { mutableStateOf("") }
    var homeGameState by remember(casinoData) { mutableStateOf(stateOptions.firstOrNull() ?: "") }
    var homeGameStateTouched by remember { mutableStateOf(false) }
    var sessionName by remember { mutableStateOf("") }
    var userEditedName by remember { mutableStateOf(false) }
    var sessionType by remember { mutableStateOf("Cash") }
    var gameType by remember { mutableStateOf("NLH") }
    var stakes by remember { mutableStateOf("1/2") }
    var antes by remember { mutableStateOf("None") }
    var buyIn by remember { mutableStateOf("") }
    var cashOut by remember { mutableStateOf("") }

    val gameTypes = listOf("NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi")
    val stakesList = listOf("1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000")
    val antesList = listOf("None", "10", "20", "40", "50", "100", "200", "400", "1000")
    val homeGameOptions = remember(homeGames) { listOf("New home game") + homeGames.map { it.displayName } }
    val selectedVenueName = if (venueMode == "Casino") selectedCasino else homeGameName.trim()
    var locationPrefillAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(selectedVenueName, venueMode) {
        if (!userEditedName && selectedVenueName.isNotEmpty()) sessionName = "$selectedVenueName · $dateDisplay"
    }

    LaunchedEffect(stateOptions) {
        if (stateOptions.isEmpty() || locationPrefillAttempted) return@LaunchedEffect
        locationPrefillAttempted = true

        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasLocationPermission) return@LaunchedEffect

        LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener { location ->
            if (location == null) return@addOnSuccessListener
            scope.launch {
                val detectedState = CardRoomRepository(context)
                    .getStateFromLocation(location.latitude, location.longitude)
                    ?.takeIf { it in stateOptions } ?: return@launch

                if (!selectedStateTouched) {
                    selectedState = detectedState
                    selectedCasino = casinoData[detectedState]?.firstOrNull() ?: ""
                }
                if (!homeGameStateTouched) {
                    homeGameState = detectedState
                }
            }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)
            .padding(horizontal = 24.dp).padding(bottom = 48.dp)) {
            Text("New Session", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(dateDisplay, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(28.dp))

            SessionSectionLabel("Venue")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SessionTypeCard("Casino", "🎰", venueMode == "Casino", Modifier.weight(1f)) { venueMode = "Casino" }
                SessionTypeCard("Home Game", "🏠", venueMode == "Home Game", Modifier.weight(1f)) { venueMode = "Home Game" }
            }
            Spacer(Modifier.height(16.dp))
            if (venueMode == "Casino") {
                DropdownSelector(label = "State / Region", options = stateOptions,
                    selectedOption = selectedState, onOptionSelected = {
                        selectedStateTouched = true
                        selectedState = it; selectedCasino = casinoData[it]?.firstOrNull() ?: ""
                    })
                Spacer(Modifier.height(8.dp))
                DropdownSelector(label = "Casino / Card Room", options = casinoData[selectedState] ?: emptyList(),
                    selectedOption = selectedCasino, onOptionSelected = { selectedCasino = it })
            } else {
                if (homeGames.isNotEmpty()) {
                    DropdownSelector(
                        label = "Saved Home Games",
                        options = homeGameOptions,
                        selectedOption = selectedHomeGameLabel,
                        onOptionSelected = {
                            selectedHomeGameLabel = it
                            val saved = homeGames.firstOrNull { game -> game.displayName == it }
                            if (saved != null) {
                                homeGameName = saved.name
                                homeGameCity = saved.city
                                homeGameState = saved.state
                            } else {
                                homeGameName = ""
                                homeGameCity = ""
                                homeGameState = stateOptions.firstOrNull() ?: ""
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = homeGameName,
                    onValueChange = { homeGameName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    label = { Text("Home game name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = homeGameCity,
                    onValueChange = { homeGameCity = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    label = { Text("City") }
                )
                Spacer(Modifier.height(8.dp))
                DropdownSelector(
                    label = "State / Region",
                    options = stateOptions,
                    selectedOption = homeGameState,
                    onOptionSelected = {
                        homeGameStateTouched = true
                        homeGameState = it
                    }
                )
            }
            Spacer(Modifier.height(24.dp))

            SessionSectionLabel("Session Name")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = sessionName, onValueChange = { sessionName = it; userEditedName = true },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                placeholder = { Text("e.g. Commerce · Apr 7") })
            Spacer(Modifier.height(24.dp))

            SessionSectionLabel("Session Type")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SessionTypeCard("Cash Game", "💵", sessionType == "Cash", Modifier.weight(1f)) { sessionType = "Cash" }
                SessionTypeCard("Tournament", "🏆", sessionType == "Tourney", Modifier.weight(1f)) { sessionType = "Tourney" }
            }
            Spacer(Modifier.height(24.dp))

            SessionSectionLabel("Game")
            Spacer(Modifier.height(10.dp))
            DropdownSelector(label = "Game Type", options = gameTypes, selectedOption = gameType, onOptionSelected = { gameType = it })
            if (sessionType == "Cash") {
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { DropdownSelector("Stakes", stakesList, stakes) { stakes = it } }
                    Box(Modifier.weight(1f)) { DropdownSelector("Antes", antesList, antes) { antes = it } }
                }
            }
            Spacer(Modifier.height(24.dp))

            SessionSectionLabel("Money")
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = buyIn,
                    onValueChange = { buyIn = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Buy-in") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            "$",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                )
                OutlinedTextField(
                    value = cashOut,
                    onValueChange = { cashOut = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Cash out") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = {
                        Text(
                            "$",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                )
            }
            Spacer(Modifier.height(36.dp))

            Button(
                onClick = {
                    if (selectedVenueName.isNotBlank()) {
                        if (venueMode == "Home Game") {
                            onSaveHomeGame(homeGameName, homeGameCity, homeGameState)
                        }
                        val name = sessionName.ifBlank { "$selectedVenueName · $dateDisplay" }
                        onConfirm(
                            name,
                            selectedVenueName,
                            dateKey,
                            sessionType,
                            venueMode,
                            gameType,
                            stakes,
                            antes,
                            buyIn.toDoubleOrNull() ?: 0.0,
                            cashOut.toDoubleOrNull() ?: 0.0
                        )
                    }
                },
                enabled = selectedVenueName.isNotBlank() &&
                    (venueMode == "Casino" || (homeGameCity.isNotBlank() && homeGameState.isNotBlank())),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
