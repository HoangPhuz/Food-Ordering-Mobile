package com.example.foodordering.Service // Hoặc package của bạn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.foodordering.MainActivity // Activity sẽ mở khi nhấn vào thông báo
import com.example.foodordering.R // Chứa icon của bạn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
// import com.example.foodordering.Model.NotificationData // Bạn có thể dùng để cấu trúc dữ liệu
// import com.example.foodordering.ViewModel.NotificationViewModel // Không cần thiết cho chỉ hiển thị system notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        var notificationTitle: String? = null
        var notificationBody: String? = null

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            notificationTitle = remoteMessage.data["title"]
            notificationBody = remoteMessage.data["body"]

        }


        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            if (notificationTitle == null) notificationTitle = it.title
            if (notificationBody == null) notificationBody = it.body
        }


        if (notificationTitle != null && notificationBody != null) {
            sendSystemNotification(notificationTitle.toString(), notificationBody.toString())
        } else {
            Log.d(TAG, "Notification title or body is null. Not showing notification.")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token for User: $token")
        sendUserFCMTokenToDatabase(token)
    }
    private fun sendUserFCMTokenToDatabase(token: String?) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && token != null) {
            val tokenRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("fcmToken")
            tokenRef.setValue(token)
                .addOnSuccessListener { Log.d(TAG, "User FCM Token updated for $userId") }
                .addOnFailureListener { e -> Log.e(TAG, "Failed to update User FCM Token: ${e.message}") }
        }
    }

    private fun sendSystemNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java) // Activity sẽ mở khi người dùng nhấn vào thông báo
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, pendingIntentFlag)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bell)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo chung",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }


        notificationManager.notify(System.currentTimeMillis().toInt() , notificationBuilder.build())
    }
    companion object {
        private const val COMPANION_TAG = "UserFCMStatic"

        fun sendUserFCMTokenToDatabase(token: String?) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null && token != null) {
                val tokenRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId).child("fcmToken")
                tokenRef.setValue(token)
                    .addOnSuccessListener { Log.d(COMPANION_TAG, "User FCM Token updated for $userId") }
                    .addOnFailureListener { e -> Log.e(COMPANION_TAG, "Failed to update User FCM Token for $userId: ${e.message}") }
            } else {
                Log.w(COMPANION_TAG, "Cannot send FCM token: User not logged in or token is null.")
            }
        }
    }
}
