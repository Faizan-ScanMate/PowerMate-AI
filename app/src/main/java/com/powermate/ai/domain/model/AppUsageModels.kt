package com.powermate.ai.domain.model

import android.graphics.Bitmap

data class AppUsageEntry(
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val lastTimeUsed: Long,
    val percentOfTrackedUsage: Float,
    val iconBitmap: Bitmap? = null,
    val todayTimeMs: Long = foregroundTimeMs,
    val last24hTimeMs: Long = foregroundTimeMs,
    val categoryLabel: String = "App",
    val launchCount: Int = 0,
    val isSystemApp: Boolean = false,
    val drainHint: String = "Local foreground activity"
)
