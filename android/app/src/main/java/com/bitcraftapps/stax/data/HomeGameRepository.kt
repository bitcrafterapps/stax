package com.bitcraftapps.stax.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeGameVenue(
    val name: String,
    val city: String,
    val state: String
) {
    val displayName: String get() = "$name · $city, $state"
}

class HomeGameRepository private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("stax_home_games", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _homeGames = MutableStateFlow(load())
    val homeGames: StateFlow<List<HomeGameVenue>> = _homeGames.asStateFlow()

    fun addOrUpdate(name: String, city: String, state: String) {
        val trimmedName = name.trim()
        val trimmedCity = city.trim()
        val trimmedState = state.trim()
        if (trimmedName.isBlank() || trimmedCity.isBlank() || trimmedState.isBlank()) return

        val updated = _homeGames.value
            .filterNot {
                it.name.equals(trimmedName, ignoreCase = true) &&
                    it.city.equals(trimmedCity, ignoreCase = true) &&
                    it.state.equals(trimmedState, ignoreCase = true)
            } + HomeGameVenue(
            name = trimmedName,
            city = trimmedCity,
            state = trimmedState
        )

        _homeGames.value = updated.sortedWith(
            compareBy<HomeGameVenue> { it.name.lowercase() }
                .thenBy { it.city.lowercase() }
                .thenBy { it.state.lowercase() }
        )
        save()
    }

    private fun save() {
        prefs.edit().putString("items", gson.toJson(_homeGames.value)).apply()
    }

    private fun load(): List<HomeGameVenue> = try {
        val json = prefs.getString("items", null) ?: return emptyList()
        val type = object : TypeToken<List<HomeGameVenue>>() {}.type
        gson.fromJson<List<HomeGameVenue>>(json, type).orEmpty()
    } catch (_: Exception) {
        emptyList()
    }

    companion object {
        @Volatile
        private var INSTANCE: HomeGameRepository? = null

        fun getInstance(context: Context): HomeGameRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HomeGameRepository(context).also { INSTANCE = it }
            }
        }
    }
}
