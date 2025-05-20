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
    private lateinit var foodQuantity: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false);

        auth = FirebaseAuth.getInstance()
        retrieveCartItems()


        binding.proceedButton.setOnClickListener {
            //
            getOrderItemsDetail()

        }
        return binding.root
    }

    private fun getOrderItemsDetail() {
        val orderIdRef = database.getReference().child("users").child(userId).child("CartItems")
        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()
        val foodQuantities = cartAdapter.getUpdatedQuantities()

        orderIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(foodSnapshot in snapshot.children){
                    //Lấy mặt hàng trong giỏ hàng
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)
                    Log.d("OrderingItem", "orderItems: $orderItems")
                    //Thêm thông tin mặt hàng vào danh sách
                    orderItems?.foodName?.let { foodName.add(it) }
                    orderItems?.foodPrice?.let { foodPrice.add(it) }
                    orderItems?.foodDescription?.let { foodDescription.add(it) }
                    orderItems?.foodImage?.let { foodImage.add(it) }
                    orderItems?.foodIngredient?.let { foodIngredient.add(it) }
                    orderItems?.foodQuantity?.let { foodQuantities.add(it)  }
                }
                orderNow(foodName, foodPrice, foodImage, foodDescription, foodIngredient, foodQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun orderNow(foodName: MutableList<String>, foodPrice: MutableList<String>, foodImage: MutableList<String>, foodDescription: MutableList<String>, foodIngredient: MutableList<String>, foodQuantities: MutableList<Int>) {
        if(isAdded && context!=null){
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putStringArrayListExtra("foodItemName", ArrayList(foodName))
            intent.putStringArrayListExtra("foodItemPrice", ArrayList(foodPrice))
            intent.putStringArrayListExtra("foodItemImage", ArrayList(foodImage))
            intent.putStringArrayListExtra("foodItemDescription", ArrayList(foodDescription))
            intent.putStringArrayListExtra("foodItemIngredient", ArrayList(foodIngredient))
            intent.putIntegerArrayListExtra("foodItemQuantities", ArrayList(foodQuantities))
            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        val cartItemsRef = database.getReference().child("users").child(userId).child("CartItems")

        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        foodQuantity = mutableListOf()

        //fetch data từ firebase
        cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(foodSnapshot in snapshot.children){
                    val cartItems = foodSnapshot.getValue(CartItems::class.java)
                    cartItems?.foodName?.let { foodNames.add(it) }
                    cartItems?.foodPrice?.let { foodPrices.add(it) }
                    cartItems?.foodDescription?.let { foodDescriptions.add(it) }
                    cartItems?.foodImage?.let { foodImagesUri.add(it) }
                    cartItems?.foodIngredient?.let { foodIngredients.add(it) }
                    cartItems?.foodQuantity?.let { foodQuantity.add(it) }
                }
                setAdapter()
            }

            private fun setAdapter() {
                cartAdapter = CartAdapter(requireContext(), foodNames, foodPrices, foodImagesUri, foodDescriptions, foodIngredients, foodQuantity)
                binding.cartRecycleView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
                binding.cartRecycleView.adapter = cartAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
    }
}