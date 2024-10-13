package com.example.if570_lab_uts_hosea_00000070462.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.data.repository.StoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class PostStoryFragment : Fragment() {

    private lateinit var etStoryText: EditText
    private lateinit var btnAddImage: Button
    private lateinit var ivStoryImage: ImageView
    private lateinit var btnPostStory: Button
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private val storyRepository = StoryRepository()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show()
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                ivStoryImage.setImageURI(selectedImageUri)
                ivStoryImage.visibility = ImageView.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_story, container, false)

        etStoryText = view.findViewById(R.id.etStoryText)
        btnAddImage = view.findViewById(R.id.btnAddImage)
        ivStoryImage = view.findViewById(R.id.ivStoryImage)
        btnPostStory = view.findViewById(R.id.btnPostStory)
        progressBar = view.findViewById(R.id.progressBar)

        btnAddImage.setOnClickListener {
            checkAndRequestPermissions()
        }

        btnPostStory.setOnClickListener {
            postStory()
        }

        return view
    }

    private fun checkAndRequestPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
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
            Toast.makeText(requireContext(), "Please enter text or select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "You must be logged in to post a story", Toast.LENGTH_SHORT).show()
            return
        }

        btnPostStory.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0

        val storyId = UUID.randomUUID().toString()
        if (selectedImageUri != null) {
            val imageRef = FirebaseStorage.getInstance().reference.child("stories/$storyId.jpg")
            val uploadTask = imageRef.putFile(selectedImageUri!!)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressBar.progress = progress
            }.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save the story with the image URL
                    storyRepository.saveStory(storyId, currentUser.uid, storyText, uri.toString()) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Story posted successfully", Toast.LENGTH_SHORT).show()
                            // Optionally, navigate back or clear the fields
                            clearFields()
                            // Navigate back to HomeFragment
                            parentFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Failed to post story", Toast.LENGTH_SHORT).show()
                            btnPostStory.isEnabled = true
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                btnPostStory.isEnabled = true
                progressBar.visibility = View.GONE
            }
        } else {
            // No image selected, save the story without image URL
            storyRepository.saveStory(storyId, currentUser.uid, storyText, null) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Story posted successfully", Toast.LENGTH_SHORT).show()
                    // Optionally, navigate back or clear the fields
                    clearFields()
                    // Navigate back to HomeFragment
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to post story", Toast.LENGTH_SHORT).show()
                    btnPostStory.isEnabled = true
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun clearFields() {
        etStoryText.text.clear()
        ivStoryImage.setImageURI(null)
        ivStoryImage.visibility = View.GONE
        selectedImageUri = null
        btnPostStory.isEnabled = true
        progressBar.visibility = View.GONE
    }
}
