package com.example.if570_lab_uts_hosea_00000070462.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.if570_lab_uts_hosea_00000070462.R
import com.example.if570_lab_uts_hosea_00000070462.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.example.if570_lab_uts_hosea_00000070462.util.ToastUtil

class ProfileFragment : Fragment() {

    private lateinit var ivProfilePicture: ImageView
    private lateinit var etName: EditText
    private lateinit var etNim: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        etName = view.findViewById(R.id.etName)
        etNim = view.findViewById(R.id.etNim)
        btnSave = view.findViewById(R.id.btnSave)
        btnLogout = view.findViewById(R.id.btnLogout)

        ivProfilePicture.setOnClickListener {
            openImageChooser()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        loadProfile()

        return view
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Select Profile Image"),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .transform(CircleCrop())
                .into(ivProfilePicture)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun saveProfile() {
        val name = etName.text.toString().trim()
        val nim = etNim.text.toString().trim()
        val userId = auth.currentUser?.uid ?: return

        val userMap = mutableMapOf<String, Any>(
            "name" to name,
            "nim" to nim
        )

        if (selectedImageUri != null) {
            val profileRef = storage.reference.child("profiles/$userId.jpg")
            profileRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    profileRef.downloadUrl.addOnSuccessListener { uri ->
                        userMap["profileImageUrl"] = uri.toString()
                        updateUserProfile(userId, userMap)
                    }
                }
                .addOnFailureListener { e ->
                    ToastUtil.showErrorToast(
                        requireContext(),
                        "Failed to upload image: ${e.message}"
                    )
                }
        } else {
            updateUserProfile(userId, userMap)
        }
    }

    private fun updateUserProfile(userId: String, userMap: Map<String, Any>) {
        db.collection("users").document(userId).update(userMap)
            .addOnSuccessListener {
                ToastUtil.showSuccessToast(requireContext(), "Profile updated successfully")
            }
            .addOnFailureListener { e ->
                ToastUtil.showErrorToast(requireContext(), "Failed to update profile: ${e.message}")
            }
    }

    private fun loadProfile() {
        val user = auth.currentUser
        val userId = user?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    etName.setText(document.getString("name") ?: "")
                    etNim.setText(document.getString("nim") ?: "")
                    val profileImageUrl = document.getString("profileImageUrl")

                    if (profileImageUrl != null) {
                        Glide.with(this)
                            .load(profileImageUrl)
                            .transform(CircleCrop())
                            .into(ivProfilePicture)
                    } else {
                        // If user registered via Google, use Google profile photo
                        if (user.photoUrl != null) {
                            Glide.with(this)
                                .load(user.photoUrl)
                                .transform(CircleCrop())
                                .into(ivProfilePicture)
                        } else {
                            Glide.with(this)
                                .load(R.drawable.ic_person)
                                .transform(CircleCrop())
                                .into(ivProfilePicture)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                ToastUtil.showErrorToast(requireContext(), "Failed to load profile: ${e.message}")
            }
    }
}