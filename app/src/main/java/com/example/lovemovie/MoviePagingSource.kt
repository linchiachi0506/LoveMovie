package com.example.lovemovie

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.lovemovie.data.Movie
import com.example.lovemovie.data.MovieRepository
import com.example.lovemovie.data.NetworkResult

class MoviePagingSource(
    private val repository: MovieRepository
) : PagingSource<Int, Movie>() {

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        return try {
            val page = params.key ?: 1

            when (val result = repository.getPopularMoviesPaging(page)) {
                is NetworkResult.Success -> {
                    LoadResult.Page(
                        data = result.data.results,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (result.data.results.isEmpty()) null else page + 1
                    )
                }
                is NetworkResult.Error -> {
                    LoadResult.Error(
                        Exception(result.message ?: "Unknown error occurred")
                    )
                }
                NetworkResult.Loading -> {
                    LoadResult.Error(Exception("Loading state not supported in PagingSource"))
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}