package com.example.foodordering.ViewModel // Hoặc package phù hợp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.foodordering.Model.NotificationData
import com.example.foodordering.R // Giả sử bạn có R

class NotificationViewModel : ViewModel() {

    // _notificationsList giữ danh sách thông báo có thể thay đổi (Mutable)
    // Chỉ ViewModel này mới được phép sửa đổi trực tiếp danh sách này.
    private val _notificationsList = MutableLiveData<List<NotificationData>>(emptyList())

    // notificationsList là LiveData không thể thay đổi từ bên ngoài (Immutable)
    // Các thành phần UI (như Fragment) sẽ quan sát LiveData này.
    val notificationsList: LiveData<List<NotificationData>>
        get() = _notificationsList

    // Danh sách tạm thời để quản lý thông báo trước khi cập nhật LiveData
    private val currentNotifications = mutableListOf<NotificationData>()

    // Phương thức để thêm một thông báo mới
    fun addNotification(notification: NotificationData) {
        currentNotifications.add(0, notification) // Thêm vào đầu danh sách để hiển thị mới nhất lên trên
        // Giới hạn số lượng thông báo hiển thị nếu cần (ví dụ: 50 thông báo gần nhất)
        // if (currentNotifications.size > 50) {
        //     currentNotifications.removeLastOrNull()
        // }
        _notificationsList.value = ArrayList(currentNotifications) // Cập nhật LiveData với một bản sao mới của danh sách
    }

    // Phương thức để xóa tất cả thông báo
    fun clearAllNotifications() {
        currentNotifications.clear()
        _notificationsList.value = ArrayList(currentNotifications)
    }

    // Ví dụ: Phương thức để thêm một thông báo mẫu (có thể gọi từ nơi khác trong app)
    fun addSampleNotification(message: String, imageResId: Int?) {
        val newNotification = NotificationData(
            title = "Thông báo mới", // Bạn có thể tùy chỉnh tiêu đề
            message = message,
            imageResId = imageResId
        )
        addNotification(newNotification)
    }
}
