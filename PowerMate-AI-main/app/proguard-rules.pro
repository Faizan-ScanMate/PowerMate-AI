# PowerMate AI keeps the release build small while preserving Android components.
-keep class com.powermate.ai.** extends android.app.Service { *; }
-keep class com.powermate.ai.** extends android.content.BroadcastReceiver { *; }
-keep class com.powermate.ai.** extends android.appwidget.AppWidgetProvider { *; }
-keep class com.powermate.ai.** extends android.app.Activity { *; }
