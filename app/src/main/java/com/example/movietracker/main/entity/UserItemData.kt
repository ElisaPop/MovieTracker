package com.example.movietracker.main.entity

import com.google.firebase.storage.StorageReference

data class UserItemData(
    val id: String? = "",
    val name: String? = "",
    val location: CustomLatLng? = CustomLatLng(0.0, 0.0),
    val imageUrl: String = BASE_IMAGE_URL,
    val bookmarkDate: String? = "",
    val pathReference: StorageReference?
)