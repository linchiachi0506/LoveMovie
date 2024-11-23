package com.example.lovemovie

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovemovie.data.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MovieRepository(application)

    // StateFlow 用於保存和觀察收藏狀態
    private val _favoriteMovies = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val favoriteMovies: StateFlow<Map<Int, Boolean>> = _favoriteMovies

    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun toggleFavorite(movieId: Int) {
        repository.toggleFavorite(movieId)
        // 更新 StateFlow 中的狀態
        updateFavoriteState(movieId)
    }

    fun isFavorite(movieId: Int): Boolean {
        return repository.isFavorite(movieId)
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
            } finally {
                _isLoading.value = false
            }
        }
    }
}