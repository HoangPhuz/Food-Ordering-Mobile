package com.example.foodordering.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.foodordering.LoginActivity
import com.example.foodordering.Model.UserModel
import com.example.foodordering.R
import com.example.foodordering.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.logoutButton.setOnClickListener{
            auth.signOut()
            Log.d("logout", "User signed out") // Thay đổi log cho rõ ràng hơn

            // Sử dụng requireContext() để lấy Context
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Thêm các flags để xóa stack Activity cũ và tạo task mới cho LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

            // Gọi finish() trên Activity chứa Fragment này
            activity?.finish()
        }
        binding.saveInforButton.setOnClickListener {
            val name = binding.name.text.toString()
            val address = binding.address.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.email.text.toString()
            updateUserData(name, address, phone, email)
            binding.name.isEnabled = false
            binding.email.isEnabled = false
            binding.address.isEnabled = false
            binding.phone.isEnabled = false
        }
        setUserData()
        binding.apply {
            name.isEnabled = false
            email.isEnabled = false
            address.isEnabled = false
            phone.isEnabled = false
        }
        binding.editButton.setOnClickListener{
            binding.apply {
                name.isEnabled = !name.isEnabled
                email.isEnabled = !email.isEnabled
                address.isEnabled = !address.isEnabled
                phone.isEnabled = !phone.isEnabled
            }
        }
        return binding.root
    }

    private fun updateUserData(name: String, address: String, phone: String, email: String) {
        val userId = auth.currentUser?.uid
        if(userId != null) {
            val userRef = database.reference.child("users").child(userId)
            val userData = hashMapOf(
                "username" to name,
                "address" to address,
                "phone" to phone,
                "email" to email
            ).toMap()
            userRef.setValue(userData).addOnSuccessListener {
                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if(userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if(userProfile != null) {
                            binding.name.setText(userProfile.username)
                            binding.address.setText(userProfile.address)
                            binding.email.setText(userProfile.email)
                            binding.phone.setText(userProfile.phone)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
}