package com.example.lovemovie.data

import com.android.volley.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
// TmdbApi.kt
interface TmdbApi {
    @GET("discover/movie")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = "eb74ce94febc33af53529bf04d1c8811",
        @Query("language") language: String = "zh-TW",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("include_video") includeVideo: Boolean = false,
        @Query("page") page: Int = 1
    ): MoviesResponse
}

interface MovieApi {
    @GET("movie/{movieId}")
    suspend fun getMovieDetail(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String="eb74ce94febc33af53529bf04d1c8811",
        @Query("language") language: String = "zh-TW"
    ): MovieDetail
}