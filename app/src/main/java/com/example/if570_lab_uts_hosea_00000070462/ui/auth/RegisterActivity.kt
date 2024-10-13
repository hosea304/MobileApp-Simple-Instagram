package com.example.if570_lab_uts_hosea_00000070462.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.repository.AuthRepository
import com.example.if570_lab_uts_hosea_00000070462.ui.auth.LoginActivity
import com.example.if570_lab_uts_hosea_00000070462.util.ToastUtil
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle back arrow click
        toolbar.setNavigationOnClickListener {
            finish() // Closes the activity and returns to the previous one
        }

        // Initialize AuthRepository
        authRepository = AuthRepository()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val backToLoginTextView = findViewById<TextView>(R.id.backToLoginTextView)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    lifecycleScope.launch {
                        val result = authRepository.register(email, password)
                        if (result.isSuccess) {
                            ToastUtil.showSuccessToast(this@RegisterActivity, "Registration successful")
                            navigateToUserInfoActivity()
                        } else {
                            ToastUtil.showErrorToast(
                                this@RegisterActivity,
                                result.exceptionOrNull()?.message ?: "Registration failed"
                            )
                        }
                    }
                } else {
                    ToastUtil.showErrorToast(this, "Passwords do not match")
                }
            } else {
                ToastUtil.showErrorToast(this, "Please fill in all fields")
            }
        }

        // Handle "Back to Login" click
        backToLoginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToUserInfoActivity() {
        val intent = Intent(this, UserInfoActivity::class.java)
        startActivity(intent)
        finish()
    }
}
