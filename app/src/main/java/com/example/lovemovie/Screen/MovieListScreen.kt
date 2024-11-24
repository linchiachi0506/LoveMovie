package com.example.lovemovie.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "查看收藏"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = moviesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                // 簡單的錯誤提示
                Text(
                    text = state.message,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }

            is UiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = paddingValues,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    val displayMovies = if (showFavorites) {
                        state.data.filter { it.id in favoriteMoviesState }
                    } else {
                        state.data
                    }

                    items(displayMovies) { movie ->
                        MovieItem(
                            movie = movie,
                            isFavorite = movie.id in favoriteMoviesState,
                            onMovieClick = onMovieClick,
                            onToggleFavorite = { viewModel.toggleFavorite(movie.id) }
                        )
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