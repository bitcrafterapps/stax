package com.example.stax.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val staxDao: StaxDao,
    private val sessionId: Long
) : ViewModel() {

    private val _session = MutableStateFlow<Session?>(null)
    val session = _session.asStateFlow()

    init {
        viewModelScope.launch {
            _session.value = staxDao.getSessionById(sessionId).first()
        }
    }

    fun updateSession(
        name: String,
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
    private val sessionId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionDetailViewModel(staxDao, sessionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 