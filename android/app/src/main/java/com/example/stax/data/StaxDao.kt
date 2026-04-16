package com.example.stax.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StaxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Query("SELECT s.casinoName, COUNT(s.id) as sessionCount, (SELECT p.imagePath FROM photos p WHERE p.sessionId = s.id ORDER BY p.id DESC LIMIT 1) as latestPhotoPath FROM sessions s GROUP BY s.casinoName")
    fun getCasinoFolders(): Flow<List<CasinoFolder>>

    @Query("SELECT s.*, (SELECT COUNT(*) FROM photos p WHERE p.sessionId = s.id) as photoCount, (SELECT imagePath FROM photos p WHERE p.sessionId = s.id ORDER BY id DESC LIMIT 1) as latestPhotoPath FROM sessions s ORDER BY s.id DESC")
    fun getSessionsWithLatestPhoto(): Flow<List<SessionWithLatestPhoto>>

    @Query("SELECT s.*, (SELECT COUNT(*) FROM photos p WHERE p.sessionId = s.id) as photoCount, (SELECT imagePath FROM photos p WHERE p.sessionId = s.id ORDER BY id DESC LIMIT 1) as latestPhotoPath FROM sessions s WHERE s.casinoName = :casinoName ORDER BY s.id DESC")
    fun getSessionsWithLatestPhotoForCasino(casinoName: String): Flow<List<SessionWithLatestPhoto>>

    @Query("SELECT * FROM sessions WHERE id = :sessionId")
    fun getSessionById(sessionId: Long): Flow<Session?>

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessionsWithDetails(): Flow<List<Session>>

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: Photo)

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId ORDER BY id DESC")
    fun getPhotosForSession(sessionId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId")
    suspend fun getPhotosForSessionOnce(sessionId: Long): List<Photo>

    @Update
    suspend fun updatePhoto(photo: Photo)

    @Delete
    suspend fun deletePhoto(photo: Photo)

    @Query("DELETE FROM photos WHERE sessionId = :sessionId")
    suspend fun deletePhotosForSession(sessionId: Long)

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHand(hand: Hand)

    @Update
    suspend fun updateHand(hand: Hand)

    @Delete
    suspend fun deleteHand(hand: Hand)

    @Query("SELECT * FROM hands WHERE sessionId = :sessionId ORDER BY id DESC")
    fun getHandsForSession(sessionId: Long): Flow<List<Hand>>
} 