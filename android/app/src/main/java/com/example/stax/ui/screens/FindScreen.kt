package com.example.stax.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.stax.R
import com.example.stax.data.CardRoomRepository
import com.example.stax.data.CardRoomWithDistance
import com.example.stax.ui.composables.StaxScreenHeader
import com.example.stax.ui.theme.StaxHeaderGradient
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.roundToInt

private enum class SearchMode { NEAR_ME, BY_STATE, FAVORITES }

private sealed class FindResult {
    data class NearbyResult(val items: List<CardRoomWithDistance>) : FindResult()
    data class StateResult(val items: List<CardRoomWithDistance>, val stateName: String) : FindResult()
    data class FavoritesResult(val items: List<CardRoomWithDistance>) : FindResult()
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun FindScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { CardRoomRepository(context) }
    val availableStates = remember { repo.availableStates }

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var result by remember { mutableStateOf<FindResult?>(null) }
    var hasSearched by remember { mutableStateOf(false) }
    var cachedLocation by remember { mutableStateOf<Location?>(null) }
    var locationFetched by remember { mutableStateOf(false) }
    var detectedState by remember { mutableStateOf<String?>(null) }

    var searchMode by remember { mutableStateOf(SearchMode.NEAR_ME) }
    var radiusMiles by remember { mutableFloatStateOf(100f) }
    var selectedState by remember { mutableStateOf(availableStates.firstOrNull() ?: "California") }
    var stateDropdownExpanded by remember { mutableStateOf(false) }

    var favorites by remember { mutableStateOf(repo.getFavorites()) }
    var homeCasino by remember { mutableStateOf(repo.getHomeCasino()) }

    fun sortedItems(items: List<CardRoomWithDistance>): List<CardRoomWithDistance> =
        CardRoomRepository.sortWithFavorites(items, favorites, homeCasino)

    fun searchNearbyWithCachedLocation(location: Location, radius: Float) {
        scope.launch {
            val items = withContext(Dispatchers.Default) {
                repo.searchNearby(location.latitude, location.longitude, radius.toDouble())
            }
            result = FindResult.NearbyResult(items)
            hasSearched = true
        }
    }

    fun performStateSearch(state: String) {
        scope.launch {
            val loc = cachedLocation
            val items = withContext(Dispatchers.Default) {
                repo.searchByState(state, loc?.latitude, loc?.longitude)
            }
            result = FindResult.StateResult(items, state)
            hasSearched = true
        }
    }

    fun loadFavorites() {
        scope.launch {
            val loc = cachedLocation
            val items = withContext(Dispatchers.Default) {
                val favs = repo.getFavoriteRooms(loc?.latitude, loc?.longitude)
                CardRoomRepository.sortWithFavorites(favs, favorites, homeCasino)
            }
            result = FindResult.FavoritesResult(items)
            hasSearched = true
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted && !locationFetched) {
            val location = getCurrentLocation(context)
            if (location != null) {
                cachedLocation = location
                locationFetched = true

                val userState = repo.getStateFromLocation(location.latitude, location.longitude)
                if (userState != null && availableStates.any { it.equals(userState, ignoreCase = true) }) {
                    val match = availableStates.first { it.equals(userState, ignoreCase = true) }
                    detectedState = match
                    selectedState = match
                }

                val items = withContext(Dispatchers.Default) {
                    repo.searchNearby(location.latitude, location.longitude, radiusMiles.toDouble())
                }
                result = FindResult.NearbyResult(items)
                hasSearched = true
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { radiusMiles }
            .debounce(250)
            .distinctUntilChanged()
            .collect { radius ->
                val loc = cachedLocation
                if (loc != null && searchMode == SearchMode.NEAR_ME) {
                    val items = withContext(Dispatchers.Default) {
                        repo.searchNearby(loc.latitude, loc.longitude, radius.toDouble())
                    }
                    result = FindResult.NearbyResult(items)
                    hasSearched = true
                }
            }
    }

    LaunchedEffect(searchMode) {
        when (searchMode) {
            SearchMode.BY_STATE -> performStateSearch(selectedState)
            SearchMode.NEAR_ME -> if (cachedLocation != null) searchNearbyWithCachedLocation(cachedLocation!!, radiusMiles)
            SearchMode.FAVORITES -> loadFavorites()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(StaxHeaderGradient)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(88.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StaxScreenHeader(
                title = "Find Card Rooms",
                subtitle = "Discover nearby poker action"
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        SearchModeToggle(searchMode, Modifier.padding(horizontal = 20.dp)) { searchMode = it }
        Spacer(modifier = Modifier.height(12.dp))

        if (!locationPermissions.allPermissionsGranted && searchMode == SearchMode.NEAR_ME) {
            PermissionRequest(
                onRequestPermission = { locationPermissions.launchMultiplePermissionRequest() }
            )
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                when (searchMode) {
                    SearchMode.NEAR_ME -> {
                        RadiusSelector(
                            radiusMiles = radiusMiles,
                            onRadiusChange = { radiusMiles = it }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    SearchMode.BY_STATE -> {
                        StateSelector(
                            selectedState = selectedState,
                            expanded = stateDropdownExpanded,
                            onExpandedChange = { stateDropdownExpanded = it },
                            onStateSelected = {
                                selectedState = it
                                stateDropdownExpanded = false
                                performStateSearch(it)
                            },
                            availableStates = availableStates
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    SearchMode.FAVORITES -> {}
                }

                if (hasSearched) {
                    val displayItems = when (val r = result) {
                        is FindResult.NearbyResult -> sortedItems(r.items)
                        is FindResult.StateResult -> sortedItems(r.items)
                        is FindResult.FavoritesResult -> r.items
                        null -> emptyList()
                    }
                    val showDistance = cachedLocation != null

                    if (displayItems.isEmpty()) {
                        if (searchMode == SearchMode.FAVORITES) {
                            EmptyFavorites()
                        } else {
                            EmptyResults(
                                searchMode = searchMode,
                                radiusMiles = radiusMiles.roundToInt(),
                                stateName = selectedState
                            )
                        }
                    } else {
                        Text(
                            if (searchMode == SearchMode.FAVORITES) "${displayItems.size} favorites"
                            else "${displayItems.size} card rooms found",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(displayItems, key = { it.room.address }) { item ->
                                val isFav = item.room.address in favorites
                                val isHome = item.room.address == homeCasino
                                CardRoomItem(
                                    name = item.room.name,
                                    address = item.room.address,
                                    city = item.room.city,
                                    state = item.room.state,
                                    logo = item.room.logo,
                                    distanceMiles = if (showDistance) item.distanceMiles else null,
                                    latitude = item.room.latitude,
                                    longitude = item.room.longitude,
                                    isFavorite = isFav,
                                    isHomeCasino = isHome,
                                    onToggleFavorite = {
                                        favorites = repo.toggleFavorite(item.room.address)
                                        homeCasino = repo.getHomeCasino()
                                        if (searchMode == SearchMode.FAVORITES) loadFavorites()
                                    },
                                    onToggleHome = {
                                        homeCasino = if (isHome) {
                                            repo.setHomeCasino(null)
                                        } else {
                                            repo.setHomeCasino(item.room.address)
                                        }
                                        favorites = repo.getFavorites()
                                        if (searchMode == SearchMode.FAVORITES) loadFavorites()
                                    },
                                    context = context
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchModeToggle(current: SearchMode, modifier: Modifier = Modifier, onChanged: (SearchMode) -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = current == SearchMode.NEAR_ME,
            onClick = { onChanged(SearchMode.NEAR_ME) },
            label = { Text("Near Me") },
            leadingIcon = {
                if (current == SearchMode.NEAR_ME) {
                    Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = current == SearchMode.BY_STATE,
            onClick = { onChanged(SearchMode.BY_STATE) },
            label = { Text("By State") },
            leadingIcon = {
                if (current == SearchMode.BY_STATE) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = current == SearchMode.FAVORITES,
            onClick = { onChanged(SearchMode.FAVORITES) },
            label = { Text("Favorites") },
            leadingIcon = {
                if (current == SearchMode.FAVORITES) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RadiusSelector(
    radiusMiles: Float,
    onRadiusChange: (Float) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Search radius",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${radiusMiles.roundToInt()} mi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = radiusMiles,
                onValueChange = onRadiusChange,
                valueRange = 10f..200f,
                steps = 0,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("10 mi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("200 mi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StateSelector(
    selectedState: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onStateSelected: (String) -> Unit,
    availableStates: List<String>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { onExpandedChange(!expanded) }
            ) {
                OutlinedTextField(
                    value = selectedState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("State") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    availableStates.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(state) },
                            onClick = { onStateSelected(state) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardRoomItem(
    name: String,
    address: String,
    city: String,
    state: String,
    logo: String?,
    distanceMiles: Double?,
    latitude: Double,
    longitude: Double,
    isFavorite: Boolean,
    isHomeCasino: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHome: () -> Unit,
    context: Context
) {
    val logoBitmap = remember(logo) {
        if (logo != null) {
            try {
                val resName = "logo_" + logo.removeSuffix(".png").replace('-', '_')
                val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (id != 0) BitmapFactory.decodeResource(context.resources, id)?.asImageBitmap()
                else null
            } catch (_: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (logoBitmap != null) {
                Image(
                    bitmap = logoBitmap,
                    contentDescription = "$name logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isHomeCasino) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "HOME",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (distanceMiles != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NearMe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formatDistance(distanceMiles),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favorite" else "Add favorite",
                    tint = if (isFavorite) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onToggleHome,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    if (isHomeCasino) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = if (isHomeCasino) "Remove home casino" else "Set as home casino",
                    tint = if (isHomeCasino) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { openDirections(context, latitude, longitude) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    Icons.Default.Directions,
                    contentDescription = "Directions",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Location Access Needed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Stax needs your location to find card rooms and casinos near you. " +
                            "Your location is only used for this search and is never stored.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                androidx.compose.material3.Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Enable Location")
                }
            }
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Favorites Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap the heart on any card room to add it to your favorites.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyResults(
    searchMode: SearchMode,
    radiusMiles: Int,
    stateName: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.NearMe,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Card Rooms Found",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (searchMode == SearchMode.NEAR_ME) {
                        "No card rooms found within $radiusMiles miles. Try increasing the radius or search by state."
                    } else {
                        "No card rooms listed for $stateName yet. Try a different state."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDistance(miles: Double): String {
    return if (miles < 0.1) "Nearby"
    else if (miles < 10) String.format("%.1f mi", miles)
    else String.format("%.0f mi", miles)
}

private fun openDirections(context: Context, latitude: Double, longitude: Double) {
    val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val browserUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
        )
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
    }
}

@SuppressLint("MissingPermission")
private suspend fun getCurrentLocation(context: Context): Location? =
    suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location -> cont.resume(location) }
            .addOnFailureListener { cont.resume(null) }
        cont.invokeOnCancellation { cts.cancel() }
    }
