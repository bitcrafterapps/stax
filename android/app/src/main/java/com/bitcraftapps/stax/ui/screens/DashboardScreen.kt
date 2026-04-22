package com.bitcraftapps.stax.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.bitcraftapps.stax.R
import com.bitcraftapps.stax.data.CardRoomRepository
import com.bitcraftapps.stax.data.HomeGameVenue
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.data.billing.LimitResult
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.data.billing.SubscriptionState
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
import com.bitcraftapps.stax.data.CasinoFolder
import com.bitcraftapps.stax.ui.composables.DropdownSelector
import com.bitcraftapps.stax.ui.composables.StaxEmptyState
import com.bitcraftapps.stax.ui.composables.StaxScreenHeader
import com.bitcraftapps.stax.ui.composables.UpgradeBanner
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    casinoFolders: List<CasinoFolder>,
    onCasinoClick: (String) -> Unit,
    casinoData: Map<String, List<String>>,
    onAddSession: (String, String, String, String, String, String, String, Double, Double) -> Unit,
    onNavigateToPaywall: () -> Unit = {},
    homeGames: List<HomeGameVenue> = emptyList(),
    onSaveHomeGame: (String, String, String) -> Unit = { _, _, _ -> },
    logoMap: Map<String, String> = emptyMap(),
) {
    var showAddSessionDialog by remember { mutableStateOf(false) }
    val entitlementManager = LocalEntitlementManager.current
    val totalSessions = casinoFolders.sumOf { it.sessionCount }

    if (showAddSessionDialog) {
        AddSessionDialog(
            casinoData = casinoData,
            homeGames = homeGames,
            onConfirm = { name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut ->
                val limitResult = entitlementManager.checkLimit(
                    Feature.SESSION_CREATE,
                    totalSessions = totalSessions
                )
                if (limitResult is LimitResult.Blocked) {
                    showAddSessionDialog = false
                    onNavigateToPaywall()
                } else {
                    onAddSession(name, casinoName, sessionType, game, gameType, stakes, antes, buyIn, cashOut)
                    showAddSessionDialog = false
                }
            },
            onSaveHomeGame = onSaveHomeGame,
            onDismiss = { showAddSessionDialog = false }
        )
    }

    val subscriptionState by entitlementManager.subscriptionState.collectAsState()
    val isPremium by entitlementManager.isPremium.collectAsState()

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
                modifier = Modifier.size(88.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StaxScreenHeader(
                title = "Casino/Card Rooms",
                subtitle = "Browse sessions by venue"
            )
        }

        // Contextual upsell banners
        if (!isPremium && totalSessions >= 3) {
            UpgradeBanner(
                message = "You've used 3 of 3 free sessions. Unlock unlimited.",
                onUpgrade = onNavigateToPaywall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        val trialState = subscriptionState
        if (trialState is SubscriptionState.Premium && trialState.isInTrial) {
            val daysLeft = entitlementManager.getTrialDaysRemaining()
            if (daysLeft <= 2) {
                UpgradeBanner(
                    message = "Your free trial ends in $daysLeft days",
                    onUpgrade = onNavigateToPaywall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        if (casinoFolders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                StaxEmptyState(
                    title = "No sessions yet",
                    message = "Tap the + button to create your first session and start building your gallery."
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 80.dp)
            ) {
                items(casinoFolders) { casinoFolder ->
                                    CasinoFolderItem(
                                        casinoFolder = casinoFolder,
                                        logoResName = logoMap[casinoFolder.casinoName],
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
    logoResName: String? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val logoBitmap = remember(logoResName) {
        if (logoResName != null) {
            try {
                val id = context.resources.getIdentifier(logoResName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap()
                else null
            } catch (_: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
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
                    tint = Color.White.copy(alpha = 0.25f)
                )
            }

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.30f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.80f)
                            )
                        )
                    )
            )

            // Bottom overlay: logo badge + casino name + session count
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap,
                        contentDescription = "${casinoFolder.casinoName} logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    Text(
                        text = casinoFolder.casinoName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${casinoFolder.sessionCount} ${if (casinoFolder.sessionCount == 1) "session" else "sessions"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSessionDialog(
    casinoData: Map<String, List<String>>,
    homeGames: List<HomeGameVenue>,
    onConfirm: (String, String, String, String, String, String, String, Double, Double) -> Unit,
    onSaveHomeGame: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dateDisplay = remember { java.text.SimpleDateFormat("MMM d, yyyy", Locale.US).format(java.util.Date()) }
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
    var type by remember { mutableStateOf("Cash") }
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
        if (!userEditedName && selectedVenueName.isNotEmpty()) {
            sessionName = "$selectedVenueName · $dateDisplay"
        }
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
            Text(
                "New Session",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                dateDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            SessionSectionLabel("Venue")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SessionTypeCard(
                    label = "Casino",
                    emoji = "🎰",
                    selected = venueMode == "Casino",
                    modifier = Modifier.weight(1f),
                    onClick = { venueMode = "Casino" }
                )
                SessionTypeCard(
                    label = "Home Game",
                    emoji = "🏠",
                    selected = venueMode == "Home Game",
                    modifier = Modifier.weight(1f),
                    onClick = { venueMode = "Home Game" }
                )
            }
            Spacer(Modifier.height(16.dp))
            if (venueMode == "Casino") {
                DropdownSelector(
                    label = "State / Region",
                    options = stateOptions,
                    selectedOption = selectedState,
                    onOptionSelected = {
                        selectedStateTouched = true
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
            } else {
                if (homeGames.isNotEmpty()) {
                    DropdownSelector(
                        label = "Saved Home Games",
                        options = homeGameOptions,
                        selectedOption = selectedHomeGameLabel,
                        onOptionSelected = {
                            selectedHomeGameLabel = it
                            val homeGame = homeGames.firstOrNull { game -> game.displayName == it }
                            if (homeGame != null) {
                                homeGameName = homeGame.name
                                homeGameCity = homeGame.city
                                homeGameState = homeGame.state
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

            SessionSectionLabel("Session Type")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SessionTypeCard(
                    label = "Cash Game",
                    emoji = "💵",
                    selected = type == "Cash",
                    modifier = Modifier.weight(1f),
                    onClick = { type = "Cash" }
                )
                SessionTypeCard(
                    label = "Tournament",
                    emoji = "🏆",
                    selected = type == "Tourney",
                    modifier = Modifier.weight(1f),
                    onClick = { type = "Tourney" }
                )
            }
            Spacer(Modifier.height(24.dp))

            SessionSectionLabel("Game")
            Spacer(Modifier.height(10.dp))
            DropdownSelector(
                label = "Game Type",
                options = gameTypes,
                selectedOption = gameType,
                onOptionSelected = { gameType = it }
            )
            if (type == "Cash") {
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

            SessionSectionLabel("Money")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            Spacer(Modifier.height(36.dp))

            Button(
                onClick = {
                    if (selectedVenueName.isNotBlank()) {
                        val venueName = selectedVenueName
                        if (venueMode == "Home Game") {
                            onSaveHomeGame(homeGameName, homeGameCity, homeGameState)
                        }
                        val name = sessionName.ifBlank { "$venueName · $dateDisplay" }
                        onConfirm(
                            name,
                            venueName,
                            type,
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