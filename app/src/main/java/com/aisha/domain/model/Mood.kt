package com.aisha.domain.model

data class Mood(
    val happiness: Float = 0.5f,      // 0.0 - 1.0
    val calmness: Float = 0.7f,         // 0.0 - 1.0
    val energy: Float = 0.5f,           // 0.0 - 1.0
    val affection: Float = 0.3f,         // 0.0 - 1.0
    val curiosity: Float = 0.5f          // 0.0 - 1.0
) {
    fun getOverallMood(): String {
        return when {
            happiness > 0.7f && calmness > 0.7f -> "Happy & Calm"
            happiness > 0.6f -> "Happy"
            calmness > 0.7f -> "Peaceful"
            energy > 0.7f -> "Energetic"
            affection > 0.6f -> "Affectionate"
            curiosity > 0.6f -> "Curious"
            energy < 0.3f -> "Tired"
            happiness < 0.3f -> "Sad"
            else -> "Neutral"
        }
    }
    
    fun getMoodEmoji(): String {
        return when {
            happiness > 0.7f -> "😊"
            happiness > 0.5f -> "🙂"
            calmness > 0.7f -> "😌"
            energy > 0.7f -> "⚡"
            affection > 0.6f -> "💕"
            curiosity > 0.6f -> "🤔"
            energy < 0.3f -> "😴"
            happiness < 0.3f -> "😔"
            else -> "😐"
        }
    }
}
