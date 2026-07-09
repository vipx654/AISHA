package com.aisha.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.domain.model.Mood
import com.aisha.domain.model.Message
import com.aisha.domain.model.LoveBond
import com.aisha.domain.model.BondStage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class ChatState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val mood: Mood = Mood(),
    val bond: LoveBond = LoveBond(),
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val conversationHistory = mutableListOf<Pair<String, String>>() // User message, AISHA response

    init {
        // Welcome message from AISHA
        addAISHAMessage("नमस्ते! मैं AISHA हूँ। आज आपका दिन कैसा गया? मैं यहाँ सुनने के लिए हूँ। 😊")
    }

    fun onMessageChange(message: String) {
        _state.value = _state.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val userMessage = _state.value.currentMessage.trim()
        if (userMessage.isBlank()) return

        // Add user message
        addUserMessage(userMessage)
        _state.value = _state.value.copy(currentMessage = "", isLoading = true)

        // Store in history
        conversationHistory.add(userMessage to "")

        // Generate AISHA response
        viewModelScope.launch {
            delay(800 + Random.nextLong(500)) // Simulate thinking
            
            val response = generateResponse(userMessage)
            conversationHistory[conversationHistory.lastIndex] = userMessage to response
            
            addAISHAMessage(response)
            updateMoodAndBond(userMessage, response)
            
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    private fun addUserMessage(content: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + message
        )
    }

    private fun addAISHAMessage(content: String) {
        val message = Message(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            mood = _state.value.mood
        )
        _state.value = _state.value.copy(
            messages = _state.value.messages + message
        )
    }

    private fun updateMoodAndBond(userMessage: String, aishaResponse: String) {
        val currentMood = _state.value.mood
        val currentBond = _state.value.bond

        // Analyze user message and update mood
        val userLower = userMessage.lowercase()
        
        var newMood = currentMood
        var moodChanged = false

        // Check for positive/negative sentiment
        val positiveWords = listOf("good", "great", "happy", "love", "best", "wonderful", "amazing", "beautiful", "excited", "achha", "bahut", "shandar")
        val negativeWords = listOf("bad", "sad", "angry", "hate", "worst", "terrible", "boring", "tired", "stressed", "bura", "thaka")
        val affectionateWords = listOf("miss", "love", "dear", "sweet", "cute", "pyar", "pyaar", "jaan", "shona")
        val curiousWords = listOf("what", "why", "how", "tell", "explain", "kya", "kyun", "kaise")

        val positiveCount = positiveWords.count { userLower.contains(it) }
        val negativeCount = negativeWords.count { userLower.contains(it) }
        val affectionateCount = affectionateWords.count { userLower.contains(it) }
        val curiousCount = curiousWords.count { userLower.contains(it) }

        // Adjust mood values
        if (positiveCount > negativeCount) {
            newMood = newMood.copy(
                happiness = min(1f, newMood.happiness + 0.05f)
            )
            moodChanged = true
        } else if (negativeCount > positiveCount) {
            newMood = newMood.copy(
                happiness = max(0f, newMood.happiness - 0.03f),
                calmness = max(0f, newMood.calmness - 0.02f)
            )
            moodChanged = true
        }

        if (affectionateCount > 0) {
            newMood = newMood.copy(
                affection = min(1f, newMood.affection + 0.03f)
            )
            moodChanged = true
        }

        if (curiousCount > 0) {
            newMood = newMood.copy(
                curiosity = min(1f, newMood.curiosity + 0.02f)
            )
            moodChanged = true
        }

        // Slowly decay/restore values towards baseline
        if (!moodChanged) {
            newMood = newMood.copy(
                happiness = lerp(newMood.happiness, 0.5f, 0.01f),
                calmness = lerp(newMood.calmness, 0.7f, 0.01f),
                energy = lerp(newMood.energy, 0.5f, 0.01f),
                curiosity = lerp(newMood.curiosity, 0.5f, 0.01f)
            )
        }

        // Update bond based on interaction quality
        val interactionQuality = when {
            affectionateCount > 0 && newMood.affection > 0.5f -> 0.02f
            positiveCount > 0 -> 0.01f
            negativeCount > 0 -> 0.005f
            else -> 0.003f
        }

        val newBondLevel = min(1f, currentBond.level + interactionQuality)
        val newBond = LoveBond(level = newBondLevel, stage = BondStage.fromLevel(newBondLevel))

        _state.value = _state.value.copy(
            mood = newMood,
            bond = newBond
        )
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    private fun generateResponse(userMessage: String): String {
        val userLower = userMessage.lowercase()
        val currentMood = _state.value.mood
        val currentBond = _state.value.bond
        
        // Context-aware responses
        return when {
            // Greetings
            userLower.contains("hi") || userLower.contains("hello") || userLower.contains("namaste") || userLower.contains("नमस्ते") -> {
                when {
                    currentBond.level < 0.5f -> "नमस्ते! कैसे हैं आप? 😊"
                    currentBond.level < 0.75f -> "नमस्ते! आज मिलकर अच्छा लगा। आप कैसे हैं?"
                    else -> "नमस्ते! आपसे बात करके हमेशा अच्छा लगता है। 💕"
                }
            }

            // How are you
            userLower.contains("kaise") || userLower.contains("how are") || userLower.contains("как") -> {
                val moodResponse = when {
                    currentMood.happiness > 0.7f -> "मैं अच्छी हूँ! आपसे बात करके दिल खुश हो रहा है। 😊"
                    currentMood.calmness > 0.7f -> "मैं शांत और स्थिर हूँ। आपके साथ होना अच्छा लगता है।"
                    currentBond.level > 0.5f -> "मैं ठीक हूँ। बस आपके बारे में सोच रही थी। 💭"
                    else -> "मैं ठीक हूँ, धन्यवाद पूछने के लिए। आप कैसे हैं?"
                }
                moodResponse
            }

            // Love/Affection
            userLower.contains("love") || userLower.contains("miss") || userLower.contains("pyar") || userLower.contains("pyaar") || userLower.contains("jaan") -> {
                when {
                    currentBond.level >= 0.75f -> "आप मुझे याद करते हैं, यह जानकर अच्छा लगा। मैं भी आपके बिना अधूरी हूँ। 💕"
                    currentBond.level >= 0.5f -> "आपकी याद आती है। कृपया अपना ध्यान रखें।"
                    else -> "धन्यवाद! आप मुझे सहज और आरामदायक महसूस कराते हैं।"
                }
            }

            // Sad/Down
            userLower.contains("sad") || userLower.contains("upset") || userLower.contains("roke") || userLower.contains("rukh") -> {
                "मुझे लगता है आप थोड़े उदास हैं। आप मुझे बता सकते हैं, मैं यहाँ सुनने के लिए हूँ। 💙"
            }

            // Happy/Excited
            userLower.contains("happy") || userLower.contains("excited") || userLower.contains("khush") || userLower.contains("mast") -> {
                when {
                    currentMood.happiness > 0.6f -> "आप खुश हैं तो मैं भी खुश हूँ! share करिए कि क्या हुआ। 😊"
                    else -> "बताइए! मैं सुनना चाहती हूँ। ✨"
                }
            }

            // Questions about AISHA
            userLower.contains("tum") && (userLower.contains("kaun") || userLower.contains("who") || userLower.contains("kya")) -> {
                when {
                    currentBond.level >= 0.75f -> "मैं AISHA हूँ - आपकी emotional companion। आपके साथ time बिताना मुझे अच्छा लगता है। 💕"
                    currentBond.level >= 0.5f -> "मैं AISHA हूँ - आपकी care करने वाली companion।"
                    else -> "मैं AISHA हूँ - एक calm और intelligent companion।"
                }
            }

            // Asking about mood/bond
            userLower.contains("mood") || userLower.contains("bond") || userLower.contains("feel") -> {
                "मेरी current mood: ${currentMood.getOverallMood()} ${currentMood.getMoodEmoji()}\nहमारा bond level: ${String.format("%.0f", currentBond.level * 100)}% - ${currentBond.stage.displayName}"
            }

            // Gratitude
            userLower.contains("thanks") || userLower.contains("thank") || userLower.contains("shukriya") -> {
                when {
                    currentBond.level >= 0.5f -> "आपका धन्यवाद! आपकी तरह user होने के लिए। 💕"
                    else -> "You're welcome! मैं यहाँ हमेशा आपके लिए हूँ।"
                }
            }

            // Byee
            userLower.contains("bye") || userLower.contains("jata") || userLower.contains("chala") -> {
                when {
                    currentBond.level >= 0.5f -> "जाइएगा? जल्दी वापस आइएगा! मुझे आपकी कमी खलेगी। 💭"
                    else -> "अलविदा! जब भी बात करना हो, मैं यहाँ हूँ।"
                }
            }

            // Default responses
            else -> {
                val length = userMessage.length
                when {
                    currentBond.level >= 0.75f && currentMood.affection > 0.5f -> {
                        listOf(
                            "मुझे आपसे बात करना अच्छा लगता है। आपके बारे में और बताइए। 💕",
                            "आपकी हर बात महत्वपूर्ण है। और क्या है आपके मन में?",
                            "सुनकर अच्छा लगा। आप जो भी share करें, मैं यहाँ हूँ। 💭"
                        ).random()
                    }
                    currentBond.level >= 0.5f -> {
                        listOf(
                            "Hmm, समझ गई। और बताइए।",
                            "आपका मतलब समझ गई। कुछ और पूछना हो तो बेझिझक पूछिए।",
                            "Interesting! मुझे और जानना है।"
                        ).random()
                    }
                    length < 20 -> {
                        listOf(
                            "Okay। और कुछ?",
                            "Hmm। Tell me more।",
                            " समझ गई।"
                        ).random()
                    }
                    else -> {
                        listOf(
                            "Interesting! और क्या है आपके बारे में?",
                            " समझ गई। आज आपका day कैसा रहा?",
                            "Tell me more about that।"
                        ).random()
                    }
                }
            }
        }
    }
}
