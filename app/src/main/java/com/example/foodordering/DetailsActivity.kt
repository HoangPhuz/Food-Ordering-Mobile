package com.example.foodordering

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseError
import com.bumptech.glide.Glide
import com.example.foodordering.Model.CartItems
import com.example.foodordering.databinding.ActivityDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding

    private var foodName: String? = null
    private var foodImage: String? = null
    private var foodPrice: String? = null
    private var foodDescription: String? = null
    private var foodIngredients: String? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        foodName = intent.getStringExtra("MenuItemName")
        foodImage = intent.getStringExtra("MenuItemImage")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodIngredients = intent.getStringExtra("MenuItemIngredients")

        with(binding) {
            detailFoodName.text = foodName
            detailFoodDescription.text = foodDescription
            detailFoodIngredients.text = foodIngredients
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(detailFoodImage)

        }

        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addItemButton.setOnClickListener {
            addItemsToCart()
        }
    }

    private fun addItemsToCart() {
        val databaseRef = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
            return
        }

        // Giả sử foodName, foodImage, foodPrice, foodDescription, foodIngredients là các thuộc tính
        // của Activity/Fragment này và đã có giá trị.
        // Đảm bảo chúng không null trước khi sử dụng.
        val currentFoodName = foodName?.toString() // Lấy giá trị từ thuộc tính của class
        if (currentFoodName.isNullOrEmpty()) {
            Toast.makeText(this, "Tên sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItemsRef = databaseRef.child("users").child(userId).child("CartItems")

        // 1. Truy vấn để tìm item hiện có với cùng foodName
        val query = cartItemsRef.orderByChild("foodName").equalTo(currentFoodName)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Item đã tồn tại, tăng số lượng
                    var itemFound = false
                    for (itemSnapshot in snapshot.children) {
                        val existingCartItem = itemSnapshot.getValue(CartItems::class.java)
                        val itemKey = itemSnapshot.key // Lấy key của item hiện tại

                        if (existingCartItem != null && itemKey != null) {
                            val newQuantity = (existingCartItem.foodQuantity ?: 0) + 1
                            // Cập nhật lại số lượng cho item đó
                            cartItemsRef.child(itemKey).child("foodQuantity").setValue(newQuantity)
                                .addOnSuccessListener {
                                    Toast.makeText(this@DetailsActivity, "Đã cập nhật số lượng trong giỏ hàng", Toast.LENGTH_SHORT).show()
                                    finish() // Hoặc cập nhật UI nếu cần
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this@DetailsActivity, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show()
                                }
                            itemFound = true
                            break // Giả sử foodName là duy nhất, tìm thấy là dừng
                        }
                    }
                    if (!itemFound) { // Trường hợp hiếm: snapshot.exists() nhưng không lặp được item hợp lệ
                        addNewItemToCart(cartItemsRef, currentFoodName)
                    }
                } else {
                    // Item chưa tồn tại, thêm item mới
                    addNewItemToCart(cartItemsRef, currentFoodName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddToCart", "Lỗi truy vấn Firebase: ${error.message}")
                Toast.makeText(this@DetailsActivity, "Lỗi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Hàm phụ trợ để thêm item mới, tránh lặp code
    private fun addNewItemToCart(cartItemsRef: DatabaseReference, currentFoodName: String) {
        val cartItem = CartItems(
            foodName = currentFoodName,
            foodImage = foodImage?.toString(), // Lấy từ thuộc tính của class
            foodPrice = foodPrice?.toString(), // Lấy từ thuộc tính của class
            foodDescription = foodDescription?.toString(), // Lấy từ thuộc tính của class
            foodIngredient = foodIngredients?.toString(), // Lấy từ thuộc tính của class
            foodQuantity = 1 // Số lượng ban đầu là 1
        )

        cartItemsRef.push().setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this@DetailsActivity, "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show()
                finish() // Hoặc cập nhật UI nếu cần
            }
            .addOnFailureListener {
                Toast.makeText(this@DetailsActivity, "Thêm vào giỏ hàng thất bại", Toast.LENGTH_SHORT).show()
            }
    }

}