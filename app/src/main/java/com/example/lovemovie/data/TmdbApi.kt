package com.example.lovemovie.data


import com.example.lovemovie.BuildConfig
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
interface TmdbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("language") language: String = "zh-TW",
        @Query("page") page: Int = 1
    ): MoviesResponse
    @GET("movie/popular")
    suspend fun getPopularMoviesPaging(
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("language") language: String = "zh-TW"
    ): MoviesResponse
    @GET("movie/{movieId}")
    suspend fun getMovieDetail(
        @Path("movieId") movieId: Int,
        @Query("api_key") apiKey: String = BuildConfig.TMDB_API_KEY,
        @Query("language") language: String = "zh-TW"
    ): MovieDetail

    @GET("account/21643797/favorite/movies")
    suspend fun getFavoriteMovies(
        @Header("accept") accept: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.TMDB_AUTH_TOKEN}",
        @Query("language") language: String = "zh-TW",
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "created_at.asc"
    ): MoviesResponse


    @POST("account/21643797/favorite")
    suspend fun markAsFavorite(
        @Header("accept") accept: String = "application/json",
        @Header("content-type") contentType: String = "application/json",
        @Header("Authorization") authorization: String = "Bearer ${BuildConfig.TMDB_AUTH_TOKEN}",
        @Body favoriteBody: FavoriteBody
    )
}
