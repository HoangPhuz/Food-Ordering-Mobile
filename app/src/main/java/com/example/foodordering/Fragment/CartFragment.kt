package com.example.foodordering.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.CartAdapter
import com.example.foodordering.Model.CartItems
import com.example.foodordering.PayOutActivity
import com.example.foodordering.databinding.FragmentCartBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference // Thêm import
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImagesUri: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var foodQuantities: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String
    private lateinit var cartItemsRef: DatabaseReference
    private var cartValueEventListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance()

        if (userId.isNotEmpty()) {
            cartItemsRef = database.reference.child("users").child(userId).child("CartItems")
            setupRecyclerView() // Khởi tạo RecyclerView và Adapter trước
            retrieveCartItems()
        } else {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem giỏ hàng.", Toast.LENGTH_LONG).show()

        }


        binding.proceedButton.setOnClickListener {
            if (::cartAdapter.isInitialized && cartAdapter.itemCount > 0) {
                getOrderItemsDetail()
            } else {
                Toast.makeText(requireContext(), "Giỏ hàng của bạn đang trống.", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun setupRecyclerView() {
        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        foodQuantities = mutableListOf()

        cartAdapter = CartAdapter(
            requireContext(),
            foodNames,
            foodPrices,
            foodImagesUri,
            foodDescriptions,
            foodIngredients,
            foodQuantities
        )
        binding.cartRecycleView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecycleView.adapter = cartAdapter
    }


    private fun retrieveCartItems() {
        // Xóa listener cũ nếu có để tránh leak và gọi nhiều lần
        if (cartValueEventListener != null) {
            cartItemsRef.removeEventListener(cartValueEventListener!!)
        }

        cartValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Xóa dữ liệu cũ trong các list trước khi thêm mới
                foodNames.clear()
                foodPrices.clear()
                foodDescriptions.clear()
                foodImagesUri.clear()
                foodIngredients.clear()
                foodQuantities.clear()

                for (foodSnapshot in snapshot.children) {
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)
                    cartItem?.let { item ->
                        item.foodName?.let { foodNames.add(it) }
                        item.foodPrice?.let { foodPrices.add(it) }
                        item.foodDescription?.let { foodDescriptions.add(it) }
                        item.foodImage?.let { foodImagesUri.add(it) }
                        item.foodIngredient?.let { foodIngredients.add(it) }
                        item.foodQuantity?.let { foodQuantities.add(it) }
                            ?: foodQuantities.add(1) // Mặc định là 1 nếu null
                    }
                }
                // Thông báo cho adapter rằng toàn bộ dữ liệu đã thay đổi
                if (::cartAdapter.isInitialized) { // Kiểm tra adapter đã được khởi tạo chưa
                    cartAdapter.notifyDataSetChanged()
                } else {
                    Log.w("CartFragment", "Adapter chưa được khởi tạo khi onDataChange được gọi.")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu giỏ hàng: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("CartFragment", "Firebase data fetch cancelled: ${error.message}")
            }
        }
        cartItemsRef.addValueEventListener(cartValueEventListener!!) // Sử dụng addValueEventListener
    }

    private fun getOrderItemsDetail() {

        val currentFoodNames = ArrayList(this.foodNames)
        val currentFoodPrices = ArrayList(this.foodPrices)
        val currentFoodImages = ArrayList(this.foodImagesUri)
        val currentFoodDescriptions = ArrayList(this.foodDescriptions)
        val currentFoodIngredients = ArrayList(this.foodIngredients)
        val currentFoodQuantities = cartAdapter.getUpdatedQuantities() // Lấy số lượng đã cập nhật từ adapter


        if (currentFoodNames.size == currentFoodQuantities.size) {
            orderNow(
                currentFoodNames,
                currentFoodPrices,
                currentFoodImages,
                currentFoodDescriptions,
                currentFoodIngredients,
                currentFoodQuantities
            )
        } else {
            Toast.makeText(requireContext(), "Lỗi dữ liệu giỏ hàng, vui lòng thử lại.", Toast.LENGTH_SHORT).show()
            Log.e("CartFragment", "Kích thước danh sách không khớp khi lấy thông tin chi tiết về đơn hàng. Tên: ${currentFoodNames.size}, Số lượng: ${currentFoodQuantities.size}")

        }

    }


    private fun orderNow(
        foodNames: MutableList<String>,
        foodPrices: MutableList<String>,
        foodImages: MutableList<String>,
        foodDescriptions: MutableList<String>,
        foodIngredients: MutableList<String>,
        foodQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putStringArrayListExtra("foodItemName", ArrayList(foodNames))
            intent.putStringArrayListExtra("foodItemPrice", ArrayList(foodPrices))
            intent.putStringArrayListExtra("foodItemImage", ArrayList(foodImages))
            intent.putStringArrayListExtra("foodItemDescription", ArrayList(foodDescriptions))
            intent.putStringArrayListExtra("foodItemIngredient", ArrayList(foodIngredients))
            intent.putIntegerArrayListExtra("foodItemQuantities", ArrayList(foodQuantities))
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (cartValueEventListener != null && ::cartItemsRef.isInitialized) {
            cartItemsRef.removeEventListener(cartValueEventListener!!)
        }
    }


}
