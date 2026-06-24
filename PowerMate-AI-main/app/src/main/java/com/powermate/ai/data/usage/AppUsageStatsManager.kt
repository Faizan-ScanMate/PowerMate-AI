package com.powermate.ai.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Process
import androidx.core.graphics.drawable.toBitmap
import com.powermate.ai.domain.model.AppUsageEntry
import java.util.Calendar
import kotlin.math.max

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

    fun topAppsSince(hours: Int = 24, limit: Int = 10): List<AppUsageEntry> {
        if (!hasUsageAccess()) return emptyList()

        val end = System.currentTimeMillis()
        val start24h = end - hours.coerceAtLeast(1) * 60L * 60L * 1000L
        val startToday = startOfTodayMillis(end)
        val packageManager = context.packageManager

        val last24h = collectForegroundUsage(start24h, end)
        val today = collectForegroundUsage(startToday, end)
        val fallback = collectFallbackUsageStats(start24h, end)

        val packageNames = (last24h.keys + today.keys + fallback.keys)
            .filter { it != context.packageName }
            .distinct()

        val merged = packageNames.mapNotNull { packageName ->
            val appInfo = packageManager.safeApplicationInfo(packageName) ?: return@mapNotNull null
            val launchable = packageManager.getLaunchIntentForPackage(packageName) != null
            val last24hStats = last24h[packageName]
            val todayStats = today[packageName]
            val fallbackStats = fallback[packageName]
            val last24hMs = max(
                last24hStats?.foregroundMs ?: 0L,
                fallbackStats?.foregroundMs ?: 0L
            )
            val todayMs = todayStats?.foregroundMs ?: 0L
            val foregroundMs = last24hMs.takeIf { it > 0L } ?: todayMs
            if (foregroundMs <= 0L) return@mapNotNull null
            if (!launchable && foregroundMs < 2 * 60_000L && isSystemPackage(packageName, appInfo)) return@mapNotNull null

            val lastUsed = max(
                max(last24hStats?.lastUsed ?: 0L, todayStats?.lastUsed ?: 0L),
                fallbackStats?.lastUsed ?: 0L
            )
            val launches = max(last24hStats?.launchCount ?: 0, todayStats?.launchCount ?: 0)

            AppUsageEntry(
                packageName = packageName,
                appName = packageManager.safeAppName(packageName),
                foregroundTimeMs = foregroundMs,
                lastTimeUsed = lastUsed,
                percentOfTrackedUsage = 0f,
                iconBitmap = packageManager.safeIconBitmap(packageName),
                todayTimeMs = todayMs,
                last24hTimeMs = last24hMs,
                categoryLabel = categoryLabel(appInfo, packageName),
                launchCount = launches,
                isSystemApp = isSystemPackage(packageName, appInfo),
                drainHint = buildDrainHint(foregroundMs, launches, categoryLabel(appInfo, packageName))
            )
        }.sortedByDescending { it.last24hTimeMs }

        val total = merged.sumOf { it.last24hTimeMs }.takeIf { it > 0L }
            ?: merged.sumOf { it.foregroundTimeMs }.takeIf { it > 0L }
            ?: return emptyList()

        return merged
            .map { entry ->
                entry.copy(percentOfTrackedUsage = (entry.last24hTimeMs * 100f) / total)
            }
            .take(limit.coerceAtLeast(1))
    }

    private fun collectForegroundUsage(start: Long, end: Long): Map<String, UsageAccumulator> {
        val result = mutableMapOf<String, UsageAccumulator>()
        val activeStarts = mutableMapOf<String, Long>()
        val events = runCatching { usageStatsManager.queryEvents(start, end) }.getOrNull() ?: return emptyMap()
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val packageName = event.packageName ?: continue
            if (packageName == context.packageName) continue

            when {
                isForegroundEvent(event.eventType) -> {
                    activeStarts[packageName] = event.timeStamp
                    result.getOrPut(packageName) { UsageAccumulator() }.apply {
                        launchCount += 1
                        lastUsed = max(lastUsed, event.timeStamp)
                    }
                }
                isBackgroundEvent(event.eventType) -> {
                    val startTime = activeStarts.remove(packageName) ?: continue
                    val duration = (event.timeStamp - startTime).coerceIn(0L, 12 * 60L * 60L * 1000L)
                    result.getOrPut(packageName) { UsageAccumulator() }.apply {
                        foregroundMs += duration
                        lastUsed = max(lastUsed, event.timeStamp)
                    }
                }
            }
        }

        activeStarts.forEach { (packageName, startTime) ->
            val duration = (end - startTime).coerceIn(0L, 12 * 60L * 60L * 1000L)
            result.getOrPut(packageName) { UsageAccumulator() }.apply {
                foregroundMs += duration
                lastUsed = max(lastUsed, end)
            }
        }

        return result
    }

    private fun collectFallbackUsageStats(start: Long, end: Long): Map<String, UsageAccumulator> {
        return runCatching {
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end)
                .orEmpty()
                .filter { it.totalTimeInForeground > 0L && it.packageName != context.packageName }
                .groupBy { it.packageName }
                .mapValues { (_, stats) ->
                    UsageAccumulator(
                        foregroundMs = stats.sumOf { it.totalTimeInForeground },
                        lastUsed = stats.maxOf { it.lastTimeUsed },
                        launchCount = 0
                    )
                }
        }.getOrDefault(emptyMap())
    }

    private fun isForegroundEvent(type: Int): Boolean {
        return type == UsageEvents.Event.MOVE_TO_FOREGROUND ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && type == UsageEvents.Event.ACTIVITY_RESUMED)
    }

    private fun isBackgroundEvent(type: Int): Boolean {
        return type == UsageEvents.Event.MOVE_TO_BACKGROUND ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                (type == UsageEvents.Event.ACTIVITY_PAUSED || type == UsageEvents.Event.ACTIVITY_STOPPED))
    }

    private fun startOfTodayMillis(now: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun PackageManager.safeApplicationInfo(packageName: String): ApplicationInfo? = runCatching {
        getApplicationInfo(packageName, 0)
    }.getOrNull()

    private fun PackageManager.safeAppName(packageName: String): String = runCatching {
        val info = getApplicationInfo(packageName, 0)
        getApplicationLabel(info).toString().takeIf { it.isNotBlank() } ?: packageName
    }.getOrElse {
        packageName.substringAfterLast('.').replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }

    private fun PackageManager.safeIconBitmap(packageName: String): Bitmap? = runCatching {
        getApplicationIcon(packageName).toBitmap(width = 96, height = 96, config = Bitmap.Config.ARGB_8888)
    }.getOrNull()

    private fun isSystemPackage(packageName: String, info: ApplicationInfo): Boolean {
        // Never filter user-visible apps — even if they are system-signed
        val knownUserApps = setOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.facebook.lite",
            "com.twitter.android",
            "com.snapchat.android",
            "com.whatsapp",
            "com.tiktok.android",
            "com.zhiliaoapp.musically",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.google.android.gm",
            "com.google.android.apps.maps",
            "com.google.android.apps.photos",
            "com.samsung.android.app.galaxyfinder",
            "com.sec.android.app.sbrowser",
            "com.android.chrome"
        )
        if (packageName in knownUserApps) return false

        // Only filter true invisible background processes
        val invisiblePackages = listOf(
            "android",
            "com.android.systemui",
            "com.android.permissioncontroller",
            "com.google.android.gms",
            "com.google.android.gsf",
            "com.samsung.android.lool",
            "com.samsung.android.spay",
            "com.android.vending.billing"
        )
        if (invisiblePackages.any { packageName == it }) return true

        // Launchers DO show in usage — Samsung One UI Home is a real app the user sees
        // Do NOT filter by "launcher" string — that was the bug
        val hasLaunchIntent = context.packageManager.getLaunchIntentForPackage(packageName) != null
        if (hasLaunchIntent) return false

        // Filter pure background system processes with no launch intent
        val systemFlag = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        val noLaunchIntent = context.packageManager.getLaunchIntentForPackage(packageName) == null
        return systemFlag && noLaunchIntent
    }

    private fun categoryLabel(info: ApplicationInfo, packageName: String = ""): String {
        // Override by known package — system category is often wrong
        val knownCategories = mapOf(
            "com.google.android.youtube" to "Video",
            "com.netflix.mediaclient" to "Video",
            "com.spotify.music" to "Audio",
            "com.instagram.android" to "Social",
            "com.facebook.katana" to "Social",
            "com.facebook.lite" to "Social",
            "com.twitter.android" to "Social",
            "com.snapchat.android" to "Social",
            "com.whatsapp" to "Social",
            "com.tiktok.android" to "Video",
            "com.zhiliaoapp.musically" to "Video",
            "com.google.android.apps.maps" to "Maps",
            "com.android.chrome" to "App",
            "com.sec.android.app.sbrowser" to "App"
        )
        knownCategories[packageName]?.let { return it }

        return when (info.category) {
            ApplicationInfo.CATEGORY_GAME -> "Game"
            ApplicationInfo.CATEGORY_AUDIO -> "Audio"
            ApplicationInfo.CATEGORY_VIDEO -> "Video"
            ApplicationInfo.CATEGORY_IMAGE -> "Media"
            ApplicationInfo.CATEGORY_SOCIAL -> "Social"
            ApplicationInfo.CATEGORY_NEWS -> "News"
            ApplicationInfo.CATEGORY_MAPS -> "Maps"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
            else -> "App"
        }
    }

    private fun buildDrainHint(durationMs: Long, launches: Int, category: String): String {
        val hours = (durationMs / 3_600_000f).toInt()
        return when {
            durationMs >= 3 * 60L * 60L * 1000L && category == "Game" ->
                "High drain — gaming ${hours}h+ raises heat and battery use"
            durationMs >= 3 * 60L * 60L * 1000L ->
                "Major screen time — likely top battery consumer today"
            launches >= 20 ->
                "Opened ${launches}x — frequent switching drains battery"
            category == "Video" && durationMs >= 45 * 60_000L ->
                "Screen-on video — significant display and CPU drain"
            category == "Social" && durationMs >= 60 * 60_000L ->
                "Social apps keep screen, network and sensors active"
            category == "Game" && durationMs >= 30 * 60_000L ->
                "Gaming raises CPU heat — watch charging temperature"
            durationMs >= 60 * 60_000L ->
                "1h+ foreground — moderate battery impact"
            else ->
                "Light use — minimal battery impact"
        }
    }

    private data class UsageAccumulator(
        var foregroundMs: Long = 0L,
        var lastUsed: Long = 0L,
        var launchCount: Int = 0
    )
}
