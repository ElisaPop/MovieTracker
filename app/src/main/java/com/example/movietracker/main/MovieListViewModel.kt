package com.example.movietracker.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.entity.MovieItem
import com.google.firebase.database.ValueEventListener

abstract class MovieListViewModel(
    private val firebaseService: FirebaseService
) : BaseServiceModel(firebaseService) {

    private val _movieData = MutableLiveData<MovieItem?>()
    val movieItem: LiveData<MovieItem?>
        get() = _movieData

    fun displayMovieDetails(movie: MovieItem) {
        _movieData.value = movie
    }

    fun displayMovieDetailsComplete() {
        _movieData.value = null
    }

    fun isBookmarked(
        movieItem: MovieItem,
        valueEventListener: ValueEventListener
    ): Boolean {
        val currentUser = firebaseService.getCurrentUser() ?: return false
        firebaseService.isBookmarked(movieItem, currentUser.uid, valueEventListener)
        return true
    }

    fun addBookmark(movieItem: MovieItem) {
        firebaseService.addBookmark(movieItem, firebaseService.getCurrentUser()?.uid ?: "")
    }

    open fun removeBookmark(movieItem: MovieItem) {
        firebaseService.removeBookmark(movieItem, firebaseService.getCurrentUser()?.uid ?: "")
    }
}