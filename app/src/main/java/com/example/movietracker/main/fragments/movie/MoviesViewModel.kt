package com.example.movietracker.main.fragments.movie

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.movietracker.firebase.FirebaseService
import com.example.movietracker.main.MovieListViewModel
import com.example.movietracker.main.network.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val repository: MovieRepository,
    firebaseService: FirebaseService
) : MovieListViewModel(firebaseService) {

    private val popularMovies = repository.getPopularMovies().cachedIn(viewModelScope)

    private val currentQuery = MutableLiveData(DEFAULT_QUERY)
    val queryMovies = currentQuery.switchMap { queryString ->
        if (queryString.isNullOrEmpty()) {
            popularMovies
        } else {
            repository.getQueriedMovies(queryString).cachedIn(viewModelScope)
        }
    }

    fun searchMovies(query: String) {
        currentQuery.value = query
    }

    companion object {
        private const val DEFAULT_QUERY = ""
    }
}