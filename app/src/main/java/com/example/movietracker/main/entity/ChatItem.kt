package com.example.movietracker.main.entity

import com.google.firebase.storage.StorageReference

data class ChatItem(
    val userId: String? = "",
    val imageUrl: String? = "",
    val userName: String? = "",
    val senderId: String? = "",
    val message: String? = "",
    val pathReference: StorageReference?
)