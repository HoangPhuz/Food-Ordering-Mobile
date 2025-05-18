package com.example.foodordering

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.RecentBuyAdapter
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.databinding.ActivityRecentOrderItemsBinding


class RecentOrderItems : AppCompatActivity() {
    private lateinit var binding: ActivityRecentOrderItemsBinding
    private lateinit var allFoodNames: ArrayList<String>
    private lateinit var allFoodPrices: ArrayList<String>
    private lateinit var allFoodImages: ArrayList<String>
    private lateinit var allFoodQuantities: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        val recentOrderItems = intent.getSerializableExtra("recentBuyOrderItem") as ArrayList<OrderDetails>
        recentOrderItems?.let { orderDetails ->
            if(orderDetails.isNotEmpty()){
                val recentOrderItem = orderDetails[0]
                allFoodNames = recentOrderItem.foodNames as ArrayList<String>
                allFoodPrices = recentOrderItem.foodPrices as ArrayList<String>
                allFoodImages = recentOrderItem.foodImages as ArrayList<String>
                allFoodQuantities = recentOrderItem.foodQuantities as ArrayList<Int>
            }
        }
        setAdapter()

        binding.backButton.setOnClickListener{
            finish()
        }

    }

    private fun setAdapter() {
        val rv = binding.recyclerViewRecentBuy
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val adapter = RecentBuyAdapter(this, allFoodNames, allFoodPrices, allFoodImages, allFoodQuantities)
        rv.adapter = adapter

    }
}