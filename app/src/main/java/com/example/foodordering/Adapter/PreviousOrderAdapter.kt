package com.example.foodordering.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R
import com.example.foodordering.databinding.BuyAgainItemBinding // Sử dụng layout item cũ

class PreviousOrderAdapter(
    private val context: Context,
    private var previousOrders: MutableList<OrderDetails>,
    private val listener: OnItemInteractionListener
) : RecyclerView.Adapter<PreviousOrderAdapter.BuyAgainViewHolder>() {

    interface OnItemInteractionListener {
        fun onBuyAgainClicked(orderDetails: OrderDetails, position: Int)
        fun onItemClicked(orderDetails: OrderDetails) // Để xem chi tiết đơn hàng
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        val order = previousOrders[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int = previousOrders.size

    fun removeItem(position: Int) {
        if (position >= 0 && position < previousOrders.size) {
            previousOrders.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, previousOrders.size)
        }
    }

    fun addItem(order: OrderDetails) {
        // Thêm vào đầu danh sách để đơn mới nhất (vừa được xác nhận) lên trên
        previousOrders.add(0, order)
        notifyItemInserted(0)
        notifyItemRangeChanged(0, previousOrders.size) // Cập nhật vị trí cho các item khác
    }


    fun updateList(newList: List<OrderDetails>) {
        val itemsToActuallyAdd = ArrayList(newList)
        this.previousOrders.clear()
        this.previousOrders.addAll(itemsToActuallyAdd)
        notifyDataSetChanged()
    }

    inner class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buyAgainFoodButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onBuyAgainClicked(previousOrders[position], position)
                }
            }
            binding.root.setOnClickListener{
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClicked(previousOrders[position])
                }
            }
        }

        fun bind(order: OrderDetails) {
            // Hiển thị tên các món ăn, có thể chỉ là món đầu tiên hoặc nối chuỗi
            binding.buyAgainFoodName.text = order.foodNames?.firstOrNull() ?: "Không có tên"
            binding.buyAgainFoodPrice.text = order.foodPrices?.firstOrNull() ?: "0đ" // Giá món đầu tiên

            // Lấy ảnh của món đầu tiên làm đại diện
            val imageUrl = order.foodImages?.firstOrNull()
            if (imageUrl != null) {
                val uri = Uri.parse(imageUrl)
                Glide.with(context).load(uri).into(binding.buyAgainFoodImage)
            } else {
                binding.buyAgainFoodImage.setImageResource(R.drawable.menu2) // Ảnh mặc định
            }
        }
    }
}
