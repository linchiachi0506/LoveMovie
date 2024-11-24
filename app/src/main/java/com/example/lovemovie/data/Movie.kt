package com.example.lovemovie.data

data class Movie(
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)
data class MoviesResponse(
    val results: List<Movie>
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String,
    val vote_average: Double,
    val runtime: Int?,
    val genres: List<Genre>,
    val production_companies: List<ProductionCompany>,
)

data class Genre(
    val id: Int,
    val name: String
)

data class ProductionCompany(
    val id: Int,
    val name: String,
    val logo_path: String?,
    val origin_country: String
)

data class FavoriteBody(
    val media_id: Int,
    val media_type: String = "movie",
    val favorite: Boolean
)