package com.powermate.ai.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import com.powermate.ai.domain.model.AppUsageEntry

class AppUsageStatsManager(private val context: Context) {
    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun topAppsSince(hours: Int = 24, limit: Int = 8): List<AppUsageEntry> {
        if (!hasUsageAccess()) return emptyList()
        val end = System.currentTimeMillis()
        val start = end - hours.coerceAtLeast(1) * 60L * 60L * 1000L
        val stats = usageStatsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
            .orEmpty()
            .filter { it.totalTimeInForeground > 0L && it.packageName != context.packageName }

        val total = stats.sumOf { it.totalTimeInForeground }.takeIf { it > 0L } ?: return emptyList()
        val packageManager = context.packageManager

        return stats
            .groupBy { it.packageName }
            .map { (packageName, appStats) ->
                val foregroundTime = appStats.sumOf { it.totalTimeInForeground }
                val lastUsed = appStats.maxOf { it.lastTimeUsed }
                AppUsageEntry(
                    packageName = packageName,
                    appName = packageManager.safeAppName(packageName),
                    foregroundTimeMs = foregroundTime,
                    lastTimeUsed = lastUsed,
                    percentOfTrackedUsage = (foregroundTime * 100f) / total
                )
            }
            .sortedByDescending { it.foregroundTimeMs }
            .take(limit.coerceAtLeast(1))
    }

    private fun PackageManager.safeAppName(packageName: String): String = runCatching {
        val info = getApplicationInfo(packageName, 0)
        getApplicationLabel(info).toString()
    }.getOrElse {
        packageName.substringAfterLast('.').replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}
