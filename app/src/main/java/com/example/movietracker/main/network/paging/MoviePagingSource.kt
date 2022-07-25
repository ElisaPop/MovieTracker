package com.example.movietracker.main.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.movietracker.main.entity.MovieDataResponse
import com.example.movietracker.main.entity.MovieItem
import com.example.movietracker.main.network.MovieApi
import retrofit2.HttpException
import java.io.IOException

private const val MOVIE_STARTING_PAGE_INDEX = 1

abstract class MoviePagingSource(
    private val movieApi: MovieApi
) : PagingSource<Int, MovieItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieItem> {
        val position = params.key ?: MOVIE_STARTING_PAGE_INDEX

        return try {
            val response = getResponse(position)
            response.let {
                val movies = it.results
                LoadResult.Page(
                    data = movies,
                    prevKey = if (position == MOVIE_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = if (movies.isEmpty()) null else position + 1
                )
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    abstract suspend fun getResponse(position: Int): MovieDataResponse
}