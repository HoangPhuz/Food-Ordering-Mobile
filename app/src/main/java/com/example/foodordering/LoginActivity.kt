package com.example.foodordering

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foodordering.Model.UserModel
import com.example.foodordering.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {
    private val userName : String ?= null
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient


    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Khởi tạo Firebase Auth
        auth = Firebase.auth
        // Khởi tạo Firebase Database
        database = Firebase.database.reference
        // Cấu hình Google Sign-In Options
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Khởi tạo GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //Đăng nhập bằng email và password
        binding.loginButton.setOnClickListener {

            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đầy đủ email/password", Toast.LENGTH_SHORT).show()
            }
            else{
                createUser();
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
            }

        }
        binding.signTV.setOnClickListener {
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }

        //Google Sign-in
        binding.googleButton.setOnClickListener{
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent) // Sử dụng launcher đã khai báo
        }
    }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Xác thực Google thất bại: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                Log.e("GoogleSignIn", "Firebase Auth thất bại: ", authTask.exception)
                            }
                        }
                    } else {
                        Toast.makeText(this, "Không lấy được thông tin tài khoản Google", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Đăng nhập Google thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    Log.e("GoogleSignIn", "Google Sign-In thất bại: ", task.exception)
                }
            } else {
                // Người dùng có thể đã hủy hoặc có lỗi khác
                Toast.makeText(this, "Đăng nhập Google bị hủy hoặc có lỗi", Toast.LENGTH_SHORT).show()
            }

        }


    private fun createUser() {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful) {
                val user: FirebaseUser? = auth.currentUser
                updateUI(user)
            }
            else{
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
                    if(task.isSuccessful) {
                        saveUserToDatabase()
                        val user: FirebaseUser? = auth.currentUser
                        updateUI(user)
                    }
                    else{
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                    }

                }

            }

        }
    }

    private fun saveUserToDatabase() {

        email = binding.email.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(userName, email, password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        database.child("users").child(userId).setValue(user)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser!=null){
            updateUI(currentUser)
        }

    }

    private fun updateUI(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}