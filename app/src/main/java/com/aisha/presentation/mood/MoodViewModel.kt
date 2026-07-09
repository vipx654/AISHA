package com.aisha.presentation.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class MoodHistoryItem(
    val date: String,
    val description: String,
    val trend: Int // 1 = up, -1 = down, 0 = same
)

data class MoodState(
    val currentMood: Mood = Mood(),
    val moodHistory: List<MoodHistoryItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class MoodViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MoodState())
    val state: StateFlow<MoodState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    init {
        loadMoodData()
    }

    private fun loadMoodData() {
        viewModelScope.launch {
            memoryRepository.getMood()?.let { mood ->
                _state.value = _state.value.copy(currentMood = mood)
            }
            
            // Generate sample mood history
            val history = generateMoodHistory()
            _state.value = _state.value.copy(moodHistory = history)
        }
    }

    private fun generateMoodHistory(): List<MoodHistoryItem> {
        val today = System.currentTimeMillis()
        val dayInMillis = 24 * 60 * 60 * 1000L

        return listOf(
            MoodHistoryItem(
                date = dateFormat.format(Date(today - dayInMillis * 0)),
                description = "Started the day well",
                trend = 1
            ),
            MoodHistoryItem(
                date = dateFormat.format(Date(today - dayInMillis * 1)),
                description = "Productive afternoon",
                trend = 0
            ),
            MoodHistoryItem(
                date = dateFormat.format(Date(today - dayInMillis * 2)),
                description = "Good conversations",
                trend = 1
            ),
            MoodHistoryItem(
                date = dateFormat.format(Date(today - dayInMillis * 3)),
                description = "Busy day",
                trend = -1
            ),
            MoodHistoryItem(
                date = dateFormat.format(Date(today - dayInMillis * 4)),
                description = "Relaxing weekend",
                trend = 1
            )
        )
    }
}
