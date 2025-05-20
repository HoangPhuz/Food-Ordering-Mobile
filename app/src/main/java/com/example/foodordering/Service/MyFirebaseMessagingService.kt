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
        // var imageType: String? = null // Nếu bạn gửi loại ảnh từ FCM

        // Ưu tiên xử lý 'data' payload vì nó luôn được gửi đến onMessageReceived
        // ngay cả khi app ở foreground hoặc background.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            notificationTitle = remoteMessage.data["title"]
            notificationBody = remoteMessage.data["body"]
            // imageType = remoteMessage.data["image_type"]
            // Bạn có thể lấy thêm các dữ liệu khác từ remoteMessage.data nếu Cloud Function gửi
            // val orderId = remoteMessage.data["orderId"]
        }

        // Nếu 'notification' payload cũng được gửi (thường FCM tự xử lý khi app ở background)
        // bạn có thể lấy thông tin từ đó nếu 'data' payload không có.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            if (notificationTitle == null) notificationTitle = it.title
            if (notificationBody == null) notificationBody = it.body
        }

        // Chỉ hiển thị thông báo nếu có nội dung
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
        // Tạo PendingIntent, đảm bảo requestCode là duy nhất nếu bạn có nhiều loại thông báo
        // hoặc sử dụng FLAG_IMMUTABLE hoặc FLAG_MUTABLE tùy theo Android S (API 31+)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_ONE_SHOT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, pendingIntentFlag)

        val channelId = getString(R.string.default_notification_channel_id) // Lấy từ strings.xml
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.bell) // Thay bằng icon của bạn
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // Tự động xóa thông báo khi người dùng nhấn vào
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao để hiện head-up notification
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kể từ Android Oreo (API 26), Notification Channel là bắt buộc.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo chung", // Tên channel hiển thị cho người dùng trong cài đặt app
                NotificationManager.IMPORTANCE_HIGH // Mức độ quan trọng
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Hiển thị thông báo. Sử dụng một ID thông báo duy nhất nếu bạn muốn cập nhật thông báo này sau
        // hoặc nhiều ID khác nhau nếu muốn hiển thị nhiều thông báo riêng biệt.
        notificationManager.notify(System.currentTimeMillis().toInt() /* ID thông báo, nên duy nhất */, notificationBuilder.build())
    }
    companion object { // <--- THÊM COMPANION OBJECT
        private const val COMPANION_TAG = "UserFCMStatic" // TAG riêng cho companion

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
