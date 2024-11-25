package com.example.lovemovie

import androidx.paging.PagingSource  // 在這行按下 Alt+Enter
import androidx.paging.PagingState
import com.example.lovemovie.data.Movie
import com.example.lovemovie.data.TmdbApi

class MoviePagingSource(
    private val api: TmdbApi  // 使用你的 MovieApi
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
            val response = api.getPopularMovies(page = page)  // 使用你現有的 API 方法

            LoadResult.Page(
                data = response.results,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.results.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}