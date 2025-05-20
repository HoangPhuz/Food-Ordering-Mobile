package com.example.foodordering

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.MenuAdapter
import com.example.foodordering.Model.MenuItem
import com.example.foodordering.databinding.FragmentMenuBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)
        binding.buttonBack.setOnClickListener {
            dismiss()
        }
        retriveMenuItems()
        return binding.root
    }

    private fun retriveMenuItems() {
        database = FirebaseDatabase.getInstance()
        val menuRef = database.getReference("menu")
        menuItems = mutableListOf()
        menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (menuSnapshot in snapshot.children) {
                    val menuItem = menuSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                Log.d("Items", "onDataChange: data received")
                //set adapter sau khi nhận data
                setAdapters()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun setAdapters() {
        if(menuItems.isNotEmpty()){
            val adapter = MenuAdapter(menuItems, requireContext())
            binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.menuRecyclerView.adapter = adapter
            Log.d("Items", "setAdapters: data set")
        }
        else{
            Log.d("Items", "setAdapters: data not set")
        }

    }
    companion object
}