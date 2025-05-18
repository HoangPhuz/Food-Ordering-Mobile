package com.example.foodordering.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.foodordering.Adapter.BuyAgainAdapter
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R
import com.example.foodordering.RecentOrderItems
import com.example.foodordering.databinding.BuyAgainItemBinding
import com.example.foodordering.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId:String
    private var listOfOrderItem:MutableList<OrderDetails> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        //Lấy và hiển thị lịch sử mua hàng
        retrieveBuyHistory()

        //Click nút mua gần đây
        binding.recentBuyItem.setOnClickListener {
            seeItemsRecentBuy()
        }
        binding.receivedButton.setOnClickListener {
            updateOrderStatus()
        }


        return binding.root
    }

    private fun updateOrderStatus() {
        val itemPushKey = listOfOrderItem[0].itemPushKey
        val completeOrderRef = database.reference.child("CompletedOrder").child(itemPushKey!!)
        completeOrderRef.child("paymentReceived").setValue(true)


    }

    private fun seeItemsRecentBuy() {
        val intent = Intent(requireContext(), RecentOrderItems::class.java)
        intent.putExtra("recentBuyOrderItem", listOfOrderItem)
        startActivity(intent)
//
//        listOfOrderItem.firstOrNull()?.let { recentBuy ->
//            val intent = Intent(requireContext(), RecentOrderItems::class.java)
//            intent.putExtra("recentBuyOrderItem", listOfOrderItem)
//            startActivity(intent)
//        }
    }

    private fun retrieveBuyHistory() {
        binding.recentBuyItem.visibility = View.INVISIBLE
        userId = auth.currentUser?.uid ?: ""

        val buyHistoryRef = database.reference.child("users").child(userId).child("BuyHistory")
        val shortingQuerry = buyHistoryRef.orderByChild("currentTime")

        shortingQuerry.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(buySnapshot in snapshot.children){
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if(listOfOrderItem.isNotEmpty()){
                    setDataInRecentBuyItem()
                    setPreviousBuyItemsRecyclerView()
                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setDataInRecentBuyItem() {
        binding.recentBuyItem.visibility = View.VISIBLE
        val recentOrderItem = listOfOrderItem.firstOrNull()
        recentOrderItem?.let{
            with(binding) {
                buyAgainFoodName.text = it.foodNames?.firstOrNull() ?: ""
                buyAgainFoodPrice.text = it.foodPrices?.firstOrNull() ?: ""
                val image = it.foodImages?.firstOrNull()
                val uri = Uri.parse(image)
                Glide.with(requireContext()).load(uri).into(buyAgainFoodImage)

                listOfOrderItem.reverse()
                if(listOfOrderItem.isNotEmpty()){

                }
            }
        }
    }

    private fun setPreviousBuyItemsRecyclerView() {
        val buyAgainFoodName = mutableListOf<String>()
        val buyAgainFoodPrice = mutableListOf<String>()
        val buyAgainFoodImage = mutableListOf<String>()
        for(i in 1 until listOfOrderItem.size){
            listOfOrderItem[i].foodNames?.firstOrNull()?.let { buyAgainFoodName.add(it) }
            listOfOrderItem[i].foodPrices?.firstOrNull()?.let { buyAgainFoodPrice.add(it) }
            listOfOrderItem[i].foodImages?.firstOrNull()?.let { buyAgainFoodImage.add(it) }
        }

        val rv = binding.buyAgainRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        buyAgainAdapter = BuyAgainAdapter(requireContext(), buyAgainFoodName, buyAgainFoodPrice, buyAgainFoodImage)
        rv.adapter = buyAgainAdapter

    }



}