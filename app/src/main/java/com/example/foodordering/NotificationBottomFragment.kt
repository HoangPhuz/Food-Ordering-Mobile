package com.example.foodordering.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels // Import để sử dụng by viewModels()
import androidx.lifecycle.Observer // Import Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.NotificationAdapter
import com.example.foodordering.ViewModel.NotificationViewModel // Import ViewModel
import com.example.foodordering.databinding.FragmentNotificationBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotificationBottomFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationBottomBinding
    private lateinit var notificationAdapter: NotificationAdapter

    // Khởi tạo ViewModel sử dụng KTX extension `by viewModels()`
    // ViewModel này sẽ được liên kết với vòng đời của Fragment.
    private val notificationViewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBottomBinding.inflate(inflater, container, false)
        setupRecyclerView()
        observeViewModel()

        // Ví dụ: Thêm một thông báo mẫu khi Fragment được tạo (bạn sẽ thay thế bằng logic thực tế)
        // Bạn có thể gọi hàm này từ FirebaseMessagingService hoặc nơi khác
        // notificationViewModel.addSampleNotification("Đơn hàng #123 đã được giao!", R.drawable.congrats)
        // notificationViewModel.addSampleNotification("Khuyến mãi tháng 5!", null)


        // (Tùy chọn) Nếu bạn muốn có nút xóa tất cả thông báo
        // binding.clearAllNotificationsButton.setOnClickListener {
        //     notificationViewModel.clearAllNotifications()
        // }

        return binding.root
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter() // Khởi tạo adapter mới (không cần truyền list ban đầu)
        binding.notificationRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
            // itemAnimator = null // Cân nhắc nếu bạn không muốn animation mặc định khi item thay đổi
        }
    }

    private fun observeViewModel() {
        // Quan sát LiveData từ ViewModel
        // Bất cứ khi nào danh sách thông báo trong ViewModel thay đổi,
        // lambda bên trong observe sẽ được thực thi.
        notificationViewModel.notificationsList.observe(viewLifecycleOwner, Observer { notifications ->
            // Cập nhật danh sách cho adapter.
            // ListAdapter sẽ tự động xử lý việc so sánh và cập nhật RecyclerView hiệu quả.
            notificationAdapter.submitList(notifications)

            // (Tùy chọn) Hiển thị/ẩn thông báo "không có thông báo nào"
            // if (notifications.isEmpty()) {
            //     binding.emptyNotificationsTextView.visibility = View.VISIBLE
            //     binding.notificationRecyclerView.visibility = View.GONE
            // } else {
            //     binding.emptyNotificationsTextView.visibility = View.GONE
            //     binding.notificationRecyclerView.visibility = View.VISIBLE
            // }
        })
    }

    companion object {
        // Có thể dùng để tạo instance của Fragment nếu cần truyền arguments
        // fun newInstance(): NotificationBottomFragment {
        //     return NotificationBottomFragment()
        // }
    }
}
