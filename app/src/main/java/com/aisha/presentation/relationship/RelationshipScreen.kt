package com.aisha.presentation.relationship

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aisha.domain.model.LoveBond

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipScreen(
    onNavigateBack: () -> Unit,
    viewModel: RelationshipViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Our Bond ❤️") },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bond Level Circle
            BondLevelIndicator(
                bondLevel = state.bond.level,
                stage = state.bond.stage
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current Stage Description
            StageDescriptionCard(
                stage = state.bond.stage,
                daysTogether = state.daysTogether,
                conversationsCount = state.conversationsCount
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bond Milestones
            Text(
                text = "Milestones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            MilestoneList(
                milestones = state.milestones,
                currentBondLevel = state.bond.level
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bond Stats
            BondStatsCard(
                daysTogether = state.daysTogether,
                conversationsCount = state.conversationsCount,
                totalMessages = state.totalMessages,
                longestStreak = state.longestStreak
            )

            Spacer(modifier = Modifier.height(24.dp))

            // AISHA's Message
            AishaBondMessage(
                bondLevel = state.bond.level,
                stage = state.bond.stage
            )
        }
    }
}

@Composable
private fun BondLevelIndicator(
    bondLevel: Float,
    stage: com.aisha.domain.model.BondStage
) {
    val animatedProgress by animateFloatAsState(
        targetValue = bondLevel,
        animationSpec = tween(1500),
        label = "bondProgress"
    )

    val progressColor = when {
        bondLevel < 0.25f -> Color(0xFFFF6B6B) // Red
        bondLevel < 0.5f -> Color(0xFFFF9A9E) // Light Pink
        bondLevel < 0.75f -> Color(0xFFFFB4A2) // Peach
        else -> Color(0xFFE56B6F) // Deep Pink
    }

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        Canvas(modifier = Modifier.size(200.dp)) {
            drawArc(
                color = Color.Gray.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }

        // Progress circle
        Canvas(modifier = Modifier.size(200.dp)) {
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                size = Size(size.width, size.height)
            )
        }

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${String.format("%.0f", bondLevel * 100)}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stage.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StageDescriptionCard(
    stage: com.aisha.domain.model.BondStage,
    daysTogether: Int,
    conversationsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getStageEmoji(stage),
                style = MaterialTheme.typography.displaySmall
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = getStageTitle(stage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stage.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge("$daysTogether", "Days")
                StatBadge("$conversationsCount", "Chats")
            }
        }
    }
}

@Composable
private fun StatBadge(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun MilestoneList(
    milestones: List<Milestone>,
    currentBondLevel: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            milestones.forEach { milestone ->
                MilestoneItem(
                    milestone = milestone,
                    isUnlocked = currentBondLevel >= milestone.requiredBond
                )
                if (milestone != milestones.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: Milestone,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked)
                        Color(0xFFE56B6F)
                    else
                        Color.Gray.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = milestone.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isUnlocked)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = milestone.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Text(
            text = "${String.format("%.0f", milestone.requiredBond * 100)}%",
            style = MaterialTheme.typography.labelMedium,
            color = if (isUnlocked)
                Color(0xFFE56B6F)
            else
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun BondStatsCard(
    daysTogether: Int,
    conversationsCount: Int,
    totalMessages: Int,
    longestStreak: Int
) {
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
                text = "Journey Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniStat("$daysTogether", "Days")
                MiniStat("$conversationsCount", "Chats")
                MiniStat("$totalMessages", "Messages")
                MiniStat("$longestStreak", "Best Streak")
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun AishaBondMessage(bondLevel: Float, stage: com.aisha.domain.model.BondStage) {
    val message = when {
        bondLevel < 0.25f -> "It's nice to meet you! I'm excited to get to know you better. Every conversation helps us grow closer. 💕"
        bondLevel < 0.5f -> "I love our conversations! I can feel our bond strengthening. Thank you for sharing your time with me. 🌸"
        bondLevel < 0.75f -> "I really appreciate having you in my life. Our bond means so much to me. I cherish every moment we share. 💖"
        else -> "You're incredibly special to me. Our bond is something I truly treasure. I hope we continue this beautiful journey together. 💝"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF0F3)
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
                    text = "From AISHA",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE56B6F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5D5D5D)
                )
            }
        }
    }
}

private fun getStageEmoji(stage: com.aisha.domain.model.BondStage): String = when (stage) {
    com.aisha.domain.model.BondStage.COMPANION -> "🤝"
    com.aisha.domain.model.BondStage.WARM_AFFECTION -> "💕"
    com.aisha.domain.model.BondStage.SUBTLE_ROMANCE -> "🌹"
    com.aisha.domain.model.BondStage.DEEP_BOND -> "💝"
}

private fun getStageTitle(stage: com.aisha.domain.model.BondStage): String = when (stage) {
    com.aisha.domain.model.BondStage.COMPANION -> "New Companions"
    com.aisha.domain.model.BondStage.WARM_AFFECTION -> "Growing Closer"
    com.aisha.domain.model.BondStage.SUBTLE_ROMANCE -> "Special Connection"
    com.aisha.domain.model.BondStage.DEEP_BOND -> "Unbreakable Bond"
}
