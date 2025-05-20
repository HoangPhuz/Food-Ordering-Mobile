package com.example.foodordering.Adapter // Package của app User

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Sử dụng ListAdapter cho hiệu quả
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R // R của app User
import com.example.foodordering.databinding.PendingOrderItemBinding // Layout item của User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PendingOrderAdapter( // Đảm bảo đây là adapter cho User
    private val context: Context,
    private val listener: OnItemInteractionListener
    // Không cần truyền list vào constructor nếu dùng ListAdapter
) : ListAdapter<OrderDetails, PendingOrderAdapter.PendingOrderViewHolder>(OrderDetailsDiffCallback()) {

    interface OnItemInteractionListener {
        fun onReceivedButtonClicked(orderDetails: OrderDetails, position: Int)
        fun onItemClicked(orderDetails: OrderDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        // Đảm bảo bạn đang dùng đúng layout item cho User app
        // Tên binding có thể khác (ví dụ: UserPendingOrderItemBinding)
        val binding = PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class PendingOrderViewHolder(private val binding: PendingOrderItemBinding) : // Sử dụng đúng binding
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.receivedButton.setOnClickListener { // Giả sử nút trong layout của User là 'receivedButton'
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = getItem(position)
                    if (order.orderDispatched) { // Chỉ cho phép click nếu đã dispatched
                        listener.onReceivedButtonClicked(order, position)
                    }
                }
            }
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(getItem(position))
                }
            }
        }

        fun bind(order: OrderDetails) {
            // Giả sử binding của User có các ID tương ứng
            binding.foodName.text = order.foodNames?.joinToString(", ") ?: "N/A"
            binding.foodPrice.text = order.totalPrice ?: "0đ"

            val imageUrl = order.foodImages?.firstOrNull()
            if (imageUrl != null) {
                Glide.with(context).load(Uri.parse(imageUrl)).into(binding.foodImage)
            } else {
                binding.foodImage.setImageResource(R.drawable.menu2) // Ảnh mặc định
            }

            if (order.currentTime > 0) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                binding.tvOrderDate.text = "Đặt lúc: ${sdf.format(Date(order.currentTime))}"
            } else {
                binding.tvOrderDate.text = "Ngày không xác định"
            }

            // Xử lý trạng thái nút "Đã nhận"
            if (order.orderAccepted && !order.paymentReceived) { // Điều kiện cơ bản để hiển thị nút
                binding.receivedButton.visibility = View.VISIBLE
                if (order.orderDispatched) {
                    binding.receivedButton.isEnabled = true
                    binding.receivedButton.alpha = 1.0f // Bình thường
                    // binding.receivedButton.text = "Đã nhận hàng" // Hoặc giữ nguyên
                } else {
                    binding.receivedButton.isEnabled = false
                    binding.receivedButton.alpha = 0.5f // Làm mờ nút
                    // binding.receivedButton.text = "Chờ gửi hàng" // Hoặc thông báo khác
                }
            } else {
                binding.receivedButton.visibility = View.GONE // Ẩn nếu đã nhận hoặc chưa chấp nhận
            }
        }
    }

    class OrderDetailsDiffCallback : DiffUtil.ItemCallback<OrderDetails>() {
        override fun areItemsTheSame(oldItem: OrderDetails, newItem: OrderDetails): Boolean {
            return oldItem.itemPushKey == newItem.itemPushKey
        }

        override fun areContentsTheSame(oldItem: OrderDetails, newItem: OrderDetails): Boolean {
            return oldItem == newItem // So sánh toàn bộ object, đảm bảo OrderDetails có data class hoặc equals/hashCode đúng
        }
    }
}
