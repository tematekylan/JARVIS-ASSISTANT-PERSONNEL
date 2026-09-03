package com.example.service

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class InterceptedNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class THackNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "THackNotifListener"
        private val _notificationsFlow = MutableSharedFlow<InterceptedNotification>(extraBufferCapacity = 64)
        val notificationsFlow: SharedFlow<InterceptedNotification> = _notificationsFlow.asSharedFlow()

        var isServiceConnected: Boolean = false
            private set

        fun getAppFriendlyName(context: Context, packageName: String): String {
            return when (packageName) {
                "com.whatsapp" -> "WhatsApp"
                "com.facebook.orca" -> "Messenger"
                "com.google.android.gm" -> "Gmail"
                "com.google.android.youtube" -> "YouTube"
                "com.instagram.android" -> "Instagram"
                "org.telegram.messenger" -> "Telegram"
                "com.twitter.android", "com.x.android" -> "X (Twitter)"
                "com.google.android.apps.messaging" -> "Messages SMS"
                else -> {
                    try {
                        val pm = context.packageManager
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        packageName.substringAfterLast(".")
                    }
                }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceConnected = true
        Log.d(TAG, "T-HACK Notification Listener Service Connected.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceConnected = false
        Log.d(TAG, "T-HACK Notification Listener Service Disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        // Ignore self-notifications to avoid infinite loops
        if (packageName == applicationContext.packageName) return

        val extras = sbn.notification?.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()
            ?: ""

        // Skip empty or purely ongoing progress notifications
        if (title.isBlank() && text.isBlank()) return
        if (sbn.isOngoing) return

        val appName = getAppFriendlyName(applicationContext, packageName)

        val intercepted = InterceptedNotification(
            id = "${sbn.id}_${sbn.postTime}",
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            timestamp = sbn.postTime
        )

        Log.d(TAG, "Captured notification from $appName: $title - $text")
        scope.launch {
            _notificationsFlow.emit(intercepted)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
