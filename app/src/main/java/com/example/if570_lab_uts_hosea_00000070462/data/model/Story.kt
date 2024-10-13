package com.example.if570_lab_uts_hosea_00000070462.data.model

data class Story(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0
)