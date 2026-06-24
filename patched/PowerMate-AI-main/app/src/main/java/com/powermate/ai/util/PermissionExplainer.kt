package com.powermate.ai.util

object PermissionExplainer {
    const val NOTIFICATION_REASON = "Used only for battery alerts such as 80%, full charge, slow charging, or overheat warnings."
    const val OVERLAY_REASON = "Used only when the user enables AOD-style charging display overlay."
    const val BOOT_REASON = "Used to restore user-enabled charging alerts after device restart."

    val privacyPromises = listOf(
        "No account required",
        "Battery data stays on device",
        "No cloud sync in v1",
        "All core charging tools included"
    )
}
