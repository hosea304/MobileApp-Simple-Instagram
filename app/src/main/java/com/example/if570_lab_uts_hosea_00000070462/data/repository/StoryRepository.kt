package com.example.if570_lab_uts_hosea_00000070462.data.repository

import com.example.if570_lab_uts_hosea_00000070462.data.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun addStory(text: String, imageUri: String?, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storyId = UUID.randomUUID().toString()

        if (imageUri != null) {
            val imageRef = storage.reference.child("stories/$storyId.jpg")
            imageRef.putFile(android.net.Uri.parse(imageUri))
                .addOnSuccessListener { taskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveStory(storyId, userId, text, uri.toString(), callback)
                    }
                }
                .addOnFailureListener {
                    callback(false)
                }
        } else {
            saveStory(storyId, userId, text, null, callback)
        }
    }

    private fun saveStory(storyId: String, userId: String, text: String, imageUrl: String?, callback: (Boolean) -> Unit) {
        val story = Story(storyId, userId, text, imageUrl)
        db.collection("stories").document(storyId).set(story)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}