package com.example.movietracker.main.network.paging

import com.example.movietracker.main.entity.MovieDataResponse
import com.example.movietracker.main.network.MovieApi


class PopularMoviePagingSource(
    private val movieApi: MovieApi
) : MoviePagingSource(movieApi) {

    override suspend fun getResponse(position: Int): MovieDataResponse {
        return movieApi.getPopularMovies(position)
    }
}