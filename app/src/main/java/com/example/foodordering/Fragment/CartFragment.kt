package com.example.foodordering.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.CartAdapter
import com.example.foodordering.CongratsBottomSheet
import com.example.foodordering.Model.CartItems
import com.example.foodordering.PayOutActivity
import com.example.foodordering.R
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
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            startActivity(intent)
        }
        return binding.root
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
                val adapter = CartAdapter(requireContext(), foodNames, foodPrices, foodImagesUri, foodDescriptions, foodIngredients, foodQuantity)
                binding.cartRecycleView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)
                binding.cartRecycleView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show()
            }

        })
    }

    companion object {

    }
}