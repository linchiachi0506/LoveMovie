package com.example.lovemovie

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.lovemovie.data.Movie
import com.example.lovemovie.data.MovieDetail
import com.example.lovemovie.data.MovieRepository
import com.example.lovemovie.data.NetworkResult
import kotlinx.coroutines.flow.Flow
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
    private val repository = MovieRepository.getInstance(application)

    private val _moviesState = MutableStateFlow<UiState<List<Movie>>>(UiState.Success(emptyList()))
    val moviesState = _moviesState.asStateFlow()

    private val _movieDetailState = MutableStateFlow<UiState<MovieDetail?>>(UiState.Success(null))
    val movieDetailState = _movieDetailState.asStateFlow()

    private val _favoriteMoviesState = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteMoviesState = _favoriteMoviesState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    val pagedMovies = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
            initialLoadSize = 20,
            prefetchDistance = 2
        )
    ) {
        MoviePagingSource(repository)
    }.flow.cachedIn(viewModelScope)

    init {
        loadMovies()
        loadFavoriteMovies()
    }

     fun loadMovies(page: Int = 1) {
        viewModelScope.launch {
            repository.getPopularMovies(page).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _moviesState.value = UiState.Loading
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _moviesState.value = UiState.Success(result.data.results)
                        _isLoading.value = false
                        result.data.results.forEach { movie ->
                            updateFavoriteState(movie.id)
                        }
                    }
                    is NetworkResult.Error -> {
                        _moviesState.value = UiState.Error(result.message ?: "Unknown error occurred")
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun loadFavoriteMovies() {
        viewModelScope.launch {
            repository.getFavoriteMovies().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        val favoriteIds = result.data.results.map { it.id }.toSet()
                        _favoriteMoviesState.value = favoriteIds
                    }
                    is NetworkResult.Error -> {
                        Log.e("MovieViewModel", "Failed to load favorites: ${result.message}")
                    }
                    is NetworkResult.Loading -> {
                    }
                }
            }
        }
    }

    fun getMovieDetail(movieId: Int) {
        viewModelScope.launch {
            repository.getMovieDetail(movieId).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _movieDetailState.value = UiState.Loading
                        _isLoading.value = true
                    }
                    is NetworkResult.Success -> {
                        _movieDetailState.value = UiState.Success(result.data)
                        _isLoading.value = false
                        updateFavoriteState(result.data.id)
                    }
                    is NetworkResult.Error -> {
                        _movieDetailState.value = UiState.Error(result.message ?: "Failed to load movie details")
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun toggleFavorite(movieId: Int, movie: Movie) {
        viewModelScope.launch {
            try {
                when (val result = repository.toggleFavorite(movieId, movie)) {
                    is NetworkResult.Success -> {
                        updateFavoriteState(movieId)
                        // 重新加載收藏列表以確保同步
                        loadFavoriteMovies()
                    }
                    is NetworkResult.Error -> {
                        Log.e("MovieViewModel", "Error toggling favorite: ${result.message}")

                    }
                    is NetworkResult.Loading -> {

                    }
                }
            } catch (e: Exception) {
                Log.e("MovieViewModel", "Error toggling favorite", e)
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