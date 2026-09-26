package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.RecordedVideo
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM recorded_videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<RecordedVideo>>

    @Query("SELECT * FROM recorded_videos WHERE id = :id LIMIT 1")
    suspend fun getVideoById(id: Long): RecordedVideo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: RecordedVideo): Long

    @Update
    suspend fun updateVideo(video: RecordedVideo)

    @Delete
    suspend fun deleteVideo(video: RecordedVideo)

    @Query("DELETE FROM recorded_videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)

    @Query("DELETE FROM recorded_videos")
    suspend fun deleteAllVideos()

    @Query("SELECT SUM(fileSizeBytes) FROM recorded_videos")
    suspend fun getTotalStorageUsedBytes(): Long?
}
