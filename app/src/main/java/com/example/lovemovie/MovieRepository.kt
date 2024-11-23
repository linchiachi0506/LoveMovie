package com.example.lovemovie

import com.example.lovemovie.data.MovieApi
import com.example.lovemovie.data.MovieDetail
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

class MovieRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val movieApi = retrofit.create(MovieApi::class.java)

    suspend fun getMovieDetail(movieId: Int): MovieDetail {
        return movieApi.getMovieDetail(movieId, API_KEY)
    }

    companion object {
        private const val API_KEY = "eb74ce94febc33af53529bf04d1c8811" // 將你的 API key 放在這裡
    }
}