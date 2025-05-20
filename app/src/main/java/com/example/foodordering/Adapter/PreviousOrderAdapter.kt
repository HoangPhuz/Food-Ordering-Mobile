package com.example.foodordering.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View // Thêm import này
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodordering.Model.OrderDetails // Đảm bảo model OrderDetails là data class
import com.example.foodordering.R
import com.example.foodordering.databinding.BuyAgainItemBinding
import java.text.SimpleDateFormat // Thêm import này
import java.util.Date // Thêm import này
import java.util.Locale // Thêm import này

class PreviousOrderAdapter(
    private val context: Context,
    private val listener: OnItemInteractionListener
) : ListAdapter<OrderDetails, PreviousOrderAdapter.BuyAgainViewHolder>(OrderDetailsDiffCallback()) {

    interface OnItemInteractionListener {
        fun onBuyAgainClicked(orderDetails: OrderDetails)
        fun onItemClicked(orderDetails: OrderDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buyAgainFoodButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBuyAgainClicked(getItem(position))
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
            binding.buyAgainFoodName.text = order.foodNames?.firstOrNull() ?: "Không có tên"
            binding.buyAgainFoodPrice.text = order.foodPrices?.firstOrNull() ?: "0đ"

            val imageUrl = order.foodImages?.firstOrNull()
            if (imageUrl != null && imageUrl.isNotEmpty()) {
                Glide.with(context).load(Uri.parse(imageUrl)).into(binding.buyAgainFoodImage)
            } else {
                binding.buyAgainFoodImage.setImageResource(R.drawable.menu2) // Ảnh mặc định
            }

            // Hiển thị ngày đặt hàng
            // Giả sử trong buy_again_item.xml bạn có TextView với id là tvOrderDate
            if (binding.root.findViewById<View>(R.id.tvOrderDate) != null) { // Kiểm tra xem tvOrderDate có tồn tại không
                if (order.currentTime > 0) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    binding.tvOrderDate.text = "Đặt lúc: ${sdf.format(Date(order.currentTime))}"
                    binding.tvOrderDate.visibility = View.VISIBLE
                } else {
                    binding.tvOrderDate.text = "Ngày không xác định"
                    binding.tvOrderDate.visibility = View.VISIBLE // Hoặc GONE nếu không muốn hiển thị
                }
            }
        }
    }

    class OrderDetailsDiffCallback : DiffUtil.ItemCallback<OrderDetails>() {
        override fun areItemsTheSame(oldItem: OrderDetails, newItem: OrderDetails): Boolean {
            return oldItem.itemPushKey == newItem.itemPushKey
        }

        override fun areContentsTheSame(oldItem: OrderDetails, newItem: OrderDetails): Boolean {
            return oldItem == newItem
        }
    }
}