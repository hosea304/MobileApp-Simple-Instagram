package com.example.if570_lab_uts_hosea_00000070462.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.repository.AuthRepository
import com.example.if570_lab_uts_hosea_00000070462.ui.main.MainActivity
import com.example.if570_lab_uts_hosea_00000070462.util.ToastUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        authRepository = AuthRepository()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val result = authRepository.login(email, password)
                    if (result.isSuccess) {
                        ToastUtil.showSuccessToast(this@LoginActivity, "Login successful")
                        checkUserProfile()
                    } else {
                        ToastUtil.showErrorToast(
                            this@LoginActivity,
                            result.exceptionOrNull()?.message ?: "Login failed"
                        )
                    }
                }
            } else {
                ToastUtil.showErrorToast(this, "Please fill in all fields")
            }
        }

        googleSignInButton.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }

        registerTextView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        // Optional: If you want to auto-login users who are already authenticated
        // Uncomment the following lines if desired
        /*
        if (auth.currentUser != null) {
            checkUserProfile()
        }
        */
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { token ->
                    lifecycleScope.launch {
                        val result = authRepository.googleSignIn(token)
                        if (result.isSuccess) {
                            ToastUtil.showSuccessToast(this@LoginActivity, "Google Sign-In successful")
                            checkUserProfile()
                        } else {
                            ToastUtil.showErrorToast(
                                this@LoginActivity,
                                result.exceptionOrNull()?.message ?: "Google Sign-In failed"
                            )
                        }
                    }
                }
            } catch (e: ApiException) {
                ToastUtil.showErrorToast(this, "Google Sign-In failed: ${e.message}")
            }
        }
    }

    private fun checkUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val nim = document.getString("nim")
                    if (!name.isNullOrEmpty() && !nim.isNullOrEmpty()) {
                        navigateToMainActivity()
                    } else {
                        navigateToUserInfoActivity()
                    }
                } else {
                    // Document doesn't exist, navigate to UserInfoActivity
                    navigateToUserInfoActivity()
                }
            }
            .addOnFailureListener { e ->
                ToastUtil.showErrorToast(this, "Failed to retrieve user data: ${e.message}")
                // Optionally, navigate to UserInfoActivity or retry
                navigateToUserInfoActivity()
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // This closes the LoginActivity
    }

    private fun navigateToUserInfoActivity() {
        val intent = Intent(this, UserInfoActivity::class.java)
        startActivity(intent)
        finish()  // This closes the LoginActivity
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
