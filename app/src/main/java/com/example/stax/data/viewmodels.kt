package com.example.stax.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val dao: StaxDao,
    private val application: Application
) : ViewModel() {
    val sessions: StateFlow<List<SessionWithLatestPhoto>> = dao.getSessionsWithLatestPhoto()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    init {
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

    fun addSession(casinoName: String, sessionType: String, gameType: String) {
        viewModelScope.launch {
            val newSession = Session(
                name = casinoName,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                type = sessionType,
                game = gameType
            )
            dao.insertSession(newSession)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val photos = dao.getPhotosForSessionOnce(sessionId)
            photos.forEach { photo ->
                val file = File(photo.imagePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            dao.deletePhotosForSession(sessionId)
            dao.deleteSession(sessionId)
        }
    }
}

class SessionsViewModel(
    private val dao: StaxDao,
    private val application: Application
) : ViewModel() {
    val sessions = dao.getAllSessionsWithDetails()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    init {
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

    fun addSession(
        casinoName: String,
        date: String,
        type: String,
        game: String,
        buyIn: Double,
        cashOut: Double
    ) {
        viewModelScope.launch {
            val newSession = Session(
                name = casinoName,
                date = date,
                type = type,
                game = game,
                buyInAmount = buyIn,
                cashOutAmount = cashOut
            )
            dao.insertSession(newSession)
        }
    }
}

class PhotoGalleryViewModel(
    private val dao: StaxDao,
    private val sessionId: Long,
    private val application: Application
) : ViewModel() {
    val photos: StateFlow<List<Photo>> = dao.getPhotosForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val session: StateFlow<Session?> = dao.getSessionById(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val permanentFile = saveImagePermanently(uri)
            dao.insertPhoto(Photo(sessionId = sessionId, imagePath = permanentFile.absolutePath, dateCreated = System.currentTimeMillis()))
        }
    }

    fun updatePhoto(photo: Photo) {
        viewModelScope.launch {
            dao.updatePhoto(photo)
        }
    }

    fun deletePhoto(photo: Photo) {
        viewModelScope.launch {
            val file = File(photo.imagePath)
            if (file.exists()) {
                file.delete()
            }
            dao.deletePhoto(photo)
        }
    }

    private fun saveImagePermanently(uri: Uri): File {
        val inputStream = application.contentResolver.openInputStream(uri)
        val newFile = File(application.getExternalFilesDir(null), "IMG_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(newFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return newFile
    }
} 