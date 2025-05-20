package com.example.foodordering.Model // Hoặc package phù hợp của bạn

data class NotificationData(
    val id: String = System.currentTimeMillis().toString(), // ID duy nhất cho mỗi thông báo
    val title: String, // Tiêu đề thông báo (nếu có)
    val message: String, // Nội dung chính của thông báo
    val imageResId: Int?, // ID của hình ảnh (có thể null nếu không có ảnh)
    val timestamp: Long = System.currentTimeMillis() // Thời gian nhận thông báo
)
