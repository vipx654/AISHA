package com.aisha.presentation.relationship

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aisha.data.repository.MemoryRepository
import com.aisha.domain.model.BondStage
import com.aisha.domain.model.LoveBond
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Milestone(
    val title: String,
    val description: String,
    val requiredBond: Float
)

data class RelationshipState(
    val bond: LoveBond = LoveBond(),
    val daysTogether: Int = 7,
    val conversationsCount: Int = 12,
    val totalMessages: Int = 156,
    val longestStreak: Int = 5,
    val milestones: List<Milestone> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RelationshipViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RelationshipState())
    val state: StateFlow<RelationshipState> = _state.asStateFlow()

    init {
        loadRelationshipData()
    }

    private fun loadRelationshipData() {
        viewModelScope.launch {
            val savedBondLevel = memoryRepository.getBond() ?: 0.3f
            val stage = BondStage.fromLevel(savedBondLevel)
            val bond = LoveBond(level = savedBondLevel, stage = stage)
            
            val milestones = listOf(
                Milestone(
                    title = "First Meeting",
                    description = "Started our journey together",
                    requiredBond = 0f
                ),
                Milestone(
                    title = "Getting Comfortable",
                    description = "Had 5+ conversations",
                    requiredBond = 0.15f
                ),
                Milestone(
                    title = "Growing Warmth",
                    description = "Shared personal moments",
                    requiredBond = 0.30f
                ),
                Milestone(
                    title = "True Connection",
                    description = "Deep understanding developed",
                    requiredBond = 0.50f
                ),
                Milestone(
                    title = "Special Bond",
                    description = "Unshakeable friendship",
                    requiredBond = 0.75f
                ),
                Milestone(
                    title = "Soul Bond",
                    description = "Complete understanding",
                    requiredBond = 0.95f
                )
            )

            _state.value = RelationshipState(
                bond = bond,
                daysTogether = 12,
                conversationsCount = 28,
                totalMessages = 342,
                longestStreak = 7,
                milestones = milestones
            )
        }
    }
}
