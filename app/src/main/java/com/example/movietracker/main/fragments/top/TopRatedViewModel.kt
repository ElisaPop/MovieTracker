package com.example.movietracker.main.fragments.top

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.network.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TopRatedViewModel @Inject constructor(
    repository: MovieRepository,
    firebaseService: FirebaseService
) : MovieListViewModel(firebaseService) {

    val topMovies = repository.getTopMovies().cachedIn(viewModelScope)

}
