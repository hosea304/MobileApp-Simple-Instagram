package com.example.if570_lab_uts_hosea_00000070462.data.model
import com.google.firebase.Timestamp

data class Story(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    var likes: Int = 0,
    var isLiked: Boolean = false,
    var isPinned: Boolean = false,
    var isSaved: Boolean = false,
    val timestamp: Timestamp? = null
)
