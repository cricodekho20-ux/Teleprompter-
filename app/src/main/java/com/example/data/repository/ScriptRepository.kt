package com.example.data.repository

import com.example.data.local.ScriptDao
import com.example.data.model.Script
import kotlinx.coroutines.flow.Flow

class ScriptRepository(private val scriptDao: ScriptDao) {
    val allScripts: Flow<List<Script>> = scriptDao.getAllScripts()

    fun searchScripts(query: String): Flow<List<Script>> {
        return scriptDao.searchScripts(query)
    }

    suspend fun getScriptById(id: Long): Script? {
        return scriptDao.getScriptById(id)
    }

    suspend fun saveScript(title: String, content: String, existingId: Long? = null): Long {
        val wordCount = Script.calculateWordCount(content)
        val estimatedSeconds = Script.calculateEstimatedSeconds(wordCount)
        val now = System.currentTimeMillis()
        
        val script = if (existingId != null && existingId > 0) {
            Script(
                id = existingId,
                title = title.ifBlank { "Untitled Script" },
                content = content,
                wordCount = wordCount,
                estimatedSeconds = estimatedSeconds,
                updatedAt = now
            )
        } else {
            Script(
                title = title.ifBlank { "Untitled Script" },
                content = content,
                wordCount = wordCount,
                estimatedSeconds = estimatedSeconds,
                createdAt = now,
                updatedAt = now
            )
        }
        return scriptDao.insertScript(script)
    }

    suspend fun duplicateScript(script: Script): Long {
        val copy = script.copy(
            id = 0,
            title = "${script.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isSample = false
        )
        return scriptDao.insertScript(copy)
    }

    suspend fun deleteScript(id: Long) {
        scriptDao.deleteScriptById(id)
    }

    suspend fun clearAllScripts() {
        scriptDao.deleteAllScripts()
    }
}
