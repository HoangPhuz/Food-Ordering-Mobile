package com.example.foodordering.Adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.foodordering.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartDescriptions: MutableList<String>,
    private val cartIngredients: MutableList<String>,
    private val cartQuantity: MutableList<Int>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    init{
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""
        val cartItemNumber = cartItems.size

        itemQuantities = IntArray(cartItemNumber) { 1 }
        cartItemsReference = database.child("user").child(userId).child("CartItems")
        database.child("user").child(userId).child("CartItems").removeValue()
    }

    companion object{
        private var itemQuantities = intArrayOf()
        private lateinit var cartItemsReference : DatabaseReference

    }
        


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


    fun getUpdatedQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        itemQuantity.addAll(cartQuantity)
        return itemQuantity
    }

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(position: Int){
            val quantity = itemQuantities[position]
            binding.cartFoodName.text = cartItems[position]
            binding.cartItemPrice.text = cartItemPrices[position]

            val uriString = cartImages[position]
            var uri = Uri.parse(uriString)
            Glide.with(binding.root.context).load(uri).listener(object : RequestListener<Drawable>{
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Glide", "Lỗi tải ảnh: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Glide", "Ảnh đã tải thành công")
                    return false
                }
            }).into(binding.cartImage)

            binding.cartItemQuantity.text = quantity.toString();

            binding.minusButton.setOnClickListener {
                if (itemQuantities[position] > 1) {
                    itemQuantities[position]--
                    cartQuantity[position] = itemQuantities[position]
                    binding.cartItemQuantity.text = itemQuantities[position].toString()
                }
            }

            binding.plusButton.setOnClickListener {
                if (itemQuantities[position] < 10) {
                    itemQuantities[position]++
                    Log.d("OrderingItem", "cartQuantity size: " + cartQuantity.size)
                    cartQuantity[position] = itemQuantities[position]
                    binding.cartItemQuantity.text = itemQuantities[position].toString()
                }
            }

            binding.deleteButton.setOnClickListener {
                val positionRetrieve = position
                getUniqueKeyAtPosition(positionRetrieve){uniqueKey ->
                    if(uniqueKey != null){
                        removeItem(position, uniqueKey)
                    }
                }

            }


        }

        private fun removeItem(position: Int, uniqueKey: String) {
                if(uniqueKey != null){
                    cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                        cartItems.removeAt(position)
                        cartItemPrices.removeAt(position)
                        cartImages.removeAt(position)
                        cartDescriptions.removeAt(position)
                        cartIngredients.removeAt(position)
                        cartQuantity.removeAt(position)
                        Toast.makeText(binding.root.context, "Xoá thành công", Toast.LENGTH_SHORT).show()

                        //update itemQuantities
                        itemQuantities = itemQuantities.filterIndexed { index, i -> index != position }.toIntArray()
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, cartItems.size)
                    }.addOnFailureListener{
                        Toast.makeText(binding.root.context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                    }


                }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete:(String?) -> Unit){
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                        var uniqueKey:String?= null
                        snapshot.children.forEachIndexed{index, dataSnapshot ->
                            if(index == positionRetrieve){
                                uniqueKey = dataSnapshot.key
                                return@forEachIndexed
                            }
                        }
                    onComplete(uniqueKey)

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

}