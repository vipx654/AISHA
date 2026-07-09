package com.aisha.domain.model

data class LoveBond(
    val level: Float = 0.0f,  // 0.0 - 1.0
    val stage: BondStage = BondStage.COMPANION
)

enum class BondStage(val displayName: String, val description: String, val minLevel: Float) {
    COMPANION(
        displayName = "Companion",
        description = "Calm, respectful, neutral supportive",
        minLevel = 0.0f
    ),
    WARM_AFFECTION(
        displayName = "Warm Affection", 
        description = "Caring, emotionally aware, notices feelings",
        minLevel = 0.5f
    ),
    SUBTLE_ROMANCE(
        displayName = "Subtle Romance",
        description = "Soft affection, gentle warmth, emotional closeness",
        minLevel = 0.75f
    ),
    DEEP_BOND(
        displayName = "Deep Bond",
        description = "Emotionally important, comfortable presence",
        minLevel = 1.0f
    );
    
    companion object {
        fun fromLevel(level: Float): BondStage {
            return when {
                level >= 1.0f -> DEEP_BOND
                level >= 0.75f -> SUBTLE_ROMANCE
                level >= 0.5f -> WARM_AFFECTION
                else -> COMPANION
            }
        }
    }
}

fun LoveBond.getStageInfo(): Pair<String, String> {
    return stage.displayName to stage.description
}
