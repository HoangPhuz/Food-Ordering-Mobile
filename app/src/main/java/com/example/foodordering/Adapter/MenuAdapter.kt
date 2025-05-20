package com.example.foodordering.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodordering.DetailsActivity
import com.example.foodordering.Model.MenuItem
import com.example.foodordering.databinding.MenuItemBinding

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val requireContext: Context)
    : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItems.size

    inner class MenuViewHolder(private val binding: MenuItemBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }
            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = menuItems[position]
            // Tạo Intent để mở DetailsActivity
            val intent = Intent(requireContext, DetailsActivity::class.java)
            // Truyền dữ liệu cần thiết vào Intent
            intent.putExtra("MenuItemName", menuItem.foodName)
            intent.putExtra("MenuItemImage", menuItem.foodImage)
            intent.putExtra("MenuItemPrice", menuItem.foodPrice)
            intent.putExtra("MenuItemDescription", menuItem.foodDescription)
            intent.putExtra("MenuItemIngredients", menuItem.foodIngredient)

            // Bắt đầu Activity mới
            requireContext.startActivity(intent)

        }

        //Set data vào RecyclerView
        fun bind(position: Int) {
            val menuItems = menuItems[position]
            binding.apply {
                menuFoodName.text=menuItems.foodName
                menuPrice.text=menuItems.foodPrice


                val Uri = Uri.parse(menuItems.foodImage)
                Glide.with(requireContext)
                    .load(Uri)
                    .into(menuImage)
            }
        }

    }


}


