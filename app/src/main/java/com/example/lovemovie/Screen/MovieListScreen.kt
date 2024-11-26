package com.example.lovemovie.Screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lovemovie.Component.EmptyContent
import com.example.lovemovie.Component.ErrorContent
import com.example.lovemovie.Component.FavoriteButton
import com.example.lovemovie.Component.MovieInfo
import com.example.lovemovie.MovieViewModel
import com.example.lovemovie.UiState
import com.example.lovemovie.data.Movie


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: MovieViewModel
) {
    val moviesState by viewModel.moviesState.collectAsState()
    val favoriteMoviesState by viewModel.favoriteMoviesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showFavorites by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showFavorites) "我的收藏" else "熱門電影") },
                actions = {
                    IconButton(
                        onClick = { showFavorites = !showFavorites }
                    ) {
                        Icon(
                            imageVector = if (showFavorites) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = "切換收藏列表",
                            tint = if (showFavorites) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = moviesState) {
                is UiState.Success -> {
                    val displayMovies = if (showFavorites) {
                        state.data.filter { it.id in favoriteMoviesState }
                    } else {
                        state.data
                    }

                    if (displayMovies.isEmpty()) {
                        EmptyContent(
                            message = if (showFavorites) {
                                "還沒有收藏的電影\n趕快去收藏喜歡的電影吧！"
                            } else {
                                "沒有找到電影"
                            }
                        ) {
                            Icon(
                                imageVector = if (showFavorites) {
                                    Icons.Default.FavoriteBorder
                                } else {
                                    Icons.Default.Movie
                                },
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = paddingValues,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = displayMovies,
                                key = { it.id }
                            ) { movie ->
                                MovieItem(
                                    movie = movie,
                                    isFavorite = movie.id in favoriteMoviesState,
                                    onMovieClick = onMovieClick,
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(movie.id, movie)
                                    }
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorContent(
                        error = state.message,
                        onRetry = { viewModel.loadMovies() }
                    )
                }

                is UiState.Loading -> {
                    // Loading content is handled by the overlay
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun MovieItem(
    movie: Movie,
    isFavorite: Boolean,
    onMovieClick: (Int) -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable { onMovieClick(movie.id) },
        shape = RoundedCornerShape(8.dp)
    ) {
        Box {
            Column {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentScale = ContentScale.Crop
                )

                MovieInfo(
                    title = movie.title,
                    voteAverage = movie.vote_average,
                    releaseDate = movie.release_date,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }

            FavoriteButton(
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
    }
}