package com.bitcraftapps.stax.data

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

class SessionDetailViewModel(
    private val staxDao: StaxDao,
    private val sessionId: Long,
    private val application: Application
) : ViewModel() {

    private val _session = MutableStateFlow<Session?>(null)
    val session = _session.asStateFlow()

    val hands: kotlinx.coroutines.flow.Flow<List<Hand>> = staxDao.getHandsForSession(sessionId)

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    private val _logoMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val logoMap: StateFlow<Map<String, String>> = _logoMap.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = staxDao.getSessionById(sessionId).first()
        }
        loadCasinoData()
        loadLogoMap()
    }

    private fun loadCasinoData() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    application.assets.open("casinos.json").use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val type = object : TypeToken<Map<String, List<String>>>() {}.type
                            val data: Map<String, List<String>> = Gson().fromJson(reader, type)
                            _casinoData.value = data
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadLogoMap() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val json = application.assets.open("cardrooms.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<CardRoom>>() {}.type
                    val rooms: List<CardRoom> = Gson().fromJson(json, type)
                    _logoMap.value = rooms.mapNotNull { room ->
                        room.logo?.let { logo ->
                            room.name to "logo_" + logo.removeSuffix(".png").replace('-', '_')
                        }
                    }.toMap()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addHand(
        holeCard1Rank: String, holeCard1Suit: String,
        holeCard2Rank: String, holeCard2Suit: String,
        position: String, result: String, notes: String,
        villains: List<VillainCards> = emptyList()
    ) {
        viewModelScope.launch {
            val formatter = java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.US)
            val villainsJson = com.google.gson.Gson().toJson(villains)
            staxDao.insertHand(
                Hand(
                    sessionId = sessionId,
                    timestamp = formatter.format(java.util.Date()),
                    holeCard1Rank = holeCard1Rank, holeCard1Suit = holeCard1Suit,
                    holeCard2Rank = holeCard2Rank, holeCard2Suit = holeCard2Suit,
                    position = position, result = result, notes = notes,
                    villainsJson = villainsJson
                )
            )
        }
    }

    fun parseVillains(hand: Hand): List<VillainCards> = try {
        val type = object : com.google.gson.reflect.TypeToken<List<VillainCards>>() {}.type
        com.google.gson.Gson().fromJson(hand.villainsJson, type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    fun toggleStarHand(hand: Hand) {
        viewModelScope.launch { staxDao.updateHand(hand.copy(isStarred = !hand.isStarred)) }
    }

    fun deleteHand(hand: Hand) {
        viewModelScope.launch { staxDao.deleteHand(hand) }
    }

    fun updateSession(
        name: String,
        casinoName: String,
        date: String,
        timeIn: String,
        timeOut: String,
        type: String,
        game: String,
        gameType: String,
        stakes: String,
        antes: String,
        buyInAmount: Double,
        cashOutAmount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val currentSession = _session.value ?: return@launch
            val updatedSession = currentSession.copy(
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
                buyInAmount = buyInAmount,
                cashOutAmount = cashOutAmount,
                notes = notes
            )
            staxDao.insertSession(updatedSession)
            _session.value = updatedSession
        }
    }
}

class SessionDetailViewModelFactory(
    private val staxDao: StaxDao,
    private val sessionId: Long,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionDetailViewModel(staxDao, sessionId, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 