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
import com.example.foodordering.databinding.ActivitySignBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var username: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    private val binding: ActivitySignBinding by lazy {
        ActivitySignBinding.inflate(layoutInflater)
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

        binding.signUpButton.setOnClickListener {
            username = binding.userName.text.toString().trim()
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vui lòng điền đầy đủ thông tin để đăng ký",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                createAccount(email, password)
            }
        }

        binding.loginTV.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.googleButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent) // Sử dụng launcher đã khai báo
        }
    }

    // Launcher for Google Sign-In result
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount? = task.result
                    if (account != null) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                // Lưu thông tin người dùng từ Google vào Database
                                //saveGoogleUserToDatabase(account)
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

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                val currentUsername = binding.userName.text.toString().trim()
                saveUserToDatabase(currentUsername, email, password)
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                Log.d("Tài khoản", "Tạo tài khoản: Thất bại", task.exception)
            }
        }
    }

    private fun saveUserToDatabase(usernameToSave: String, emailToSave: String, passwordToSave: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val user = UserModel(usernameToSave, emailToSave, passwordToSave) // Sử dụng password đã mã hóa bởi Firebase Auth hoặc để trống nếu không muốn lưu raw password
            database.child("users").child(userId).setValue(user)
                .addOnSuccessListener {
                    Log.d("Database", "Lưu thông tin người dùng thành công")
                }
                .addOnFailureListener { e ->
                    Log.e("Database", "Lỗi khi lưu thông tin người dùng: ", e)
                }
        } else {
            Log.e("Database", "Không lấy được User ID để lưu thông tin")
        }
    }

    private fun saveGoogleUserToDatabase(googleAccount: GoogleSignInAccount) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val usernameFromGoogle = googleAccount.displayName ?: googleAccount.email ?: "User" // Lấy displayName hoặc email làm username
            val emailFromGoogle = googleAccount.email
            if (emailFromGoogle != null) {
                val user = UserModel(usernameFromGoogle, emailFromGoogle, "")
                database.child("users").child(userId).setValue(user)
                    .addOnSuccessListener {
                        Log.d("Database", "Lưu thông tin người dùng Google thành công")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Database", "Lỗi khi lưu thông tin người dùng Google: ", e)
                    }
            } else {
                Log.e("Database", "Không lấy được email từ tài khoản Google")
                Toast.makeText(this, "Không thể lấy email từ tài khoản Google để lưu.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("Database", "Không lấy được User ID để lưu thông tin người dùng Google")
        }
    }
}