package com.example.movietracker.main.network

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.example.movietracker.main.network.paging.PopularMoviePagingSource
import com.example.movietracker.main.network.paging.QueryMoviePagingSource
import com.example.movietracker.main.network.paging.TopMoviePagingSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(private val movieApi: MovieApi) {

    fun getQueriedMovies(query: String) =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { QueryMoviePagingSource(movieApi, query) }
        ).liveData

    fun getPopularMovies() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PopularMoviePagingSource(movieApi) }
        ).liveData

    fun getTopMovies() =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                maxSize = 100,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { TopMoviePagingSource(movieApi) }
        ).liveData

    suspend fun getMovieData(movieId: String) =
        movieApi.getMovieData(movieId)

}