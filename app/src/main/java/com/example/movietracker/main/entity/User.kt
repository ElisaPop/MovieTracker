package com.example.movietracker.main.entity

const val BASE_IMAGE_URL = "person-icon.png"

data class User(
    val id: String? = "",
    val name: String? = "",
    val email: String? = "",
    val location: CustomLatLng? = CustomLatLng(0.0, 0.0),
    val imageUrl: String = BASE_IMAGE_URL,
    val bookmark: HashMap<String, BookmarkMovie>? = null
)
