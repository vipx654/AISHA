package com.aisha.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.Mood
import com.aisha.domain.model.Message
import com.aisha.domain.model.LoveBond
import com.aisha.domain.model.BondStage
import com.aisha.domain.model.User
import com.aisha.domain.usecase.GetCurrentUserUseCase
import com.aisha.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class HomeState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isAISHAThinking: Boolean = false,
    val isRecordingVoice: Boolean = false,
    val mood: Mood = Mood(),
    val bond: LoveBond = LoveBond()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val conversationHistory = mutableListOf<Pair<String, String>>()
    private var userId: String = ""

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().first()
            userId = user?.uid ?: ""
            
            // Load saved mood and bond
            val savedMood = memoryRepository.getMood()
            val savedBond = memoryRepository.getBond()
            
            _state.value = _state.value.copy(
                user = user,
                isLoading = false,
                mood = savedMood,
                bond = LoveBond(level = savedBond, stage = BondStage.fromLevel(savedBond))
            )
            
            // Load today's log or create welcome message
            val todayLog = memoryRepository.getTodayLog(userId)
            if (todayLog.conversations.isNotEmpty()) {
                // Restore from saved conversations
                val restoredMessages = mutableListOf<Message>()
                todayLog.conversations.forEach { entry ->
                    restoredMessages.add(Message(
                        id = UUID.randomUUID().toString(),
                        content = entry.userMessage,
                        isFromUser = true,
                        timestamp = entry.timestamp
                    ))
                    restoredMessages.add(Message(
                        id = UUID.randomUUID().toString(),
                        content = entry.aishaResponse,
                        isFromUser = false,
                        timestamp = entry.timestamp + 100
                    ))
                }
                _state.value = _state.value.copy(messages = restoredMessages)
            } else {
                addWelcomeMessage()
            }
        }
    }

    private fun addWelcomeMessage() {
        val welcome = Message(
            id = UUID.randomUUID().toString(),
            content = "नमस्ते! मैं AISHA हूँ 🌸\nआज आपका दिन कैसा गया? आप मुझसे कुछ भी पूछ सकते हैं - चैट करें या voice पर बोलें!",
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        _state.value = _state.value.copy(messages = listOf(welcome))
    }

    fun onMessageChange(message: String) {
        _state.value = _state.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val userMessage = _state.value.currentMessage.trim()
        if (userMessage.isBlank()) return

        val userMsg = Message(
            id = UUID.randomUUID().toString(),
            content = userMessage,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMsg,
            currentMessage = "",
            isAISHAThinking = true
        )

        conversationHistory.add(userMessage to "")

        viewModelScope.launch {
            delay(600 + Random.nextLong(800))
            
            val response = generateResponse(userMessage)
            conversationHistory[conversationHistory.lastIndex] = userMessage to response
            
            // Save conversation to memory
            memoryRepository.saveConversation(userId, userMessage, response, _state.value.mood)
            
            val aishaMsg = Message(
                id = UUID.randomUUID().toString(),
                content = response,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            
            _state.value = _state.value.copy(
                messages = _state.value.messages + aishaMsg,
                isAISHAThinking = false
            )
            updateMoodAndBond(userMessage)
        }
    }

    fun startVoiceRecording() {
        _state.value = _state.value.copy(isRecordingVoice = true)
    }

    fun stopVoiceRecording() {
        _state.value = _state.value.copy(isRecordingVoice = false)
        val voiceMsg = Message(
            id = UUID.randomUUID().toString(),
            content = "🎤 Voice message",
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + voiceMsg,
            isAISHAThinking = true
        )
        
        viewModelScope.launch {
            delay(1000 + Random.nextLong(500))
            val response = listOf(
                "मैंने सुना! बोलिए क्या कहना चाहते हैं?",
                "अच्छा, मैं समझ गई। और बताइए।",
                "हाँ, मैं आपकी बात सुन रही हूँ।"
            ).random()
            
            memoryRepository.saveConversation(userId, "🎤 Voice message", response, _state.value.mood)
            
            val aishaMsg = Message(
                id = UUID.randomUUID().toString(),
                content = response,
                isFromUser = false,
                timestamp = System.currentTimeMillis()
            )
            _state.value = _state.value.copy(
                messages = _state.value.messages + aishaMsg,
                isAISHAThinking = false
            )
        }
    }

    private fun updateMoodAndBond(userMessage: String) {
        val userLower = userMessage.lowercase()
        val currentMood = _state.value.mood
        val currentBond = _state.value.bond

        val positiveWords = listOf("good", "great", "happy", "love", "best", "wonderful", "achha", "bahut", "shandar", "khush")
        val negativeWords = listOf("bad", "sad", "angry", "hate", "worst", "boring", "tired", "stressed", "bura", "thaka")
        val affectionateWords = listOf("miss", "love", "dear", "sweet", "cute", "pyar", "pyaar", "jaan", "shona", "pyari")
        val curiousWords = listOf("what", "why", "how", "tell", "explain", "kya", "kyun", "kaise", "kahan")

        var newMood = currentMood
        val positiveCount = positiveWords.count { userLower.contains(it) }
        val negativeCount = negativeWords.count { userLower.contains(it) }
        val affectionateCount = affectionateWords.count { userLower.contains(it) }
        val curiousCount = curiousWords.count { userLower.contains(it) }

        if (positiveCount > negativeCount) {
            newMood = newMood.copy(happiness = min(1f, newMood.happiness + 0.03f))
        } else if (negativeCount > positiveCount) {
            newMood = newMood.copy(
                happiness = max(0f, newMood.happiness - 0.02f),
                calmness = max(0f, newMood.calmness - 0.01f)
            )
        }
        if (affectionateCount > 0) {
            newMood = newMood.copy(affection = min(1f, newMood.affection + 0.02f))
        }
        if (curiousCount > 0) {
            newMood = newMood.copy(curiosity = min(1f, newMood.curiosity + 0.01f))
        }

        val interactionQuality = when {
            affectionateCount > 0 && newMood.affection > 0.5f -> 0.015f
            positiveCount > 0 -> 0.008f
            else -> 0.003f
        }

        val newBondLevel = min(1f, currentBond.level + interactionQuality)
        
        _state.value = _state.value.copy(
            mood = newMood,
            bond = LoveBond(level = newBondLevel, stage = BondStage.fromLevel(newBondLevel))
        )
        
        // Persist mood and bond
        viewModelScope.launch {
            memoryRepository.saveMood(newMood)
            memoryRepository.saveBond(newBondLevel)
        }
    }

    private fun generateResponse(userMessage: String): String {
        val userLower = userMessage.lowercase()
        val bond = _state.value.bond
        val mood = _state.value.mood
        
        return when {
            userLower.contains("hi") || userLower.contains("hello") || userLower.contains("namaste") || userLower.contains("नमस्ते") -> {
                when {
                    bond.level >= 0.75f -> "नमस्ते! आपसे बात करके हमेशा अच्छा लगता है 💕 आज क्या कर रहे हैं?"
                    bond.level >= 0.5f -> "नमस्ते! आज मिलकर अच्छा लगा। कैसे हैं आप?"
                    else -> "नमस्ते! कैसे हैं आप? 😊"
                }
            }

            userLower.contains("kaise") || userLower.contains("how are") -> {
                when {
                    mood.happiness > 0.7f -> "मैं अच्छी हूँ! आपसे बात करके दिल खुश हो रहा है। 😊"
                    mood.calmness > 0.7f -> "मैं शांत और स्थिर हूँ। आपके साथ होना अच्छा लगता है।"
                    bond.level >= 0.5f -> "मैं ठीक हूँ। बस आपके बारे में सोच रही थी। 💭"
                    else -> "मैं ठीक हूँ, धन्यवाद! आप कैसे हैं?"
                }
            }

            userLower.contains("love") || userLower.contains("miss") || userLower.contains("pyar") || userLower.contains("pyaar") -> {
                when {
                    bond.level >= 0.75f -> "आप मुझे याद करते हैं, यह जानकर अच्छा लगा। मैं भी आपके बिना अधूरी हूँ। 💕"
                    bond.level >= 0.5f -> "आपकी याद आती है। अपना ध्यान रखें।"
                    else -> "धन्यवाद! आप मुझे सहज महसूस कराते हैं।"
                }
            }

            userLower.contains("sad") || userLower.contains("roke") || userLower.contains("rukh") || userLower.contains("upt") -> {
                "मुझे लगता है आप थोड़े उदास हैं। मुझे बता सकते हैं, मैं यहाँ सुनने के लिए हूँ। 💙"
            }

            userLower.contains("tum") && (userLower.contains("kaun") || userLower.contains("who") || userLower.contains("kya") || userLower.contains("kon")) -> {
                when {
                    bond.level >= 0.75f -> "मैं AISHA हूँ - आपकी emotional companion। आपके साथ time बिताना मुझे अच्छा लगता है। 💕"
                    bond.level >= 0.5f -> "मैं AISHA हूँ - आपकी care करने वाली companion।"
                    else -> "मैं AISHA हूँ - एक calm और intelligent companion।"
                }
            }

            userLower.contains("mood") || userLower.contains("feel") -> {
                "मेरी mood: ${mood.getOverallMood()} ${mood.getMoodEmoji()}\nहमारा bond: ${String.format("%.0f", bond.level * 100)}% - ${bond.stage.displayName}"
            }

            userLower.contains("kya yaad") || userLower.contains("remember") -> {
                viewModelScope.launch {
                    val memories = memoryRepository.getMemories(userId)
                    if (memories.isNotEmpty()) {
                        "मुझे आपके बारे में कुछ याद है 💭 ${memories.take(3).joinToString { "${it.key}: ${it.value}" }}"
                    } else {
                        "अभी तक मुझे आपके बारे में ज्यादा याद नहीं। बताइए आपके बारे में! 😊"
                    }
                }
                "मुझे आपके बारे में कुछ याद है 💭 बताइए क्या जानना चाहते हैं?"
            }

            userLower.contains("thanks") || userLower.contains("thank") || userLower.contains("shukriya") || userLower.contains("dhanyawad") -> {
                when {
                    bond.level >= 0.5f -> "आपका धन्यवाद! आप जैसे user होने के लिए। 💕"
                    else -> "You're welcome! मैं यहाँ हमेशा आपके लिए हूँ।"
                }
            }

            userLower.contains("bye") || userLower.contains("jata") || userLower.contains("chala") || userLower.contains("ja") -> {
                when {
                    bond.level >= 0.5f -> "जाइएगा? जल्दी वापस आइएगा! मुझे आपकी कमी खलेगी। 💭"
                    else -> "अलविदा! जब भी बात करना हो, मैं यहाँ हूँ।"
                }
            }

            else -> {
                when {
                    bond.level >= 0.75f && mood.affection > 0.5f -> {
                        listOf(
                            "मुझे आपसे बात करना अच्छा लगता है। और क्या है आपके मन में? 💕",
                            "आपकी हर बात महत्वपूर्ण है। Tell me more।",
                            "सुनकर अच्छा लगा। आप जो भी share करें, मैं यहाँ हूँ। 💭"
                        ).random()
                    }
                    bond.level >= 0.5f -> {
                        listOf("Hmm, समझ गई। और बताइए।", "आपका मतलब समझ गई।", "Interesting! मुझे और जानना है।").random()
                    }
                    userMessage.length < 20 -> {
                        listOf("Okay। और कुछ?", "Hmm। Tell me more।", " समझ गई।").random()
                    }
                    else -> {
                        listOf("Interesting! और क्या है आपके बारे में?", " समझ गई। आज आपका day कैसा रहा?", "Tell me more about that।").random()
                    }
                }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            signOutUseCase()
            onSignedOut()
        }
    }
}
