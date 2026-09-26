package com.example.data.repository

import com.example.data.local.VideoDao
import com.example.data.model.RecordedVideo
import kotlinx.coroutines.flow.Flow
import java.io.File

class VideoRepository(private val videoDao: VideoDao) {
    val allVideos: Flow<List<RecordedVideo>> = videoDao.getAllVideos()

    suspend fun getVideoById(id: Long): RecordedVideo? {
        return videoDao.getVideoById(id)
    }

    suspend fun saveVideo(
        title: String,
        filePath: String,
        uriString: String = "",
        durationMs: Long,
        aspectRatio: String,
        resolution: String = "1080p",
        fileSizeBytes: Long = 0L
    ): Long {
        val video = RecordedVideo(
            title = title,
            filePath = filePath,
            uriString = uriString,
            durationMs = durationMs,
            aspectRatio = aspectRatio,
            resolution = resolution,
            fileSizeBytes = fileSizeBytes,
            createdAt = System.currentTimeMillis()
        )
        return videoDao.insertVideo(video)
    }

    suspend fun deleteVideo(video: RecordedVideo) {
        try {
            val file = File(video.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        videoDao.deleteVideo(video)
    }

    suspend fun clearAllVideos() {
        videoDao.deleteAllVideos()
    }

    suspend fun getTotalStorageUsedBytes(): Long {
        return videoDao.getTotalStorageUsedBytes() ?: 0L
    }
}
