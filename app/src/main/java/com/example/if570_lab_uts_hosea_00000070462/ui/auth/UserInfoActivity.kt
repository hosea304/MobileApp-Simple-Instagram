package com.example.if570_lab_uts_hosea_00000070462.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.if570_lab_uts_hosea_00000070462.R
import android.widget.Button
import android.widget.EditText
import com.example.if570_lab_uts_hosea_00000070462.ui.main.MainActivity
import com.example.if570_lab_uts_hosea_00000070462.util.ToastUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserInfoActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val nimEditText = findViewById<EditText>(R.id.nimEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val nim = nimEditText.text.toString().trim()

            if (name.isNotEmpty() && nim.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: return@setOnClickListener
                val userMap = hashMapOf(
                    "name" to name,
                    "nim" to nim
                )
                db.collection("users").document(userId).set(userMap)
                    .addOnSuccessListener {
                        ToastUtil.showSuccessToast(this, "Information saved successfully")
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        ToastUtil.showErrorToast(this, "Failed to save information: ${e.message}")
                    }
            } else {
                ToastUtil.showErrorToast(this, "Please fill in all fields")
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
