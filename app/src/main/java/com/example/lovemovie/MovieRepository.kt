package com.example.lovemovie

import android.content.Context
import android.util.Log
import com.example.lovemovie.data.FavoriteBody
import com.example.lovemovie.data.MovieDetail
import com.example.lovemovie.data.MoviesResponse
import com.example.lovemovie.data.TmdbApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class MovieRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tmdbApi = retrofit.create(TmdbApi::class.java)

    suspend fun getPopularMovies(page: Int = 1): Result<MoviesResponse> {
        return try {
            Result.success(tmdbApi.getPopularMovies(page = page))
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Failed to get popular movies: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getMovieDetail(movieId: Int): Result<MovieDetail> {
        return try {
            Result.success(tmdbApi.getMovieDetail(movieId))
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Failed to get movie detail: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFavoriteMovies(page: Int = 1): Result<MoviesResponse> {
        return try {
            Result.success(tmdbApi.getFavoriteMovies(page = page))
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Failed to get favorite movies: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(movieId: Int) {
        val isFavorite = isFavorite(movieId)
        try {
            markAsFavorite(movieId, !isFavorite)
            // 更新本地存儲
            sharedPreferences.edit()
                .putBoolean(movieId.toString(), !isFavorite)
                .apply()
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Failed to toggle favorite: ${e.message}")
            // 如果API調用失敗，可能需要回滾本地狀態
            throw e
        }
    }

    private suspend fun markAsFavorite(movieId: Int, favorite: Boolean) {
        val favoriteBody = FavoriteBody(
            media_type = "movie",  // 添加必要的 media_type
            media_id = movieId,
            favorite = favorite
        )

        tmdbApi.markAsFavorite(favoriteBody = favoriteBody)
    }

    fun isFavorite(movieId: Int): Boolean {
        return sharedPreferences.getBoolean(movieId.toString(), false)
    }

    // 新增同步遠程收藏狀態到本地的方法
    suspend fun syncFavorites() {
        try {
            val favorites = tmdbApi.getFavoriteMovies().results
            sharedPreferences.edit().apply {
                clear() // 清除舊的收藏狀態
                favorites.forEach { movie ->
                    putBoolean(movie.id.toString(), true)
                }
                apply()
            }
        } catch (e: Exception) {
            Log.e("API_DEBUG", "Failed to sync favorites: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: MovieRepository? = null

        fun getInstance(context: Context): MovieRepository {
            return instance ?: synchronized(this) {
                instance ?: MovieRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}