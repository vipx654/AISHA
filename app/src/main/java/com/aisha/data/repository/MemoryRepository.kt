package com.aisha.data.repository

import com.aisha.data.local.MemoryLocalDataSource
import com.aisha.domain.model.DayLog
import com.aisha.domain.model.Memory
import com.aisha.domain.model.Mood
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val localDataSource: MemoryLocalDataSource
) {
    suspend fun getTodayLog(userId: String): DayLog {
        return localDataSource.getTodayLog(userId)
    }

    suspend fun saveConversation(userId: String, userMessage: String, aishaResponse: String, mood: Mood?) {
        localDataSource.addConversationEntry(userMessage, aishaResponse, mood)
        localDataSource.extractAndSaveMemories(userId, userMessage)
    }

    suspend fun getAllDayLogs(): List<DayLog> {
        return localDataSource.getAllDayLogs()
    }

    suspend fun deleteDayLog(date: String) {
        localDataSource.deleteDayLog(date)
    }

    suspend fun getMemories(userId: String): List<Memory> {
        return localDataSource.getMemoriesForUser(userId)
    }

    suspend fun recallMemories(userId: String, keyword: String): List<String> {
        return localDataSource.recallFromMemory(userId, keyword)
    }

    suspend fun saveMood(mood: Mood) {
        localDataSource.saveCurrentMood(mood)
    }

    suspend fun getMood(): Mood {
        return localDataSource.getCurrentMood()
    }

    suspend fun saveBond(level: Float) {
        localDataSource.saveCurrentBond(level)
    }

    suspend fun getBond(): Float {
        return localDataSource.getCurrentBond()
    }
}
