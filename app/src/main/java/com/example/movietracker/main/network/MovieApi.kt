package com.example.movietracker.main.network

import com.example.movietracker.BuildConfig
import com.example.movietracker.main.entity.MovieDataResponse
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.network.MovieApi.Companion.API_KEY
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val API_KEY = BuildConfig.API_KEY
    }

    @GET("movie/{movie_id}?api_key=$API_KEY")
    suspend fun getMovieData(@Path("movie_id") movieId: String): MovieItem

    @GET("movie/popular?api_key=$API_KEY")
    suspend fun getPopularMovies(
        @Query("page") page: Int
    ): MovieDataResponse

    @GET("movie/top_rated?api_key=$API_KEY")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int
    ): MovieDataResponse

    @GET("search/movie?api_key=$API_KEY")
    suspend fun getQueriedMovies(
        @Query("query") query: String,
        @Query("page") page: Int
    ): MovieDataResponse


}