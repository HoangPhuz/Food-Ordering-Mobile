package com.example.foodordering.Service // Hoặc package phù hợp

import android.util.Log
import com.example.foodordering.Model.NotificationData
import com.example.foodordering.R
import com.example.foodordering.ViewModel.NotificationViewModel // Cần cách để truy cập ViewModel
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Quan trọng: Để truy cập ViewModel từ một Service (không có vòng đời UI trực tiếp),
// bạn cần một cơ chế phù hợp. Các cách phổ biến:
// 1. Singleton ViewModel (Không khuyến khích cho ViewModel có state UI).
// 2. Sử dụng một Repository làm trung gian, Service ghi vào Repository, ViewModel đọc từ Repository.
// 3. Gửi Local Broadcast từ Service, Activity/Fragment lắng nghe và cập nhật ViewModel.
// 4. Sử dụng Dependency Injection (Hilt, Koin) để inject một đối tượng có thể giao tiếp với ViewModel.

// Ví dụ này sẽ sử dụng một cách đơn giản hóa để minh họa,
// nhưng trong ứng dụng thực tế, hãy cân nhắc các giải pháp kiến trúc tốt hơn.
// Giả sử bạn có một cách để lấy instance của NotificationViewModel, ví dụ qua một Singleton (KHÔNG KHUYẾN KHÍCH):
object ViewModelLocator {
    var notificationViewModel: NotificationViewModel? = null
}


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Kiểm tra xem message có chứa data payload không.
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            // Xử lý data payload ở đây.
            // Ví dụ: lấy title, body, image_type từ remoteMessage.data
            val title = remoteMessage.data["title"] ?: "Thông báo"
            val body = remoteMessage.data["body"] ?: "Bạn có thông báo mới."
            val imageType = remoteMessage.data["image_type"] // Ví dụ: "order_success", "promo"

            // Tạo NotificationData
            val imageResId = when (imageType) {
                "order_success" -> R.drawable.congrats // Thay bằng icon thực tế của bạn
                "order_driver" -> R.drawable.truck   // Thay bằng icon thực tế của bạn
                "order_cancelled" -> R.drawable.sademoji // Thay bằng icon thực tế của bạn
                // Thêm các trường hợp khác nếu cần
                else -> null // Không có ảnh hoặc ảnh mặc định
            }

            val newNotification = NotificationData(
                title = title,
                message = body,
                imageResId = imageResId
            )

            // Gửi thông báo này đến ViewModel để cập nhật UI
            // CẢNH BÁO: Đoạn code dưới đây sử dụng ViewModelLocator là một cách đơn giản hóa.
            // Trong ứng dụng lớn, hãy sử dụng Repository, EventBus, hoặc DI.
            ViewModelLocator.notificationViewModel?.addNotification(newNotification)

            // Bạn cũng có thể hiển thị một System Notification ở đây nếu ứng dụng không mở
            // hoặc nếu bạn muốn thông báo ngay cả khi BottomSheet không hiển thị.
            // sendSystemNotification(title, body)
        }

        // Kiểm tra xem message có chứa notification payload không.
        // (Thường được xử lý tự động bởi FCM khi app ở background,
        // nhưng bạn có thể tùy chỉnh nếu muốn)
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            // Nếu bạn muốn xử lý notification payload này theo cách riêng khi app ở foreground
            // (ví dụ, không để FCM tự hiển thị mà dùng logic của bạn)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Gửi token này lên server của bạn để có thể gửi thông báo đến thiết bị này.
        // sendRegistrationToServer(token)
    }

    // private fun sendSystemNotification(title: String, messageBody: String) {
    //     // Code để tạo và hiển thị một System Notification (sử dụng NotificationManagerCompat)
    // }
}
