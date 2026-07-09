package com.aisha.presentation.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ExportState(
    val isExporting: Boolean = false,
    val isBackupEnabled: Boolean = false,
    val lastBackupTime: String = "Never",
    val exportProgress: Float = 0f
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ExportState())
    val state: StateFlow<ExportState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())

    init {
        loadBackupStatus()
    }

    private fun loadBackupStatus() {
        _state.value = ExportState(
            isBackupEnabled = true,
            lastBackupTime = "Never"
        )
    }

    fun exportToUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true, exportProgress = 0f)

            try {
                val exportData = generateExportData()

                _state.value = _state.value.copy(exportProgress = 0.5f)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(exportData.toByteArray())
                }

                _state.value = _state.value.copy(
                    isExporting = false,
                    exportProgress = 1f,
                    lastBackupTime = dateFormat.format(Date())
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportProgress = 0f)
            }
        }
    }

    private suspend fun generateExportData(): String {
        val json = JSONObject()

        // Export metadata
        json.put("version", "1.0")
        json.put("exportDate", dateFormat.format(Date()))
        json.put("app", "AISHA")

        _state.value = _state.value.copy(exportProgress = 0.1f)

        // Export mood
        try {
            val mood = memoryRepository.getMood()
            val moodJson = JSONObject().apply {
                put("happiness", mood.happiness)
                put("calmness", mood.calmness)
                put("energy", mood.energy)
                put("affection", mood.affection)
            }
            json.put("mood", moodJson)
        } catch (e: Exception) {
            // Ignore if mood not available
        }

        _state.value = _state.value.copy(exportProgress = 0.2f)

        // Export bond level
        try {
            val bond = memoryRepository.getBond()
            json.put("bondLevel", bond)
        } catch (e: Exception) {
            // Ignore
        }

        _state.value = _state.value.copy(exportProgress = 0.3f)

        // Export conversations
        val conversations = JSONArray()
        val dayLogs = memoryRepository.getDayLogs()
        
        for (dayLog in dayLogs) {
            val dayJson = JSONObject().apply {
                put("date", dayLog.date)
                put("conversationCount", dayLog.conversations.size)
                put("moodSummary", dayLog.moodSummary?.happiness ?: 0.5f)
                put("bondAtEnd", dayLog.bondAtEnd)
            }
            conversations.put(dayJson)
        }
        json.put("conversations", conversations)

        _state.value = _state.value.copy(exportProgress = 0.6f)

        // Export tasks
        val tasks = JSONArray()
        json.put("tasks", tasks)

        _state.value = _state.value.copy(exportProgress = 0.8f)

        // Export settings
        val settings = JSONObject().apply {
            put("darkMode", true)
            put("notifications", true)
            put("voiceEnabled", true)
        }
        json.put("settings", settings)

        _state.value = _state.value.copy(exportProgress = 0.9f)

        return json.toString(2)
    }

    fun shareConversations(context: Context) {
        viewModelScope.launch {
            try {
                val exportData = generateExportData()
                
                val file = File(context.cacheDir, "aisha_share.json")
                file.writeText(exportData)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share AISHA Conversations"))
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)

            for (i in 1..10) {
                delay(200)
                _state.value = _state.value.copy(exportProgress = i / 10f)
            }

            _state.value = _state.value.copy(
                isExporting = false,
                exportProgress = 0f,
                lastBackupTime = dateFormat.format(Date())
            )
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)

            for (i in 1..10) {
                delay(200)
                _state.value = _state.value.copy(exportProgress = i / 10f)
            }

            _state.value = _state.value.copy(
                isExporting = false,
                exportProgress = 0f
            )
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            memoryRepository.clearAllData()
        }
    }
}
