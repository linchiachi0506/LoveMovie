package com.example.lovemovie

import com.example.lovemovie.data.MovieApi
import com.example.lovemovie.data.MovieDetail
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import android.content.SharedPreferences


class MovieRepository (private val context: Context){
    private val sharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val movieApi = retrofit.create(MovieApi::class.java)

    suspend fun getMovieDetail(movieId: Int): MovieDetail {
        return movieApi.getMovieDetail(movieId, API_KEY)
    }
    fun toggleFavorite(movieId: Int) {
        val isFavorite = isFavorite(movieId)
        sharedPreferences.edit()
            .putBoolean(movieId.toString(), !isFavorite)
            .apply()
    }
    fun isFavorite(movieId: Int): Boolean {
        // 第二個參數是默認值，如果找不到這個 key 就返回這個默認值
        return sharedPreferences.getBoolean(movieId.toString(), false)
    }
    companion object {
        private const val API_KEY = "eb74ce94febc33af53529bf04d1c8811" // 將你的 API key 放在這裡
    }
}