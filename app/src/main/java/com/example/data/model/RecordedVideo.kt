package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recorded_videos")
data class RecordedVideo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val uriString: String = "",
    val durationMs: Long = 0L,
    val aspectRatio: String = "9:16",
    val resolution: String = "1080p",
    val fileSizeBytes: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
