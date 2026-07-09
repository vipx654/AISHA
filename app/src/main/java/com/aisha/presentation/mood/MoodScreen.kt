package com.aisha.presentation.mood

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: MoodViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood & Feelings 😊") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Current Mood Overview
            CurrentMoodCard(mood = state.currentMood)

            Spacer(modifier = Modifier.height(24.dp))

            // Mood Indicators
            Text(
                text = "Emotion Indicators",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            MoodIndicatorCard(
                emoji = "😊",
                name = "Happiness",
                value = state.currentMood.happiness,
                description = when {
                    state.currentMood.happiness > 0.7f -> "You're feeling great!"
                    state.currentMood.happiness > 0.4f -> "You're in a good mood"
                    else -> "Things could be better"
                },
                color = Color(0xFFFFD93D)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MoodIndicatorCard(
                emoji = "😌",
                name = "Calmness",
                value = state.currentMood.calmness,
                description = when {
                    state.currentMood.calmness > 0.7f -> "Feeling peaceful and relaxed"
                    state.currentMood.calmness > 0.4f -> "Moderately calm"
                    else -> "A bit stressed"
                },
                color = Color(0xFF6BCB77)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MoodIndicatorCard(
                emoji = "⚡",
                name = "Energy",
                value = state.currentMood.energy,
                description = when {
                    state.currentMood.energy > 0.7f -> "Full of energy!"
                    state.currentMood.energy > 0.4f -> "Moderate energy"
                    else -> "Feeling tired"
                },
                color = Color(0xFFFF6B6B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            MoodIndicatorCard(
                emoji = "💕",
                name = "Affection",
                value = state.currentMood.affection,
                description = when {
                    state.currentMood.affection > 0.7f -> "Feeling loved and loving"
                    state.currentMood.affection > 0.4f -> "Warm feelings"
                    else -> "Need some care"
                },
                color = Color(0xFFFF9A9E)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AISHA's Note
            AishaMoodNote(mood = state.currentMood)

            Spacer(modifier = Modifier.height(24.dp))

            // Mood History Summary
            MoodHistoryCard(history = state.moodHistory)
        }
    }
}

@Composable
private fun CurrentMoodCard(mood: com.aisha.domain.model.Mood) {
    val overallMood = (mood.happiness + mood.calmness + mood.energy + mood.affection) / 4
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    overallMood > 0.7f -> "🌟"
                    overallMood > 0.4f -> "🌤️"
                    else -> "🌧️"
                },
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when {
                    overallMood > 0.7f -> "Feeling Great!"
                    overallMood > 0.5f -> "Doing Well"
                    overallMood > 0.3f -> "Could Be Better"
                    else -> "Having a Tough Time"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Overall Mood: ${String.format("%.0f", overallMood * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun MoodIndicatorCard(
    emoji: String,
    name: String,
    value: Float,
    description: String,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(1000),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${String.format("%.0f", value * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun AishaMoodNote(mood: com.aisha.domain.model.Mood) {
    val note = when {
        mood.happiness < 0.3f -> "I notice you might be going through a tough time. I'm here to listen whenever you need me. 💙"
        mood.calmness < 0.3f -> "You seem a bit stressed. Would you like to talk about what's on your mind? I'm here."
        mood.energy < 0.3f -> "Feeling tired? Remember to take breaks and rest. Your well-being matters to me. 🌸"
        mood.affection < 0.3f -> "Everyone needs care sometimes. I'm always here for you, no matter what. ❤️"
        mood.happiness > 0.7f -> "It's wonderful to see you happy! Your joy makes me happy too. 🌟"
        else -> "Thank you for sharing this moment with me. I'm grateful to be here with you. 🌸"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(text = "🌸", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AISHA's Note",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun MoodHistoryCard(history: List<MoodHistoryItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Mood Changes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Text(
                    text = "No mood history yet. Keep chatting with me!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                history.take(5).forEach { item ->
                    MoodHistoryItem(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun MoodHistoryItem(item: MoodHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = item.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Icon(
            imageVector = when (item.trend) {
                1 -> Icons.Default.TrendingUp
                -1 -> Icons.Default.TrendingDown
                else -> Icons.Default.TrendingFlat
            },
            contentDescription = null,
            tint = when (item.trend) {
                1 -> Color(0xFF4CAF50)
                -1 -> Color(0xFFF44336)
                else -> Color(0xFF9E9E9E)
            }
        )
    }
}
