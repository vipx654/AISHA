package com.aisha.domain.model

data class DayLog(
    val id: String = "",
    val userId: String = "",
    val date: String = "", // Format: "yyyy-MM-dd"
    val conversations: List<ConversationEntry> = emptyList(),
    val moodSummary: Mood = Mood(),
    val bondAtEnd: Float = 0f,
    val notes: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ConversationEntry(
    val id: String = "",
    val userMessage: String = "",
    val aishaResponse: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val moodSnapshot: Mood? = null
)

data class Memory(
    val id: String = "",
    val userId: String = "",
    val key: String = "", // e.g., "favorite_food", "hobby", "person_name"
    val value: String = "", // e.g., "Biryani", "Reading", "Rahul"
    val context: String = "", // When/why this was mentioned
    val firstMentioned: Long = System.currentTimeMillis(),
    val lastMentioned: Long = System.currentTimeMillis(),
    val mentionCount: Int = 1
)
