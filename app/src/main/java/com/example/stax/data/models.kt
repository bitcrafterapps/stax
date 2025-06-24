package com.example.stax.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val date: String,
    val type: String,
    val game: String,
    val buyInAmount: Double = 0.0,
    val cashOutAmount: Double = 0.0,
    val photos: List<Photo> = emptyList(),
    val notes: String = ""
)

data class SessionWithPhotoCount(
    @Embedded
    val session: Session,
    val photoCount: Int
)

data class SessionWithLatestPhoto(
    @Embedded
    val session: Session,
    val photoCount: Int,
    val latestPhotoPath: String?
)

@Parcelize
@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val imagePath: String,
    val rating: Int = 0,
    val dateCreated: Long
) : Parcelable 