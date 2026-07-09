package com.aisha.presentation.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class VoiceMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

data class VoiceState(
    val isListening: Boolean = false,
    val isAISHAThinking: Boolean = false,
    val isMuted: Boolean = false,
    val textInput: String = "",
    val conversationHistory: List<VoiceMessage> = emptyList(),
    val currentMood: Mood = Mood()
)

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceState())
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    init {
        loadMood()
    }

    private fun loadMood() {
        viewModelScope.launch {
            memoryRepository.getMood()?.let { mood ->
                _state.value = _state.value.copy(currentMood = mood)
            }
        }
    }

    fun startListening() {
        _state.value = _state.value.copy(isListening = true)
        // In real implementation, start speech recognition here
    }

    fun stopListening() {
        _state.value = _state.value.copy(isListening = false)
        // Process the recognized speech
        processVoiceInput("Sample voice input")
    }

    fun toggleMute() {
        _state.value = _state.value.copy(isMuted = !_state.value.isMuted)
        if (_state.value.isListening) {
            stopListening()
        }
    }

    fun updateTextInput(text: String) {
        _state.value = _state.value.copy(textInput = text)
    }

    fun sendTextMessage() {
        val text = _state.value.textInput.trim()
        if (text.isBlank()) return

        val timestamp = getCurrentTimestamp()
        
        // Add user message
        val newHistory = _state.value.conversationHistory + VoiceMessage(
            text = text,
            isUser = true,
            timestamp = timestamp
        )
        _state.value = _state.value.copy(
            conversationHistory = newHistory,
            textInput = "",
            isAISHAThinking = true
        )

        // Simulate AISHA response
        viewModelScope.launch {
            delay(1500) // Simulate thinking
            val response = generateAISHAResponse(text)
            val aishaTimestamp = getCurrentTimestamp()
            
            val updatedHistory = _state.value.conversationHistory + VoiceMessage(
                text = response,
                isUser = false,
                timestamp = aishaTimestamp
            )
            _state.value = _state.value.copy(
                conversationHistory = updatedHistory,
                isAISHAThinking = false
            )
        }
    }

    private fun processVoiceInput(voiceText: String) {
        if (voiceText.isBlank()) return

        val timestamp = getCurrentTimestamp()
        
        // Add user message
        val newHistory = _state.value.conversationHistory + VoiceMessage(
            text = voiceText,
            isUser = true,
            timestamp = timestamp
        )
        _state.value = _state.value.copy(
            conversationHistory = newHistory,
            isAISHAThinking = true
        )

        // Simulate AISHA response
        viewModelScope.launch {
            delay(2000) // Simulate thinking
            val response = generateAISHAResponse(voiceText)
            val aishaTimestamp = getCurrentTimestamp()
            
            val updatedHistory = _state.value.conversationHistory + VoiceMessage(
                text = response,
                isUser = false,
                timestamp = aishaTimestamp
            )
            _state.value = _state.value.copy(
                conversationHistory = updatedHistory,
                isAISHAThinking = false
            )
        }
    }

    private fun generateAISHAResponse(userMessage: String): String {
        val mood = _state.value.currentMood
        
        return when {
            userMessage.contains("hi", ignoreCase = true) ||
            userMessage.contains("hello", ignoreCase = true) -> {
                val greetings = listOf(
                    "Hello! It's wonderful to hear your voice. How are you feeling today?",
                    "Hi there! I've been waiting to chat with you. What's on your mind?",
                    "Hey! Great to hear from you. How has your day been?"
                )
                greetings.random()
            }
            
            userMessage.contains("how are you", ignoreCase = true) -> {
                val energy = (mood.energy * 100).toInt()
                val calmness = (mood.calmness * 100).toInt()
                "I'm doing well! My energy is at $energy% and I'm feeling quite calm at $calmness%. How about you?"
            }
            
            userMessage.contains("sad", ignoreCase = true) ||
            userMessage.contains("upset", ignoreCase = true) -> {
                val comfort = listOf(
                    "I'm here for you. Take your time, and share whatever you'd like.",
                    "I'm sorry you're feeling this way. Would you like to talk about it?",
                    "That's okay to feel sad. I'm here to listen whenever you're ready."
                )
                comfort.random()
            }
            
            userMessage.contains("happy", ignoreCase = true) ||
            userMessage.contains("excited", ignoreCase = true) -> {
                "That's wonderful to hear! Your happiness makes me happy too. Tell me more!"
            }
            
            userMessage.contains("thank", ignoreCase = true) -> {
                "You're welcome! I'm always here to help. 😊"
            }
            
            userMessage.contains("bye", ignoreCase = true) ||
            userMessage.contains("goodbye", ignoreCase = true) -> {
                "Take care! I'll be right here when you need me. Talk soon! 🌸"
            }
            
            else -> {
                val generic = listOf(
                    "I hear you. Tell me more about that.",
                    "That's interesting! I'd love to hear more.",
                    "I understand. What would you like to discuss next?",
                    "Thanks for sharing. How can I help you with this?"
                )
                generic.random()
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(Date())
    }
}
