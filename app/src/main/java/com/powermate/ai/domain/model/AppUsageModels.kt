package com.powermate.ai.domain.model

data class AppUsageEntry(
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val lastTimeUsed: Long,
    val percentOfTrackedUsage: Float
)
