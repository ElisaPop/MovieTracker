package com.example.movietracker.main.entity

data class MovieDataResponse(
    val results: List<MovieItem>,
    val total_results: Int
)