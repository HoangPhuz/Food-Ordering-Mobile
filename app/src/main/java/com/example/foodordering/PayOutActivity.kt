package com.example.foodordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodordering.Fragment.CartFragment
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.databinding.ActivityPayOutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PayOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var name:String
    private lateinit var address:String
    private lateinit var phone:String
    private lateinit var totalAmount:String
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private lateinit var database: FirebaseDatabase
    private lateinit var userId:String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance()

        setUserData()
        val intent = intent

        foodItemName = intent.getStringArrayListExtra("foodItemName") as ArrayList<String>
        foodItemPrice = intent.getStringArrayListExtra("foodItemPrice") as ArrayList<String>
        foodItemImage = intent.getStringArrayListExtra("foodItemImage") as ArrayList<String>
        foodItemDescription = intent.getStringArrayListExtra("foodItemDescription") as ArrayList<String>
        foodItemIngredient = intent.getStringArrayListExtra("foodItemIngredient") as ArrayList<String>
        foodItemQuantities = intent.getIntegerArrayListExtra("foodItemQuantities") as ArrayList<Int>
        Log.d("OrderingItem", "foodItemQuantities: $foodItemQuantities")
        totalAmount = calculateTotalAmount().toString() + " vnđ"
        //binding.totalAmount.isEnabled = false
        binding.totalAmount.setText(totalAmount)
        binding.buttonBack.setOnClickListener {
            finish()
        }
        binding.PlaceMyOrder.setOnClickListener {
            name = binding.name.text.toString().trim()
            address = binding.address.text.toString().trim()
            phone = binding.phone.text.toString().trim()
            if(name.isEmpty() || address.isEmpty() || phone.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()

            }
            else{
                placeOrder()
            }

        }
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodItemPrice.size) {
            var price = foodItemPrice[i]
            val lastVND = price.substring(price.length - 3)
            val priceIntValue = if (lastVND == "vnđ") {
                price.substring(0, price.length - 3).toInt()
            }
            else {
                price.toInt()
            }
            totalAmount += priceIntValue*foodItemQuantities[i]
        }
        Log.d("OrderingItem", "totalAmount: $totalAmount")
        return totalAmount
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPushKey = database.reference.child("OrderDetails").push().key
        val orderDetails = OrderDetails(userId, name, foodItemName, foodItemQuantities, foodItemPrice, foodItemImage, address, totalAmount, phone, false, false, itemPushKey, time)
        val orderRef = database.reference.child("OrderDetails").child(itemPushKey!!)
        orderRef.setValue(orderDetails).addOnSuccessListener {
            val bottomSheetDialog = CongratsBottomSheet()
            bottomSheetDialog.show(supportFragmentManager, "Test")
            removeItemsFromCart()
            addOrderToHistory(orderDetails)
        }.addOnFailureListener{
            Toast.makeText(this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show()
        }


    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        database.reference.child("users").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails).addOnSuccessListener {

            }
    }

    private fun removeItemsFromCart() {
        val cartItemsRef = database.reference.child("users").child(userId).child("CartItems")
        cartItemsRef.removeValue()
    }

    private fun setUserData() {
        val user = auth.currentUser
        if(user!=null){
            val userId = user.uid
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        name = snapshot.child("username").getValue(String::class.java)?:""
                        address = snapshot.child("address").getValue(String::class.java)?:""
                        phone = snapshot.child("phone").getValue(String::class.java)?:""
                    }
                    binding.name.setText(name)
                    binding.address.setText(address)
                    binding.phone.setText(phone)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
        val name = intent.getStringExtra("name")
    }


}