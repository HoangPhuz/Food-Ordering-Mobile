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
    private val cartQuantity: MutableList<Int> // Sẽ là nguồn chính cho số lượng
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    // cartItemsReference là thuộc tính của instance, không nằm trong companion object
    private lateinit var cartItemsReference: DatabaseReference

    init {
        val userId = auth.currentUser?.uid ?: ""
        val database = FirebaseDatabase.getInstance().reference
        cartItemsReference = database.child("users").child(userId).child("CartItems")
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

    // Trả về một bản sao của danh sách số lượng hiện tại
    fun getUpdatedQuantities(): MutableList<Int> {
        return ArrayList(cartQuantity) // Sử dụng ArrayList để tạo bản sao mới
    }

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            // Sử dụng adapterPosition để đảm bảo lấy đúng vị trí, đặc biệt sau khi có thay đổi item
            val currentPosition = adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) {
                // Item không còn tồn tại hoặc đang trong quá trình thay đổi, không làm gì cả
                return
            }

            binding.cartFoodName.text = cartItems[currentPosition]
            binding.cartItemPrice.text = cartItemPrices[currentPosition]

            val uriString = cartImages[currentPosition]
            val uri = Uri.parse(uriString)
            Glide.with(binding.root.context).load(uri).listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Glide", "Lỗi tải ảnh cho ${cartItems[currentPosition]}: ${e?.message}")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    // Log.d("Glide", "Ảnh đã tải thành công cho ${cartItems[currentPosition]}")
                    return false
                }
            }).into(binding.cartImage)

            binding.cartItemQuantity.text = cartQuantity[currentPosition].toString()

            binding.minusButton.setOnClickListener {
                val pos = adapterPosition // Lấy vị trí hiện tại khi click
                if (pos != RecyclerView.NO_POSITION) {
                    if (cartQuantity[pos] > 1) {
                        cartQuantity[pos]--
                        // Cập nhật lại view cho item này để hiển thị số lượng mới
                        notifyItemChanged(pos)
                    }
                }
            }

            binding.plusButton.setOnClickListener {
                val pos = adapterPosition // Lấy vị trí hiện tại khi click
                if (pos != RecyclerView.NO_POSITION) {
                    if (cartQuantity[pos] < 10) { // Giới hạn số lượng tối đa là 10
                        cartQuantity[pos]++
                        // Cập nhật lại view cho item này
                        notifyItemChanged(pos)
                    }
                }
            }

            binding.deleteButton.setOnClickListener {
                val pos = adapterPosition // Lấy vị trí hiện tại khi click
                if (pos != RecyclerView.NO_POSITION) {
                    getUniqueKeyAtPosition(pos) { uniqueKey ->
                        Log.d("deleteOrderItem", "Callback cho vị trí $pos, uniqueKey: $uniqueKey")
                        if (uniqueKey != null) {
                            removeItem(pos, uniqueKey)
                        } else {
                            Log.e("deleteOrderItem", "Không lấy được uniqueKey cho vị trí $pos. Không thể xóa.")
                            Toast.makeText(binding.root.context, "Lỗi: Không tìm thấy sản phẩm để xóa", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            if (position < 0 || position >= cartItems.size) {
                Log.e("removeItem", "Vị trí không hợp lệ: $position. Không thể xóa.")
                return
            }
            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                Log.d("removeItem", "Xóa thành công item với key $uniqueKey trên Firebase.")
                // Xóa item khỏi tất cả các danh sách local
                cartItems.removeAt(position)
                cartItemPrices.removeAt(position)
                cartImages.removeAt(position)
                cartDescriptions.removeAt(position)
                cartIngredients.removeAt(position)
                cartQuantity.removeAt(position) // Quan trọng: Xóa cả số lượng tương ứng

                notifyItemRemoved(position)
                // Thông báo cho adapter về sự thay đổi phạm vi của các item còn lại
                // Điều này giúp cập nhật đúng các vị trí (positions) của các viewholder khác
                notifyItemRangeChanged(position, cartItems.size - position)

                Toast.makeText(binding.root.context, "Xoá thành công", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Log.e("removeItem", "Xóa thất bại item với key $uniqueKey trên Firebase: ${exception.message}")
                Toast.makeText(binding.root.context, "Xóa thất bại trên server", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
            Log.d("getUniqueKey", "Đang lấy key cho vị trí: $positionRetrieve")
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        // Chuyển children thành List để truy cập bằng index một cách an toàn
                        // Giả định thứ tự item trên Firebase khớp với thứ tự trong adapter
                        val childrenList = snapshot.children.toList()
                        if (positionRetrieve >= 0 && positionRetrieve < childrenList.size) {
                            uniqueKey = childrenList[positionRetrieve].key
                            Log.d("getUniqueKey", "Tìm thấy key: $uniqueKey cho vị trí $positionRetrieve")
                        } else {
                            Log.w("getUniqueKey", "Vị trí $positionRetrieve nằm ngoài giới hạn của danh sách children trên Firebase (size: ${childrenList.size})")
                        }
                    } else {
                        Log.w("getUniqueKey", "Không tìm thấy children tại cartItemsReference hoặc snapshot không tồn tại.")
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getUniqueKey", "Lỗi khi lấy dữ liệu từ Firebase: ${error.message}")
                    onComplete(null) // Trả về null nếu có lỗi
                }
            })
        }
    }
}
