package com.example.if570_lab_uts_hosea_00000070462.ui.story

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.repository.StoryRepository
import com.google.firebase.auth.FirebaseAuth

class PostStoryActivity : AppCompatActivity() {

    private lateinit var etStoryText: EditText
    private lateinit var btnAddImage: Button
    private lateinit var ivStoryImage: ImageView
    private lateinit var btnPostStory: Button

    private var selectedImageUri: Uri? = null
    private val storyRepository = StoryRepository()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivStoryImage.setImageURI(selectedImageUri)
            ivStoryImage.visibility = ImageView.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_story)

        etStoryText = findViewById(R.id.etStoryText)
        btnAddImage = findViewById(R.id.btnAddImage)
        ivStoryImage = findViewById(R.id.ivStoryImage)
        btnPostStory = findViewById(R.id.btnPostStory)

        btnAddImage.setOnClickListener {
            checkAndRequestPermissions()
        }

        btnPostStory.setOnClickListener {
            postStory()
        }
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        pickImage.launch("image/*")
    }

    private fun postStory() {
        val storyText = etStoryText.text.toString().trim()
        if (storyText.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please enter text or select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to post a story", Toast.LENGTH_SHORT).show()
            return
        }

        storyRepository.addStory(storyText, selectedImageUri?.toString()) { success ->
            if (success) {
                Toast.makeText(this, "Story posted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to post story", Toast.LENGTH_SHORT).show()
            }
        }
    }
}