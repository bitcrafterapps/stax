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
    val casinoName: String = "",
    val date: String,
    val type: String,
    val game: String,
    val gameType: String = "",
    val stakes: String = "",
    val antes: String = "",
    val timeIn: String = "",
    val timeOut: String = "",
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

data class CasinoFolder(
    val casinoName: String,
    val sessionCount: Int,
    val latestPhotoPath: String?
)

data class VillainCards(
    val card1Rank: String = "",
    val card1Suit: String = "",
    val card2Rank: String = "",
    val card2Suit: String = ""
)

@Entity(tableName = "hands")
data class Hand(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: String = "",
    val holeCard1Rank: String = "",
    val holeCard1Suit: String = "",
    val holeCard2Rank: String = "",
    val holeCard2Suit: String = "",
    val position: String = "",
    val result: String = "",   // "Won", "Lost", "Folded"
    val notes: String = "",
    val isStarred: Boolean = false,
    val villainsJson: String = "[]"   // JSON array of VillainCards
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