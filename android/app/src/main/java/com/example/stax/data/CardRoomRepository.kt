package com.example.stax.data

import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.*

data class CardRoom(
    val name: String,
    val city: String,
    val state: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val logo: String? = null
)

class CardRoomRepository(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences("stax_cardrooms", Context.MODE_PRIVATE)
    }

    private val allRooms: List<CardRoom> by lazy {
        val json = context.assets.open("cardrooms.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<CardRoom>>() {}.type
        val raw: List<CardRoom> = Gson().fromJson(json, type)
        raw.distinctBy { it.address.lowercase(Locale.US) }
    }

    fun searchByState(
        stateName: String,
        userLat: Double? = null,
        userLng: Double? = null
    ): List<CardRoomWithDistance> {
        val rooms = allRooms.filter { it.state.equals(stateName, ignoreCase = true) }
        return if (userLat != null && userLng != null) {
            rooms.map { room ->
                CardRoomWithDistance(room, haversineDistanceMiles(userLat, userLng, room.latitude, room.longitude))
            }.sortedBy { it.distanceMiles }
        } else {
            rooms.sortedBy { it.city }.map { CardRoomWithDistance(it, 0.0) }
        }
    }

    fun searchNearby(
        latitude: Double,
        longitude: Double,
        radiusMiles: Double
    ): List<CardRoomWithDistance> =
        allRooms.mapNotNull { room ->
            val dist = haversineDistanceMiles(latitude, longitude, room.latitude, room.longitude)
            if (dist <= radiusMiles) CardRoomWithDistance(room, dist) else null
        }.sortedBy { it.distanceMiles }

    val availableStates: List<String> by lazy {
        allRooms.map { it.state }.distinct().sorted()
    }

    fun getFavorites(): Set<String> =
        prefs.getStringSet("favorites", emptySet()) ?: emptySet()

    fun getFavoriteRooms(
        userLat: Double? = null,
        userLng: Double? = null
    ): List<CardRoomWithDistance> {
        val favAddresses = getFavorites()
        if (favAddresses.isEmpty()) return emptyList()
        val rooms = allRooms.filter { it.address in favAddresses }
        return rooms.map { room ->
            val dist = if (userLat != null && userLng != null)
                haversineDistanceMiles(userLat, userLng, room.latitude, room.longitude)
            else 0.0
            CardRoomWithDistance(room, dist)
        }
    }

    fun toggleFavorite(address: String): Set<String> {
        val current = getFavorites().toMutableSet()
        if (current.contains(address)) current.remove(address) else current.add(address)
        prefs.edit().putStringSet("favorites", current).apply()
        if (!current.contains(address) && getHomeCasino() == address) {
            prefs.edit().remove("home_casino").apply()
        }
        return current
    }

    fun getHomeCasino(): String? =
        prefs.getString("home_casino", null)

    fun setHomeCasino(address: String?): String? {
        if (address == null) {
            prefs.edit().remove("home_casino").apply()
        } else {
            prefs.edit().putString("home_casino", address).apply()
            val favs = getFavorites().toMutableSet()
            favs.add(address)
            prefs.edit().putStringSet("favorites", favs).apply()
        }
        return address
    }

    @Suppress("DEPRECATION")
    suspend fun getStateFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.US)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull()?.adminArea)
                    }
                }
            } else {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                addresses?.firstOrNull()?.adminArea
            }
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        fun haversineDistanceMiles(
            lat1: Double, lon1: Double,
            lat2: Double, lon2: Double
        ): Double {
            val earthRadiusMiles = 3958.8
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            val c = 2 * asin(sqrt(a))
            return earthRadiusMiles * c
        }

        fun sortWithFavorites(
            items: List<CardRoomWithDistance>,
            favorites: Set<String>,
            homeCasino: String?
        ): List<CardRoomWithDistance> {
            return items.sortedWith(compareBy<CardRoomWithDistance> {
                when (it.room.address) {
                    homeCasino -> 0
                    in favorites -> 1
                    else -> 2
                }
            }.thenBy { it.distanceMiles })
        }
    }
}

data class CardRoomWithDistance(
    val room: CardRoom,
    val distanceMiles: Double
)
