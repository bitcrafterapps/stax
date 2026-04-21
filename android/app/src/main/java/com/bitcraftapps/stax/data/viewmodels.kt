package com.bitcraftapps.stax.data

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CasinoFoldersViewModel(
    private val dao: StaxDao,
    private val application: Application
) : ViewModel() {
    private val homeGameRepository = HomeGameRepository.getInstance(application)
    val casinoFolders: StateFlow<List<CasinoFolder>> = dao.getCasinoFolders()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    // Maps casinoName → drawable resource name (e.g. "Commerce Casino" → "logo_commerce_casino")
    private val _logoMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val logoMap: StateFlow<Map<String, String>> = _logoMap.asStateFlow()
    val homeGames: StateFlow<List<HomeGameVenue>> = homeGameRepository.homeGames

    init {
        loadCasinoData()
        loadLogoMap()
    }

    private fun loadCasinoData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val json = application.assets.open("cardrooms.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<CardRoom>>() {}.type
                    val rooms: List<CardRoom> = Gson().fromJson(json, type)
                    val data = rooms
                        .groupBy { it.state }
                        .mapValues { (_, roomsInState) -> roomsInState.map { it.name }.sorted() }
                        .toSortedMap()
                    _casinoData.value = data
                } catch (_: Exception) {}
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
                    val map = rooms.mapNotNull { room ->
                        room.logo?.let { logo ->
                            val resName = "logo_" + logo.removeSuffix(".png").replace('-', '_')
                            room.name to resName
                        }
                    }.toMap()
                    _logoMap.value = map
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addSession(
        name: String,
        casinoName: String,
        sessionType: String,
        game: String,
        gameType: String,
        stakes: String,
        antes: String,
        buyIn: Double,
        cashOut: Double
    ) {
        viewModelScope.launch {
            val newSession = Session(
                name = name,
                casinoName = casinoName,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                type = sessionType,
                game = game,
                gameType = gameType,
                stakes = stakes,
                antes = antes,
                buyInAmount = buyIn,
                cashOutAmount = cashOut
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

    fun saveHomeGame(name: String, city: String, state: String) {
        homeGameRepository.addOrUpdate(name, city, state)
    }
}

class CasinoSessionsViewModel(
    private val dao: StaxDao,
    private val casinoName: String,
    private val application: Application
) : ViewModel() {
    val sessions: StateFlow<List<SessionWithLatestPhoto>> = dao.getSessionsWithLatestPhotoForCasino(casinoName)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _logoResName = MutableStateFlow<String?>(null)
    val logoResName: StateFlow<String?> = _logoResName.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val json = application.assets.open("cardrooms.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<CardRoom>>() {}.type
                    val rooms: List<CardRoom> = Gson().fromJson(json, type)
                    val room = rooms.find { it.name == casinoName }
                    _logoResName.value = room?.logo?.let { logo ->
                        "logo_" + logo.removeSuffix(".png").replace('-', '_')
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            val photos = dao.getPhotosForSessionOnce(sessionId)
            photos.forEach { photo ->
                val file = File(photo.imagePath)
                if (file.exists()) file.delete()
            }
            dao.deletePhotosForSession(sessionId)
            dao.deleteSession(sessionId)
        }
    }
}

class CasinoSessionsViewModelFactory(
    private val dao: StaxDao,
    private val casinoName: String,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CasinoSessionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CasinoSessionsViewModel(dao, casinoName, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SessionsViewModel(
    private val dao: StaxDao,
    private val application: Application
) : ViewModel() {
    private val homeGameRepository = HomeGameRepository.getInstance(application)
    val sessions = dao.getAllSessionsWithDetails()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _casinoData = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val casinoData: StateFlow<Map<String, List<String>>> = _casinoData.asStateFlow()

    private val _logoMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val logoMap: StateFlow<Map<String, String>> = _logoMap.asStateFlow()
    val homeGames: StateFlow<List<HomeGameVenue>> = homeGameRepository.homeGames

    init {
        loadCasinoData()
        loadLogoMap()
    }

    private fun loadCasinoData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val json = application.assets.open("cardrooms.json").bufferedReader().use { it.readText() }
                    val type = object : TypeToken<List<CardRoom>>() {}.type
                    val rooms: List<CardRoom> = Gson().fromJson(json, type)
                    val data = rooms
                        .groupBy { it.state }
                        .mapValues { (_, roomsInState) -> roomsInState.map { it.name }.sorted() }
                        .toSortedMap()
                    _casinoData.value = data
                } catch (_: Exception) {}
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
                    val map = rooms.mapNotNull { room ->
                        room.logo?.let { logo ->
                            val resName = "logo_" + logo.removeSuffix(".png").replace('-', '_')
                            room.name to resName
                        }
                    }.toMap()
                    _logoMap.value = map
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun addSession(
        name: String,
        casinoName: String,
        date: String,
        type: String,
        game: String,
        gameType: String,
        stakes: String,
        antes: String,
        buyIn: Double,
        cashOut: Double
    ) {
        viewModelScope.launch {
            val newSession = Session(
                name = name,
                casinoName = casinoName,
                date = date,
                type = type,
                game = game,
                gameType = gameType,
                stakes = stakes,
                antes = antes,
                buyInAmount = buyIn,
                cashOutAmount = cashOut
            )
            dao.insertSession(newSession)
        }
    }

    fun saveHomeGame(name: String, city: String, state: String) {
        homeGameRepository.addOrUpdate(name, city, state)
    }
}

class PhotoGalleryViewModel(
    private val dao: StaxDao,
    private val sessionId: Long,
    private val application: Application
) : ViewModel() {
    private val captionPrefs = application.getSharedPreferences("photo_captions", Context.MODE_PRIVATE)

    val photos: StateFlow<List<Photo>> = dao.getPhotosForSession(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val session: StateFlow<Session?> = dao.getSessionById(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _captions = MutableStateFlow(loadCaptions())
    val captions: StateFlow<Map<Long, String>> = _captions.asStateFlow()
    private val _editVersions = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val editVersions: StateFlow<Map<Long, Int>> = _editVersions.asStateFlow()

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
            captionPrefs.edit().remove(captionKey(photo.id)).apply()
            _captions.value = _captions.value - photo.id
            dao.deletePhoto(photo)
        }
    }

    fun savePhotoEdits(photo: Photo, editedBitmap: Bitmap, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            FileOutputStream(photo.imagePath).use { out ->
                editedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            val trimmedCaption = caption.trim()
            if (trimmedCaption.isBlank()) {
                captionPrefs.edit().remove(captionKey(photo.id)).apply()
                _captions.value = _captions.value - photo.id
            } else {
                captionPrefs.edit().putString(captionKey(photo.id), trimmedCaption).apply()
                _captions.value = _captions.value + (photo.id to trimmedCaption)
            }
            _editVersions.value = _editVersions.value + (photo.id to ((_editVersions.value[photo.id] ?: 0) + 1))
        }
    }

    private fun saveImagePermanently(uri: Uri): File {
        val inputStream = application.contentResolver.openInputStream(uri)
        val timestamp = System.currentTimeMillis()
        val newFile = File(application.getExternalFilesDir(null), "IMG_$timestamp.jpg")
        val outputStream = FileOutputStream(newFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        saveToDeviceGallery(newFile)

        return newFile
    }

    private fun saveToDeviceGallery(file: File) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stax")
            }
            val contentUri = application.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
            contentUri?.let { galleryUri ->
                application.contentResolver.openOutputStream(galleryUri)?.use { out ->
                    FileInputStream(file).use { input -> input.copyTo(out) }
                }
            }
        } catch (_: Exception) { }
    }

    private fun loadCaptions(): Map<Long, String> =
        captionPrefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith("caption_")) return@mapNotNull null
            val id = key.removePrefix("caption_").toLongOrNull() ?: return@mapNotNull null
            val caption = value as? String ?: return@mapNotNull null
            id to caption
        }.toMap()

    private fun captionKey(photoId: Long): String = "caption_$photoId"
} 