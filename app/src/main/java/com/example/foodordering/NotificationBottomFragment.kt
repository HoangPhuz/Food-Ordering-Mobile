package com.example.foodordering

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodordering.Adapter.NotificationAdapter
import com.example.foodordering.databinding.FragmentNotificationBottomBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class NotificationBottomFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotificationBottomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBottomBinding.inflate(layoutInflater, container, false)
        val notificationList = arrayListOf("Đơn đặt hàng của bạn đã huỷ thành công", "Đơn hàng đang được tài xế tới lấy", "Đơn hàng đã được đặt thành công");
        val notificationImageList = arrayListOf(R.drawable.sademoji, R.drawable.truck, R.drawable.congrats);
        val notificationAdapter = NotificationAdapter(notificationList, notificationImageList)
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerView.adapter = notificationAdapter
        return binding.root
    }

    companion object {

    }
}