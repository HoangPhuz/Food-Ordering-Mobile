package com.example.foodordering

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.example.foodordering.databinding.ActivityRecentOrderItemsBinding


class RecentOrderItems : AppCompatActivity() {
    private lateinit var binding: ActivityRecentOrderItemsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentOrderItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener{
            finish()
        }

    }
}