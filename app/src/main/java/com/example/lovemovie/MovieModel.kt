package com.example.lovemovie

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovemovie.data.Movie
import com.example.lovemovie.data.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    private val _moviesState = MutableStateFlow<UiState<List<Movie>>>(UiState.Success(emptyList()))
    val moviesState = _moviesState.asStateFlow()

    private val _movieDetailState = MutableStateFlow<UiState<MovieDetail?>>(UiState.Success(null))
    val movieDetailState = _movieDetailState.asStateFlow()

    private val _favoriteMoviesState = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteMoviesState = _favoriteMoviesState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadMovies()
        loadFavoriteMovies()
    }

    private fun loadMovies(page: Int = 1) {
        viewModelScope.launch {
            _moviesState.value = UiState.Loading
            try {
                repository.getPopularMovies(page).fold(
                    onSuccess = { response ->
                        _moviesState.value = UiState.Success(response.results)
                        // 更新收藏狀態
                        response.results.forEach { movie ->
                            updateFavoriteState(movie.id)
                        }
                    },
                    onFailure = { e ->
                        _moviesState.value = UiState.Error(e.message ?: "Unknown error occurred")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFavoriteMovies() {
        viewModelScope.launch {
            try {
                repository.getFavoriteMovies().fold(
                    onSuccess = { response ->
                        val favoriteIds = response.results.map { it.id }.toSet()
                        _favoriteMoviesState.value = favoriteIds
                    },
                    onFailure = { e ->
                        Log.e("MovieViewModel", "Failed to load favorites: ${e.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error loading favorites", e)
            }
        }
    }

    fun getMovieDetail(movieId: Int) {
        viewModelScope.launch {
            _movieDetailState.value = UiState.Loading
            try {
                repository.getMovieDetail(movieId).fold(
                    onSuccess = { detail ->
                        _movieDetailState.value = UiState.Success(detail)
                        updateFavoriteState(detail.id)
                    },
                    onFailure = { e ->
                        _movieDetailState.value = UiState.Error(e.message ?: "Failed to load movie details")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(movieId)
                updateFavoriteState(movieId)
                // 重新加載收藏列表以確保同步
                loadFavoriteMovies()
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error toggling favorite", e)
                // 可以添加錯誤通知機制
            }
        }
    }

    private fun updateFavoriteState(movieId: Int) {
        val isFavorite = repository.isFavorite(movieId)
        _favoriteMoviesState.update { currentSet ->
            if (isFavorite) {
                currentSet + movieId
            } else {
                currentSet - movieId
            }
        }
    }


}