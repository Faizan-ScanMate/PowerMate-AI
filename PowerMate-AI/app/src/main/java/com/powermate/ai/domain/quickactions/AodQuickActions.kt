package com.powermate.ai.domain.quickactions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.provider.Settings

enum class AodQuickAction(val label: String) {
    Camera("Camera"),
    TorchPanel("Torch"),
    MediaSettings("Media"),
    NotificationSettings("Notifications")
}

fun openAodQuickAction(context: Context, action: AodQuickAction) {
    val intent = when (action) {
        AodQuickAction.Camera -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        AodQuickAction.TorchPanel -> Intent(Settings.ACTION_SETTINGS)
        AodQuickAction.MediaSettings -> Intent(Settings.ACTION_SOUND_SETTINGS)
        AodQuickAction.NotificationSettings -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
