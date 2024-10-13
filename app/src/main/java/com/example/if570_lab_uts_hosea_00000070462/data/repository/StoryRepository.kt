package com.example.if570_lab_uts_hosea_00000070462.data.repository

import android.util.Log
import com.example.if570_lab_uts_hosea_00000070462.data.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class StoryRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getStories(callback: (List<Story>) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        // Get the user's likedStories and pinnedStories arrays
        val userRef = db.collection("users").document(userId)
        userRef.get().addOnSuccessListener { userSnapshot ->
            val likedStories = userSnapshot.get("likedStories") as? List<String> ?: emptyList()
            val pinnedStories = userSnapshot.get("pinnedStories") as? List<String> ?: emptyList()

            // Get the stories
            db.collection("stories")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val stories = result.toObjects(Story::class.java)

                    // Log the number of stories retrieved
                    Log.d("StoryRepository", "Retrieved ${stories.size} stories from Firestore")

                    // Set isLiked and isPinned
                    stories.forEach { story ->
                        story.isLiked = likedStories.contains(story.id)
                        story.isPinned = pinnedStories.contains(story.id)
                    }

                    // Sort stories so that pinned stories appear at the top
                    val sortedStories = stories.sortedWith(
                        compareByDescending<Story> { it.isPinned }
                            .thenByDescending { it.timestamp?.seconds }
                    )

                    callback(sortedStories)
                }
                .addOnFailureListener { exception ->
                    Log.e("StoryRepository", "Error retrieving stories", exception)
                    callback(emptyList())
                }
        }.addOnFailureListener { exception ->
            Log.e("StoryRepository", "Error retrieving user data", exception)
            callback(emptyList())
        }
    }

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

    fun likeStory(storyId: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storyRef = db.collection("stories").document(storyId)
        val likedByRef = storyRef.collection("likedBy").document(userId)
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val likedBySnapshot = transaction.get(likedByRef)
            if (likedBySnapshot.exists()) {
                transaction.delete(likedByRef)
                transaction.update(storyRef, "likes", FieldValue.increment(-1))
                transaction.update(userRef, "likedStories", FieldValue.arrayRemove(storyId))
            } else {
                transaction.set(likedByRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.update(storyRef, "likes", FieldValue.increment(1))
                transaction.update(userRef, "likedStories", FieldValue.arrayUnion(storyId))
            }
        }.addOnSuccessListener {
            callback(true)
        }.addOnFailureListener { exception ->
            Log.e("StoryRepository", "Error in likeStory transaction", exception)
            callback(false)
        }
    }

    fun saveStory(storyId: String, userId: String, text: String, imageUrl: String?, callback: (Boolean) -> Unit) {
        val storyData = hashMapOf(
            "id" to storyId,
            "userId" to userId,
            "text" to text,
            "imageUrl" to imageUrl,
            "likes" to 0,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("stories").document(storyId).set(storyData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { exception ->
                Log.e("StoryRepository", "Error saving story", exception)
                callback(false)
            }
    }



    fun pinStory(storyId: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        // Get current pinnedStories
        userRef.get().addOnSuccessListener { snapshot ->
            val pinnedStories = snapshot.get("pinnedStories") as? List<String> ?: emptyList()
            val update = if (pinnedStories.contains(storyId)) {
                mapOf("pinnedStories" to FieldValue.arrayRemove(storyId))
            } else {
                mapOf("pinnedStories" to FieldValue.arrayUnion(storyId))
            }
            userRef.update(update)
                .addOnSuccessListener { callback(true) }
                .addOnFailureListener { exception ->
                    Log.e("StoryRepository", "Error in pinStory update", exception)
                    callback(false)
                }
        }.addOnFailureListener { exception ->
            Log.e("StoryRepository", "Error fetching user data in pinStory", exception)
            callback(false)
        }
    }


}