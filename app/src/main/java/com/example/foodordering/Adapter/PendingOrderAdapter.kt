package com.example.foodordering.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R
import com.example.foodordering.databinding.PendingOrderItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PendingOrderAdapter(
    private val context: Context,
    private var pendingOrders: MutableList<OrderDetails>,
    private val listener: OnItemInteractionListener
) : RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder>() {

    interface OnItemInteractionListener {
        fun onReceivedButtonClicked(orderDetails: OrderDetails, position: Int)
        fun onItemClicked(orderDetails: OrderDetails) // Để xem chi tiết đơn hàng
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingOrderViewHolder {
        val binding = PendingOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingOrderViewHolder, position: Int) {
        val order = pendingOrders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = pendingOrders.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < pendingOrders.size) {
            pendingOrders.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, pendingOrders.size) // Cập nhật vị trí các item còn lại
        }
    }

    fun addItem(order: OrderDetails) {
        pendingOrders.add(0, order) // Thêm vào đầu danh sách
        notifyItemInserted(0)
        notifyItemRangeChanged(0, pendingOrders.size)
    }

    fun updateList(newList: List<OrderDetails>) {
        val itemsToActuallyAdd = ArrayList(newList)
        this.pendingOrders.clear()
        this.pendingOrders.addAll(itemsToActuallyAdd)
        notifyDataSetChanged()
    }


    inner class PendingOrderViewHolder(private val binding: PendingOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.receivedButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onReceivedButtonClicked(pendingOrders[position], position)
                }
            }
            binding.root.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(pendingOrders[position])
                }
            }
        }

        fun bind(order: OrderDetails) {
            binding.foodName.text = order.foodNames?.joinToString(", ") ?: "Không có tên"
            binding.foodPrice.text = order.totalPrice ?: "0đ" // Hiển thị tổng giá của đơn hàng

            // Lấy ảnh của món đầu tiên làm đại diện
            val imageUrl = order.foodImages?.firstOrNull()
            if (imageUrl != null) {
                val uri = Uri.parse(imageUrl)
                Glide.with(context).load(uri).into(binding.foodImage)
            } else {
                binding.foodImage.setImageResource(R.drawable.menu2) // Ảnh mặc định
            }

            // Hiển thị ngày đặt hàng
            if (order.currentTime > 0) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                binding.tvOrderDate.text = "Đặt lúc: ${sdf.format(Date(order.currentTime))}"
            } else {
                binding.tvOrderDate.text = "Ngày không xác định"
            }
        }
    }
}
