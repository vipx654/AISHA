package com.aisha.data.local

import android.content.Context
import android.content.SharedPreferences
import com.aisha.domain.model.ConversationEntry
import com.aisha.domain.model.DayLog
import com.aisha.domain.model.Memory
import com.aisha.domain.model.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aisha_memory", Context.MODE_PRIVATE)
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        private const val KEY_TODAY_LOG = "today_log"
        private const val KEY_DAY_LOGS = "day_logs"
        private const val KEY_MEMORIES = "memories"
        private const val KEY_CURRENT_MOOD = "current_mood"
        private const val KEY_CURRENT_BOND = "current_bond"
    }

    // ============ Day Log Functions ============
    
    suspend fun getTodayLog(userId: String): DayLog = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val existingLog = getTodayLogFromPrefs()
        
        if (existingLog != null && existingLog.date == today) {
            existingLog
        } else {
            // Create new day log
            val newLog = DayLog(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = today,
                conversations = emptyList(),
                moodSummary = getCurrentMood(),
                bondAtEnd = getCurrentBond(),
                isDeleted = false,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
            saveDayLog(newLog)
            newLog
        }
    }

    private fun getTodayLogFromPrefs(): DayLog? {
        val json = prefs.getString(KEY_TODAY_LOG, null) ?: return null
        return try {
            gson.fromJson(json, DayLog::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveDayLog(dayLog: DayLog) = withContext(Dispatchers.IO) {
        // Save as today's log
        prefs.edit().putString(KEY_TODAY_LOG, gson.toJson(dayLog)).apply()
        
        // Also save to day logs list
        val logs = getAllDayLogs().toMutableList()
        val existingIndex = logs.indexOfFirst { it.date == dayLog.date }
        if (existingIndex >= 0) {
            logs[existingIndex] = dayLog
        } else {
            logs.add(dayLog)
        }
        // Keep only last 30 days
        val recentLogs = logs.sortedByDescending { it.date }.take(30)
        prefs.edit().putString(KEY_DAY_LOGS, gson.toJson(recentLogs)).apply()
    }

    suspend fun getAllDayLogs(): List<DayLog> = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_DAY_LOGS, null) ?: return@withContext emptyList()
        try {
            val type = object : TypeToken<List<DayLog>>() {}.type
            gson.fromJson<List<DayLog>>(json, type)?.filter { !it.isDeleted } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getAllDayLogsSync(): List<DayLog> {
        val json = prefs.getString(KEY_DAY_LOGS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DayLog>>() {}.type
            gson.fromJson<List<DayLog>>(json, type)?.filter { !it.isDeleted } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addConversationEntry(
        userMessage: String,
        aishaResponse: String,
        mood: Mood?
    ) = withContext(Dispatchers.IO) {
        val todayLog = getTodayLog("")
        val entry = ConversationEntry(
            id = UUID.randomUUID().toString(),
            userMessage = userMessage,
            aishaResponse = aishaResponse,
            timestamp = System.currentTimeMillis(),
            moodSnapshot = mood
        )
        val updatedLog = todayLog.copy(
            conversations = todayLog.conversations + entry,
            lastUpdated = System.currentTimeMillis()
        )
        saveDayLog(updatedLog)
    }

    suspend fun deleteDayLog(date: String) = withContext(Dispatchers.IO) {
        val logs = getAllDayLogs().toMutableList()
        val index = logs.indexOfFirst { it.date == date }
        if (index >= 0) {
            logs[index] = logs[index].copy(isDeleted = true)
            prefs.edit().putString(KEY_DAY_LOGS, gson.toJson(logs)).apply()
        }
    }

    // ============ Memory Functions ============
    
    suspend fun saveMemory(userId: String, key: String, value: String, context: String = "") = withContext(Dispatchers.IO) {
        val memories = getAllMemories().toMutableList()
        val existingIndex = memories.indexOfFirst { it.userId == userId && it.key == key }
        
        val memory = if (existingIndex >= 0) {
            val existing = memories[existingIndex]
            memories[existingIndex] = existing.copy(
                value = value,
                context = if (context.isNotBlank()) context else existing.context,
                lastMentioned = System.currentTimeMillis(),
                mentionCount = existing.mentionCount + 1
            )
            memories[existingIndex]
        } else {
            Memory(
                id = UUID.randomUUID().toString(),
                userId = userId,
                key = key,
                value = value,
                context = context,
                firstMentioned = System.currentTimeMillis(),
                lastMentioned = System.currentTimeMillis(),
                mentionCount = 1
            )
        }
        
        if (existingIndex < 0) {
            memories.add(memory)
        }
        prefs.edit().putString(KEY_MEMORIES, gson.toJson(memories)).apply()
        memory
    }

    suspend fun getAllMemories(): List<Memory> = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_MEMORIES, null) ?: return@withContext emptyList()
        try {
            val type = object : TypeToken<List<Memory>>() {}.type
            gson.fromJson<List<Memory>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMemoriesForUser(userId: String): List<Memory> = withContext(Dispatchers.IO) {
        getAllMemories().filter { it.userId == userId }
    }

    suspend fun getMemory(userId: String, key: String): Memory? = withContext(Dispatchers.IO) {
        getAllMemories().find { it.userId == userId && it.key == key }
    }

    suspend fun recallFromMemory(userId: String, keyword: String): List<String> = withContext(Dispatchers.IO) {
        getMemoriesForUser(userId)
            .filter { it.key.contains(keyword, ignoreCase = true) || 
                     it.value.contains(keyword, ignoreCase = true) }
            .map { "${it.key}: ${it.value}" }
    }

    // ============ Mood & Bond Persistence ============
    
    suspend fun saveCurrentMood(mood: Mood) = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_CURRENT_MOOD, gson.toJson(mood)).apply()
    }

    suspend fun getCurrentMood(): Mood = withContext(Dispatchers.IO) {
        val json = prefs.getString(KEY_CURRENT_MOOD, null) ?: return@withContext Mood()
        try {
            gson.fromJson(json, Mood::class.java) ?: Mood()
        } catch (e: Exception) {
            Mood()
        }
    }

    suspend fun saveCurrentBond(bondLevel: Float) = withContext(Dispatchers.IO) {
        prefs.edit().putFloat(KEY_CURRENT_BOND, bondLevel).apply()
    }

    suspend fun getCurrentBond(): Float = withContext(Dispatchers.IO) {
        prefs.getFloat(KEY_CURRENT_BOND, 0f)
    }

    // ============ Memory Extraction ============
    
    suspend fun extractAndSaveMemories(userId: String, userMessage: String) = withContext(Dispatchers.IO) {
        val lowerMessage = userMessage.lowercase()
        
        // Extract names mentioned (simple pattern matching)
        val namePatterns = listOf(
            "mera naam" to "name",
            "mujhe bola" to "name",
            "i am" to "name", 
            "i'm" to "name",
            "my name" to "name"
        )
        
        for ((pattern, key) in namePatterns) {
            if (lowerMessage.contains(pattern)) {
                // Try to extract the name
                val parts = userMessage.split(" ")
                val patternIndex = parts.indexOfFirst { it.lowercase().contains(pattern.split(" ").last()) }
                if (patternIndex >= 0 && patternIndex + 1 < parts.size) {
                    val name = parts[patternIndex + 1].replace(Regex("[^a-zA-Z]"), "")
                    if (name.length > 1) {
                        saveMemory(userId, key, name, "Mentioned in conversation")
                    }
                }
            }
        }
        
        // Extract hobbies/interests
        val hobbyPatterns = listOf(
            "mujhe pasand hai" to "hobby",
            "i like" to "hobby",
            "i love" to "hobby",
            "mera hobby" to "hobby"
        )
        
        for ((pattern, key) in hobbyPatterns) {
            if (lowerMessage.contains(pattern)) {
                saveMemory(userId, key, userMessage, "Interest mentioned")
            }
        }
    }

    // ============ Clear All Data ============
    
    suspend fun clearAll() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}
