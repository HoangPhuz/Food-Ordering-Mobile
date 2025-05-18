package com.example.foodordering

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.RecentBuyAdapter
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.databinding.ActivityRecentOrderItemsBinding
import com.google.firestore.admin.v1.Index.IndexField.Order


class RecentOrderItems : AppCompatActivity() {
    private val binding: ActivityRecentOrderItemsBinding by lazy {
        ActivityRecentOrderItemsBinding.inflate(layoutInflater)
    }
    private lateinit var allFoodNames: ArrayList<String>
    private lateinit var allFoodImages: ArrayList<String>
    private lateinit var allFoodPrices: ArrayList<String>
    private lateinit var allFoodQuantities: ArrayList<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val recentOrderItems =
            intent.getSerializableExtra("recentBuyOrderItem") as ArrayList<OrderDetails>
        recentOrderItems?.let { orderDetails ->
            if (orderDetails.isNotEmpty()) {
                val recentOrderItem = orderDetails[0]
                allFoodNames = recentOrderItem.foodNames as ArrayList<String>
                allFoodImages = recentOrderItem.foodImages as ArrayList<String>
                allFoodPrices = recentOrderItem.foodPrices as ArrayList<String>
                allFoodQuantities = recentOrderItem.foodQuantities as ArrayList<Int>
            }
        }
        setAdapter()

    }

    private fun setAdapter() {
        val recycleView = binding.recyclerViewRecentBuy
        recycleView.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this, allFoodNames, allFoodImages, allFoodPrices, allFoodQuantities)
        recycleView.adapter = adapter
    }
}