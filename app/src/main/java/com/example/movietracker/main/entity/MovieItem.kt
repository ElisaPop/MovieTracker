package com.example.movietracker.main.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieItem(
    val backdrop_path: String? = "",
    val id: Int = 0,
    val original_language: String? = "",
    val original_title: String? = "",
    val overview: String? = "",
    val popularity: Double = 0.0,
    val poster_path: String? = "",
    val release_date: String? = "",
    val title: String? = "",
    val video: Boolean = false,
    val vote_average: Double = 0.0,
    val vote_count: Double = 0.0
) : Parcelable