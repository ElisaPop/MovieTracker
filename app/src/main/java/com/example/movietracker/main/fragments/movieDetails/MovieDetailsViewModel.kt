package com.example.movietracker.main.fragments.movieDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.BaseServiceModel
import com.example.movietracker.main.entity.MovieItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    firebaseService: FirebaseService
) : BaseServiceModel(firebaseService) {

    private val _selectedMovie = MutableLiveData<MovieItem>()

    val selectedMovie: LiveData<MovieItem>
        get() = _selectedMovie

    fun setSelectedMovie(movieDetails: MovieItem) {
        _selectedMovie.value = movieDetails
    }
}