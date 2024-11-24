package com.example.lovemovie

import android.content.Context
import android.util.Log
import com.android.volley.BuildConfig
import com.example.lovemovie.data.FavoriteBody

import com.example.lovemovie.data.MovieDetail
import com.example.lovemovie.data.MoviesResponse
import com.example.lovemovie.data.TmdbApi

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException

class MovieRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val tmdbApi = retrofit.create(TmdbApi::class.java)

    suspend fun getPopularMovies(page: Int = 1): MoviesResponse {
        return tmdbApi.getPopularMovies(page = page)
    }

    suspend fun getMovieDetail(movieId: Int): MovieDetail {
        return tmdbApi.getMovieDetail(movieId)
    }

    suspend fun toggleFavorite(movieId: Int) {
        val isFavorite = isFavorite(movieId)
        // 切換收藏狀態
        markAsFavorite(movieId, !isFavorite)
        // 更新本地存儲
        sharedPreferences.edit()
            .putBoolean(movieId.toString(), !isFavorite)
            .apply()
    }

    suspend fun markAsFavorite(movieId: Int, favorite: Boolean) {
        val favoriteBody = FavoriteBody(
            media_id = movieId,
            favorite = favorite
        )

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TmdbApi::class.java)

        try {
            api.markAsFavorite(favoriteBody = favoriteBody)
        } catch (e: Exception) {
                       Log.e("API_DEBUG", "API Error: ${e.message}")
        }
    }

    fun isFavorite(movieId: Int): Boolean {
        return sharedPreferences.getBoolean(movieId.toString(), false)
    }
}