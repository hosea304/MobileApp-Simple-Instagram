package com.example.if570_lab_uts_hosea_00000070462.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.repository.AuthRepository
import com.example.if570_lab_uts_hosea_00000070462.ui.main.MainActivity
import com.example.if570_lab_uts_hosea_00000070462.util.ToastUtil
import kotlinx.coroutines.launch
import android.widget.Button
import android.widget.EditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        authRepository = AuthRepository()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)

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
                            navigateToMainActivity()
                        } else {
                            ToastUtil.showErrorToast(this@RegisterActivity, result.exceptionOrNull()?.message ?: "Registration failed")
                        }
                    }
                } else {
                    ToastUtil.showErrorToast(this, "Passwords do not match")
                }
            } else {
                ToastUtil.showErrorToast(this, "Please fill in all fields")
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
