package com.aisha.domain.model

data class Message(
    val id: String = "",
    val content: String = "",
    val isFromUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val mood: Mood? = null  // AISHA's mood when responding
)

data class ChatSession(
    val id: String = "",
    val userId: String = "",
    val messages: List<Message> = emptyList(),
    val currentMood: Mood = Mood(),
    val currentBond: LoveBond = LoveBond(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastInteraction: Long = System.currentTimeMillis()
)
