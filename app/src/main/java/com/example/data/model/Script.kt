package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class Script(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val wordCount: Int = 0,
    val estimatedSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSample: Boolean = false
) {
    companion object {
        fun calculateWordCount(text: String): Int {
            if (text.isBlank()) return 0
            // Supports both Latin whitespace and Indic language word boundaries
            return text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        }

        fun calculateEstimatedSeconds(wordCount: Int, wordsPerMinute: Int = 130): Int {
            if (wordCount <= 0) return 0
            return ((wordCount.toDouble() / wordsPerMinute) * 60).toInt().coerceAtLeast(5)
        }
    }
}
