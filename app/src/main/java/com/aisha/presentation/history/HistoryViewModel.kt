package com.aisha.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.DayLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val dayLogs: List<DayLog> = emptyList(),
    val isLoading: Boolean = true,
    val selectedDayLog: DayLog? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val logs = memoryRepository.getAllDayLogs()
            _state.value = _state.value.copy(
                dayLogs = logs.sortedByDescending { it.date },
                isLoading = false
            )
        }
    }

    fun selectDayLog(dayLog: DayLog) {
        _state.value = _state.value.copy(selectedDayLog = dayLog)
    }

    fun clearSelection() {
        _state.value = _state.value.copy(selectedDayLog = null)
    }

    fun deleteDayLog(date: String) {
        viewModelScope.launch {
            memoryRepository.deleteDayLog(date)
            loadHistory()
        }
    }
}
