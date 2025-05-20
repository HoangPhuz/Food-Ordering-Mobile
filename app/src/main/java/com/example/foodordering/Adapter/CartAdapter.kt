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
    private val cartFoodNames: MutableList<String>, // Đổi tên để rõ ràng hơn
    private val cartItemPrices: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartDescriptions: MutableList<String>, // Giữ lại nếu bạn dùng
    private val cartIngredients: MutableList<String>,  // Giữ lại nếu bạn dùng
    private val cartQuantities: MutableList<Int>    // Đổi tên để rõ ràng hơn
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val cartItemsReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        .child("users").child(userId).child("CartItems")

    // Không cần init block nếu userId và cartItemsReference đã được khởi tạo như trên.
    // init {
    //     val userId = auth.currentUser?.uid ?: ""
    //     if (userId.isNotEmpty()) { // Kiểm tra userId không rỗng
    //         val database = FirebaseDatabase.getInstance().reference
    //         cartItemsReference = database.child("users").child(userId).child("CartItems")
    //     } else {
    //         // Xử lý trường hợp userId rỗng, ví dụ: log lỗi hoặc không cho phép thao tác
    //         Log.e("CartAdapter", "User ID is empty, cart operations might fail.")
    //         // Hoặc throw một exception nếu đây là trạng thái không hợp lệ
    //     }
    // }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return cartFoodNames.size
    }

    fun getUpdatedQuantities(): MutableList<Int> {
        return ArrayList(cartQuantities)
    }

    inner class CartViewHolder(val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val currentPosition = adapterPosition
            if (currentPosition == RecyclerView.NO_POSITION) return

            binding.cartFoodName.text = cartFoodNames[currentPosition]
            binding.cartItemPrice.text = cartItemPrices[currentPosition]
            binding.cartItemQuantity.text = cartQuantities[currentPosition].toString()

            val uriString = cartImages[currentPosition]
            if (uriString.isNotEmpty()) { // Kiểm tra uriString không rỗng
                val uri = Uri.parse(uriString)
                Glide.with(binding.root.context).load(uri)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                        ): Boolean {
                            Log.e("Glide", "Lỗi tải ảnh cho ${cartFoodNames[currentPosition]}: ${e?.message}")
                            // Có thể đặt ảnh placeholder ở đây nếu muốn
                            // binding.cartImage.setImageResource(R.drawable.placeholder_image)
                            return false
                        }
                        override fun onResourceReady(
                            resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }
                    }).into(binding.cartImage)
            } else {
                // Xử lý trường hợp uriString rỗng, ví dụ đặt ảnh placeholder
                Log.w("Glide", "Image URI is empty for ${cartFoodNames[currentPosition]}")
                // binding.cartImage.setImageResource(R.drawable.placeholder_image)
            }


            binding.minusButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (cartQuantities[pos] > 1) {
                        val newQuantity = cartQuantities[pos] - 1
                        updateQuantityOnFirebase(pos, newQuantity)
                    } else {
                        // Nếu muốn xóa item khi số lượng là 1 và nhấn trừ, thêm logic ở đây
                        // Hoặc đơn giản là không làm gì nếu không muốn số lượng < 1
                        Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.plusButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    if (cartQuantities[pos] < 10) { // Giới hạn số lượng tối đa
                        val newQuantity = cartQuantities[pos] + 1
                        updateQuantityOnFirebase(pos, newQuantity)
                    } else {
                        Toast.makeText(context, "Số lượng tối đa là 10", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            binding.deleteButton.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    getUniqueKeyAtPosition(pos) { uniqueKey ->
                        if (uniqueKey != null) {
                            removeItem(pos, uniqueKey)
                        } else {
                            Log.e("CartAdapter", "Không lấy được uniqueKey để xóa tại vị trí $pos.")
                            Toast.makeText(binding.root.context, "Lỗi: Không tìm thấy sản phẩm để xóa", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        private fun updateQuantityOnFirebase(position: Int, newQuantity: Int) {
            if (userId.isEmpty()) {
                Toast.makeText(context, "Lỗi: Người dùng không xác định.", Toast.LENGTH_SHORT).show()
                return
            }
            getUniqueKeyAtPosition(position) { uniqueKey ->
                if (uniqueKey != null) {
                    cartItemsReference.child(uniqueKey).child("foodQuantity").setValue(newQuantity)
                        .addOnSuccessListener {
                            Log.d("CartAdapter", "Cập nhật số lượng thành công trên Firebase cho key $uniqueKey.")
                            // Cập nhật local list và UI
                            cartQuantities[position] = newQuantity
                            notifyItemChanged(position)
                            // Không cần Toast ở đây nếu không muốn làm phiền người dùng mỗi lần +/-
                        }
                        .addOnFailureListener { e ->
                            Log.e("CartAdapter", "Lỗi cập nhật số lượng trên Firebase cho key $uniqueKey: ${e.message}")
                            Toast.makeText(context, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show()
                            // Cân nhắc rollback thay đổi local nếu cập nhật Firebase thất bại
                        }
                } else {
                    Log.e("CartAdapter", "Không lấy được uniqueKey để cập nhật số lượng tại vị trí $position.")
                    Toast.makeText(context, "Lỗi: Không tìm thấy sản phẩm để cập nhật", Toast.LENGTH_SHORT).show()
                }
            }
        }


        private fun removeItem(position: Int, uniqueKey: String) {
            if (userId.isEmpty()) {
                Toast.makeText(context, "Lỗi: Người dùng không xác định.", Toast.LENGTH_SHORT).show()
                return
            }
            if (position < 0 || position >= cartFoodNames.size) { // Kiểm tra với cartFoodNames vì các list khác có cùng size
                Log.e("CartAdapter", "Vị trí không hợp lệ: $position. Không thể xóa.")
                return
            }
            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                Log.d("CartAdapter", "Xóa thành công item với key $uniqueKey trên Firebase.")
                // Xóa item khỏi tất cả các danh sách local
                // Cần kiểm tra lại index sau khi xóa để tránh lỗi nếu có nhiều thao tác nhanh
                if (position < cartFoodNames.size) { // Đảm bảo vị trí vẫn hợp lệ
                    cartFoodNames.removeAt(position)
                    cartItemPrices.removeAt(position)
                    cartImages.removeAt(position)
                    cartDescriptions.removeAt(position)
                    cartIngredients.removeAt(position)
                    cartQuantities.removeAt(position)

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartFoodNames.size - position) // Hoặc cartFoodNames.size
                    Toast.makeText(binding.root.context, "Xoá thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("CartAdapter", "Vị trí $position không còn hợp lệ sau khi có thể có thay đổi khác.")
                }

            }.addOnFailureListener { exception ->
                Log.e("CartAdapter", "Xóa thất bại item với key $uniqueKey trên Firebase: ${exception.message}")
                Toast.makeText(binding.root.context, "Xóa thất bại trên server", Toast.LENGTH_SHORT).show()
            }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String?) -> Unit) {
            if (userId.isEmpty()) {
                Log.e("CartAdapter", "User ID rỗng trong getUniqueKeyAtPosition.")
                onComplete(null)
                return
            }
            // Lấy key dựa trên foodName, giả định foodName là duy nhất trong giỏ hàng của user
            // Nếu foodName không duy nhất, bạn cần một cách khác để xác định item,
            // ví dụ: lưu key khi tải dữ liệu ban đầu.
            // Cách hiện tại (dùng index) có thể không ổn định nếu thứ tự trên Firebase thay đổi
            // hoặc không khớp với thứ tự hiển thị.

            // Cách tốt hơn là khi retrieveCartItems, bạn lưu cả key của mỗi item.
            // Tuy nhiên, với cách hiện tại, chúng ta sẽ cố gắng lấy theo thứ tự.
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        val childrenList = snapshot.children.toList()
                        if (positionRetrieve >= 0 && positionRetrieve < childrenList.size) {
                            uniqueKey = childrenList[positionRetrieve].key
                            Log.d("CartAdapter", "Tìm thấy key: $uniqueKey cho vị trí $positionRetrieve")
                        } else {
                            Log.w("CartAdapter", "Vị trí $positionRetrieve nằm ngoài giới hạn của danh sách children trên Firebase (size: ${childrenList.size})")
                        }
                    } else {
                        Log.w("CartAdapter", "Không tìm thấy children tại cartItemsReference hoặc snapshot không tồn tại.")
                    }
                    onComplete(uniqueKey)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartAdapter", "Lỗi khi lấy dữ liệu từ Firebase: ${error.message}")
                    onComplete(null)
                }
            })
        }
    }
}
