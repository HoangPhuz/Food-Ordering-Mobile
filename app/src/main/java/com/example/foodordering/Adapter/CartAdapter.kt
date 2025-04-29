package com.example.foodordering.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodordering.databinding.CartItemBinding

class CartAdapter(private val cartItems: MutableList<String>, private val cartItemPrices: MutableList<String>, private val cartImage: MutableList<Int>): RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val itemQuantities = MutableList(cartItems.size) { 1 }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            val quantity = itemQuantities[position]
            binding.cartFoodName.text = cartItems[position]
            binding.cartItemPrice.text = cartItemPrices[position]
            binding.cartImage.setImageResource(cartImage[position])
            binding.cartItemQuantity.text = quantity.toString();

            binding.minusButton.setOnClickListener {
                if (itemQuantities[position] > 1) {
                    itemQuantities[position]--
                    binding.cartItemQuantity.text = itemQuantities[position].toString()
                }
            }

            binding.plusButton.setOnClickListener {
                itemQuantities[position]++
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }

            binding.deleteButton.setOnClickListener {
                val itemPosition = adapterPosition // Lấy vị trí của mục trong adapter
                if(itemPosition != RecyclerView.NO_POSITION){
                    cartItems.removeAt(itemPosition)
                    cartItemPrices.removeAt(itemPosition)
                    cartImage.removeAt(itemPosition)
                    itemQuantities.removeAt(itemPosition)
                    notifyItemRemoved(itemPosition)
                    notifyItemRangeChanged(itemPosition, cartItems.size)

                }

            }


        }
    }

}