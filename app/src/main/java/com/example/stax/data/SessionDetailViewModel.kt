package com.example.stax.data

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

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = staxDao.getSessionById(sessionId).first()
        }
        loadCasinoData()
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