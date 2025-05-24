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
import com.example.foodordering.Adapter.PendingOrderAdapter
import com.example.foodordering.Adapter.PreviousOrderAdapter
import com.example.foodordering.Model.CartItems
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R
import com.example.foodordering.RecentOrderItems
import com.example.foodordering.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment(), PendingOrderAdapter.OnItemInteractionListener, PreviousOrderAdapter.OnItemInteractionListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    private lateinit var pendingOrderAdapter: PendingOrderAdapter
    private lateinit var previousOrderAdapter: PreviousOrderAdapter


    private lateinit var buyHistoryRef: DatabaseReference
    private var buyHistoryListener: ValueEventListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        setupRecyclerViews()
        if (userId.isNotEmpty()) {
            buyHistoryRef = database.reference.child("users").child(userId).child("BuyHistory")
            retrieveOrderHistory()
        } else {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử.", Toast.LENGTH_LONG).show()
            updateEmptyStateViews(emptyList(), emptyList())
        }

        return binding.root
    }

    private fun setupRecyclerViews() {
        pendingOrderAdapter = PendingOrderAdapter(requireContext(), this)
        binding.pendingOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingOrdersRecyclerView.adapter = pendingOrderAdapter


        previousOrderAdapter = PreviousOrderAdapter(requireContext(), this)
        binding.previousOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingOrdersRecyclerView.adapter = previousOrderAdapter

    }

    private fun retrieveOrderHistory() {
        if (buyHistoryListener != null && ::buyHistoryRef.isInitialized) {
            buyHistoryRef.removeEventListener(buyHistoryListener!!)
        }

        buyHistoryListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allOrdersFromHistory = mutableListOf<OrderDetails>()
                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        allOrdersFromHistory.add(it)
                    }
                }
                allOrdersFromHistory.sortByDescending { it.currentTime }

                val currentPendingOrders = mutableListOf<OrderDetails>()
                val currentPreviousOrders = mutableListOf<OrderDetails>()

                for (order in allOrdersFromHistory) {
                    if (order.orderAccepted && !order.paymentReceived) {
                        currentPendingOrders.add(order)
                    } else if (order.orderAccepted && order.paymentReceived) {
                        currentPreviousOrders.add(order)
                    }
                }

                pendingOrderAdapter.submitList(currentPendingOrders)
                previousOrderAdapter.submitList(currentPreviousOrders)
                updateEmptyStateViews(currentPendingOrders, currentPreviousOrders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Failed to retrieve order history: ${error.message}")
                Toast.makeText(context, "Lỗi tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
                updateEmptyStateViews(emptyList(), emptyList())
            }
        }
        if (::buyHistoryRef.isInitialized) {
            buyHistoryRef.addValueEventListener(buyHistoryListener!!)
        }
    }

    private fun updateEmptyStateViews(pendingList: List<OrderDetails>, previousList: List<OrderDetails>) {
        binding.tvNoPendingOrders.visibility = if (pendingList.isEmpty()) View.VISIBLE else View.GONE
        binding.pendingOrdersRecyclerView.visibility = if (pendingList.isEmpty()) View.GONE else View.VISIBLE

        binding.tvNoPreviousOrders.visibility = if (previousList.isEmpty()) View.VISIBLE else View.GONE
        binding.previousOrdersRecyclerView.visibility = if (previousList.isEmpty()) View.GONE else View.VISIBLE
    }


    override fun onReceivedButtonClicked(orderDetails: OrderDetails, position: Int) {
        if (userId.isEmpty() || orderDetails.itemPushKey.isNullOrEmpty()) {
            Toast.makeText(context, "Lỗi thông tin đơn hàng hoặc người dùng.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!orderDetails.orderDispatched) {
            Toast.makeText(context, "Đơn hàng đang được chuẩn bị, chưa thể xác nhận đã nhận.", Toast.LENGTH_LONG).show()
            return
        }

        val userOrderRef = buyHistoryRef.child(orderDetails.itemPushKey!!)
        val completedOrderRefPath = "CompletedOrder/${orderDetails.itemPushKey!!}/paymentReceived"
        val updates = hashMapOf<String, Any>("paymentReceived" to true)

        userOrderRef.updateChildren(updates)
            .addOnSuccessListener {
                database.reference.child(completedOrderRefPath).setValue(true)
                    .addOnFailureListener { e ->
                        Log.e("HistoryFragment", "Failed to mark order ${orderDetails.itemPushKey} as paymentReceived in CompletedOrder: ${e.message}")
                    }
                Toast.makeText(context, "Đã xác nhận nhận đơn hàng!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("HistoryFragment", "Failed to mark order ${orderDetails.itemPushKey} as paymentReceived: ${e.message}")
                Toast.makeText(context, "Lỗi xác nhận đơn hàng", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClicked(orderDetails: OrderDetails) {
        Log.d("HistoryFragment", "Item clicked: ${orderDetails.itemPushKey}")
        val intent = Intent(requireContext(), RecentOrderItems::class.java)
        val orderListToSend = ArrayList<OrderDetails>()
        orderListToSend.add(orderDetails)
        intent.putExtra("recentBuyOrderItem", orderListToSend)
        startActivity(intent)
    }

    override fun onBuyAgainClicked(orderDetails: OrderDetails) {
        Log.d("HistoryFragment", "Đặt lại đơn hàng với pushKey: ${orderDetails.itemPushKey}")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đặt lại đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        if (orderDetails.foodNames.isNullOrEmpty() ||
            orderDetails.foodPrices.isNullOrEmpty() ||
            orderDetails.foodImages.isNullOrEmpty() ||
            orderDetails.foodQuantities.isNullOrEmpty()
        ) {
            Toast.makeText(requireContext(), "Không thể đặt lại đơn hàng này do thiếu thông tin sản phẩm.", Toast.LENGTH_LONG).show()
            Log.e("onBuyAgainClicked", "Order details (foodNames, Prices, Images, or Quantities) are null/empty for push key: ${orderDetails.itemPushKey}")
            return
        }

        val cartRef = database.reference.child("users").child(currentUserId).child("CartItems")
        var itemsSuccessfullyAdded = 0
        val totalItemsToReAdd = orderDetails.foodNames!!.size

        for (i in 0 until totalItemsToReAdd) {
            val foodName = orderDetails.foodNames!![i]
            val foodPrice = orderDetails.foodPrices!![i]
            val foodImage = orderDetails.foodImages!![i]
            val foodQuantity = orderDetails.foodQuantities!![i]
            val foodDescription = "Mô tả mặc định"
            val foodIngredient = "Thành phần mặc định"

            val cartItem = CartItems(
                foodName = foodName,
                foodPrice = foodPrice,
                foodImage = foodImage,
                foodDescription = foodDescription,
                foodIngredient = foodIngredient,
                foodQuantity = foodQuantity
            )

            val cartItemKey = cartRef.push().key
            if (cartItemKey != null) {
                cartRef.child(cartItemKey).setValue(cartItem)
                    .addOnSuccessListener {
                        itemsSuccessfullyAdded++
                        if (itemsSuccessfullyAdded == totalItemsToReAdd) {
                            Toast.makeText(requireContext(), "Đã thêm ${totalItemsToReAdd} món vào giỏ hàng!", Toast.LENGTH_LONG).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("onBuyAgainClicked", "Lỗi khi thêm món '$foodName' (từ đơn ${orderDetails.itemPushKey}) vào giỏ: ${e.message}")
                        if (i == totalItemsToReAdd - 1 && itemsSuccessfullyAdded < totalItemsToReAdd) {
                            Toast.makeText(requireContext(), "Có lỗi khi thêm một số món vào giỏ hàng.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Log.e("onBuyAgainClicked", "Không thể tạo key cho món '$foodName' (từ đơn ${orderDetails.itemPushKey}) trong giỏ hàng.")
                if (i == totalItemsToReAdd - 1 && itemsSuccessfullyAdded < totalItemsToReAdd) {
                    Toast.makeText(requireContext(), "Lỗi tạo key cho sản phẩm trong giỏ hàng.", Toast.LENGTH_LONG).show()
                }
            }
        }
        if (totalItemsToReAdd == 0) {
            Toast.makeText(requireContext(), "Đơn hàng này không có món nào để đặt lại.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (buyHistoryListener != null && ::buyHistoryRef.isInitialized) {
            buyHistoryRef.removeEventListener(buyHistoryListener!!)
        }
    }
}