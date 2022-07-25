package com.example.movietracker.main.network.paging

import com.example.movietracker.main.entity.MovieDataResponse
import com.example.movietracker.main.network.MovieApi


class QueryMoviePagingSource(
    private val movieApi: MovieApi,
    private val query: String
) : MoviePagingSource(movieApi) {

    override suspend fun getResponse(position: Int): MovieDataResponse {
        return movieApi.getQueriedMovies(query, position)
    }

}