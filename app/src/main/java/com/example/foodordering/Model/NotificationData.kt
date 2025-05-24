package com.example.foodordering.Model

data class NotificationData(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val imageResId: Int?,
    val timestamp: Long = System.currentTimeMillis()
)
