package com.example.lovemovie

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lovemovie.data.MovieDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private val repository = MovieRepository()

    private val _favoriteMovies = mutableStateMapOf<Int, Boolean>()
    val favoriteMovies: Map<Int, Boolean> = _favoriteMovies
    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    fun toggleFavorite(movieId: Int) {
        _favoriteMovies[movieId] = !(_favoriteMovies[movieId] ?: false)
    }

    fun isFavorite(movieId: Int): Boolean {
        return _favoriteMovies[movieId] ?: false
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