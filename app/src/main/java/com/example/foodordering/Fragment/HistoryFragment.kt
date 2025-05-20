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
import com.example.foodordering.Adapter.PreviousOrderAdapter
import com.example.foodordering.Adapter.PendingOrderAdapter
import com.example.foodordering.Model.CartItems // Đảm bảo import CartItems
import com.example.foodordering.Model.OrderDetails
import com.example.foodordering.R
import com.example.foodordering.RecentOrderItems
import com.example.foodordering.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment(), PendingOrderAdapter.OnItemInteractionListener, PreviousOrderAdapter.OnItemInteractionListener {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String

    private var allOrderDetails: MutableList<OrderDetails> = mutableListOf()
    private var pendingOrderList: MutableList<OrderDetails> = mutableListOf()
    private var previousOrderList: MutableList<OrderDetails> = mutableListOf()

    private lateinit var pendingOrderAdapter: PendingOrderAdapter
    private lateinit var previousOrderAdapter: PreviousOrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        setupRecyclerViews()
        retrieveOrderHistory()

        return binding.root
    }

    private fun setupRecyclerViews() {
        // Pending Orders RecyclerView
        pendingOrderAdapter = PendingOrderAdapter(requireContext(), pendingOrderList, this)
        binding.pendingOrdersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingOrderAdapter
        }

        // Previous Orders RecyclerView
        previousOrderAdapter = PreviousOrderAdapter(requireContext(), previousOrderList, this)
        binding.previousOrdersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = previousOrderAdapter
        }
    }

    private fun retrieveOrderHistory() {
        if (userId.isEmpty()) {
            Log.e("HistoryFragment", "User ID is empty. Cannot retrieve history.")
            updateEmptyStateViews()
            return
        }

        val buyHistoryRef = database.reference.child("users").child(userId).child("BuyHistory")
        val sortingQuery = buyHistoryRef.orderByChild("currentTime")

        sortingQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrderDetails.clear()
                pendingOrderList.clear()
                previousOrderList.clear()

                for (buySnapshot in snapshot.children) {
                    val buyHistoryItem = buySnapshot.getValue(OrderDetails::class.java)
                    buyHistoryItem?.let {
                        allOrderDetails.add(it)
                    }
                }
                allOrderDetails.reverse()

                for (order in allOrderDetails) {
                    if (order.orderAccepted && !order.paymentReceived) {
                        pendingOrderList.add(order)
                    } else if (order.orderAccepted && order.paymentReceived) {
                        previousOrderList.add(order)
                    }
                }

                // Cập nhật adapter với bản sao của danh sách để tránh lỗi tham chiếu
                pendingOrderAdapter.updateList(ArrayList(pendingOrderList))
                previousOrderAdapter.updateList(ArrayList(previousOrderList))
                updateEmptyStateViews()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryFragment", "Failed to retrieve order history: ${error.message}")
                Toast.makeText(context, "Lỗi tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
                updateEmptyStateViews()
            }
        })
    }

    private fun updateEmptyStateViews() {
        binding.tvNoPendingOrders.visibility = if (pendingOrderList.isEmpty()) View.VISIBLE else View.GONE
        binding.pendingOrdersRecyclerView.visibility = if (pendingOrderList.isEmpty()) View.GONE else View.VISIBLE

        binding.tvNoPreviousOrders.visibility = if (previousOrderList.isEmpty()) View.VISIBLE else View.GONE
        binding.previousOrdersRecyclerView.visibility = if (previousOrderList.isEmpty()) View.GONE else View.VISIBLE
    }


    // --- Implement PendingOrderAdapter.OnItemInteractionListener ---
    override fun onReceivedButtonClicked(orderDetails: OrderDetails, position: Int) {
        orderDetails.itemPushKey?.let { pushKey ->
            val userOrderRef = database.reference.child("users").child(userId).child("BuyHistory").child(pushKey)
            val completedOrderRef = database.reference.child("CompletedOrder").child(pushKey)

            val updates = hashMapOf<String, Any>("paymentReceived" to true)

            userOrderRef.updateChildren(updates)
                .addOnSuccessListener {
                    completedOrderRef.updateChildren(updates)
                        .addOnFailureListener { e ->
                            Log.e("HistoryFragment", "Failed to mark order ${pushKey} as received in CompletedOrder: ${e.message}")
                        }
                    Toast.makeText(context, "Đã xác nhận nhận đơn hàng!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("HistoryFragment", "Failed to mark order ${pushKey} as received: ${e.message}")
                    Toast.makeText(context, "Lỗi xác nhận đơn hàng", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // --- Implement BuyAgainAdapter.OnItemInteractionListener ---
    override fun onBuyAgainClicked(orderDetails: OrderDetails, position: Int) {
        Log.d("HistoryFragment", "Đặt lại cho đơn hàng với pushKey: ${orderDetails.itemPushKey}")

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đặt lại đơn hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Kiểm tra xem các danh sách cần thiết trong orderDetails có null hoặc rỗng không
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
        val totalItemsToReAdd = orderDetails.foodNames!!.size // Số lượng món ăn trong đơn hàng cũ

        // Lặp qua từng món ăn trong đơn hàng cũ để thêm vào giỏ hàng mới
        for (i in 0 until totalItemsToReAdd) {
            val foodName = orderDetails.foodNames!![i]
            val foodPrice = orderDetails.foodPrices!![i]
            val foodImage = orderDetails.foodImages!![i]
            val foodQuantity = orderDetails.foodQuantities!![i]

            // Lưu ý: Model OrderDetails của bạn không có foodDescription và foodIngredient
            // Nếu bạn muốn thêm chúng, bạn cần cập nhật model OrderDetails và cách nó được tạo trong PayOutActivity
            // Hiện tại, chúng ta sẽ để trống hoặc dùng giá trị mặc định cho chúng trong CartItems.
            val foodDescription = "Mô tả mặc định" // Hoặc lấy từ orderDetails nếu có
            val foodIngredient = "Thành phần mặc định" // Hoặc lấy từ orderDetails nếu có

            val cartItem = CartItems(
                foodName = foodName,
                foodPrice = foodPrice,
                foodImage = foodImage,
                foodDescription = foodDescription, // Cần cập nhật nếu bạn có dữ liệu này trong OrderDetails
                foodIngredient = foodIngredient,   // Cần cập nhật nếu bạn có dữ liệu này trong OrderDetails
                foodQuantity = foodQuantity
            )

            // Tạo một key mới cho mỗi item trong giỏ hàng
            val cartItemKey = cartRef.push().key
            if (cartItemKey != null) {
                cartRef.child(cartItemKey).setValue(cartItem)
                    .addOnSuccessListener {
                        itemsSuccessfullyAdded++
                        // Kiểm tra xem tất cả các item đã được thêm thành công chưa
                        if (itemsSuccessfullyAdded == totalItemsToReAdd) {
                            Toast.makeText(requireContext(), "Đã thêm ${totalItemsToReAdd} món vào giỏ hàng!", Toast.LENGTH_LONG).show()
                            // Tùy chọn: Điều hướng người dùng đến CartFragment
                            // Ví dụ: (activity as? YourMainActivity)?.navigateToFragment(CartFragment())
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("onBuyAgainClicked", "Lỗi khi thêm món '$foodName' (từ đơn ${orderDetails.itemPushKey}) vào giỏ: ${e.message}")
                        // Có thể hiển thị một thông báo lỗi chung nếu một vài món không thêm được
                        if (i == totalItemsToReAdd - 1 && itemsSuccessfullyAdded < totalItemsToReAdd) { // Kiểm tra ở item cuối cùng
                            Toast.makeText(requireContext(), "Có lỗi khi thêm một số món vào giỏ hàng.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Log.e("onBuyAgainClicked", "Không thể tạo key cho món '$foodName' (từ đơn ${orderDetails.itemPushKey}) trong giỏ hàng.")
                if (i == totalItemsToReAdd - 1 && itemsSuccessfullyAdded < totalItemsToReAdd) { // Kiểm tra ở item cuối cùng
                    Toast.makeText(requireContext(), "Lỗi tạo key cho sản phẩm trong giỏ hàng.", Toast.LENGTH_LONG).show()
                }
            }
        }

        if (totalItemsToReAdd == 0) { // Trường hợp đơn hàng cũ không có món nào (ít khả năng xảy ra)
            Toast.makeText(requireContext(), "Đơn hàng này không có món nào để đặt lại.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClicked(orderDetails: OrderDetails) {
        Log.d("HistoryFragment", "Item clicked: ${orderDetails.itemPushKey}")
        val intent = Intent(requireContext(), RecentOrderItems::class.java)
        val orderListToSend = ArrayList<OrderDetails>()
        orderListToSend.add(orderDetails)
        intent.putExtra("recentBuyOrderItem", orderListToSend) // RecentOrderItems cần nhận ArrayList<OrderDetails>
        startActivity(intent)
    }
}
