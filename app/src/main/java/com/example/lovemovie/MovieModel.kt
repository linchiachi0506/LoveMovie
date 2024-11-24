package com.example.lovemovie

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovemovie.data.Movie
import com.example.lovemovie.data.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies = _movies.asStateFlow()

    private val _favoriteMovies = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteMovies: StateFlow<Map<Int, Boolean>> = _favoriteMovies

    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _favoriteMoviesState = MutableStateFlow<Set<Int>>(setOf())
    val favoriteMoviesState = _favoriteMoviesState.asStateFlow()
    init {
        loadMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getPopularMovies()
                _movies.value = response.results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun isFavorite(movieId: Int): Boolean {
        return repository.isFavorite(movieId)
    }
    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            try {
                val isFavorite = repository.isFavorite(movieId)
                repository.markAsFavorite(movieId, !isFavorite)
                // 更新本地存儲
                repository.toggleFavorite(movieId)
                // 更新 StateFlow 中的狀態
                updateFavoriteState(movieId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun testMarkAsFavorite() {
        viewModelScope.launch {
            try {
                Log.d("API_DEBUG", "Testing markAsFavorite API...")
                repository.markAsFavorite(763215, true)
                Log.d("API_DEBUG", "API 調用成功")
            } catch (e: Exception) {
                Log.e("API_DEBUG", "API 錯誤: ${e.message}")
                Log.e("API_DEBUG", "Stack trace: ", e)
            }
        }
    }


    private fun updateFavoriteState(movieId: Int) {
        val currentMap = _favoriteMovies.value.toMutableMap()
        currentMap[movieId] = repository.isFavorite(movieId)
        _favoriteMovies.value = currentMap
    }

    fun getMovieDetail(movieId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.getMovieDetail(movieId)
                _movieDetail.value = response
            } catch (e: Exception) {
                // 處理錯誤
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}