package com.example.foodordering.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.Model.NotificationData // Import model mới
import com.example.foodordering.R // Giả sử bạn có R
import com.example.foodordering.databinding.NotificationItemBinding

// Sử dụng ListAdapter để xử lý cập nhật danh sách hiệu quả hơn với DiffUtil
class NotificationAdapter : ListAdapter<NotificationData, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notificationItem = getItem(position)
        holder.bind(notificationItem)
    }

    inner class NotificationViewHolder(private val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationData) {
            binding.apply {
                notificationTextView.text = notification.message // Hiển thị nội dung thông báo
                // notificationTitleTextView.text = notification.title // Nếu bạn có TextView cho tiêu đề

                if (notification.imageResId != null) {
                    notificationImageView.setImageResource(notification.imageResId)
                    notificationImageView.visibility = View.VISIBLE
                } else {
                    // Nếu không có ảnh, bạn có thể ẩn ImageView hoặc đặt ảnh mặc định
                    notificationImageView.visibility = View.GONE
                    // notificationImageView.setImageResource(R.drawable.default_notification_icon) // Ví dụ
                }
            }
        }
    }

    // DiffUtil giúp RecyclerView cập nhật hiệu quả chỉ những item thay đổi
    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationData>() {
        override fun areItemsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem.id == newItem.id // So sánh dựa trên ID duy nhất
        }

        override fun areContentsTheSame(oldItem: NotificationData, newItem: NotificationData): Boolean {
            return oldItem == newItem // So sánh toàn bộ nội dung object
        }
    }
}
