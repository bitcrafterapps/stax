package com.example.stax.data

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DashboardViewModel(private val dao: StaxDao) : ViewModel() {
    val sessions: StateFlow<List<SessionWithLatestPhoto>> = dao.getSessionsWithLatestPhoto()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSession(casinoName: String, sessionType: String, gameType: String) {
        viewModelScope.launch {
            dao.insertSession(
                Session(
                    name = casinoName,
                    date = System.currentTimeMillis(),
                    sessionType = sessionType,
                    gameType = gameType
                )
            )
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

class PhotoGalleryViewModel(
    private val dao: StaxDao,
    private val sessionId: Long,
    private val application: Application
) : ViewModel() {
    val photos: StateFlow<List<Photo>> = dao.getPhotosForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            val permanentFile = saveImagePermanently(uri)
            dao.insertPhoto(Photo(sessionId = sessionId, imagePath = permanentFile.absolutePath))
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