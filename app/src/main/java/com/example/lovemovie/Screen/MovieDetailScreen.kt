package com.example.lovemovie.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lovemovie.Component.FavoriteButton
import com.example.lovemovie.Component.MovieInfo
import com.example.lovemovie.MovieViewModel
import com.example.lovemovie.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    viewModel: MovieViewModel,
    movieId: Int,
    onBackPress: () -> Unit
) {
    val movieDetailState by viewModel.movieDetailState.collectAsState()
    val favoriteMoviesState by viewModel.favoriteMoviesState.collectAsState()
    // 使用 favoriteMoviesState 來確定當前收藏狀態
    val isFavorite = movieId in favoriteMoviesState

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* 留空，讓返回按鈕更醒目 */ },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = movieDetailState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.getMovieDetail(movieId) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("重試")
                        }
                    }
                }

                is UiState.Success -> {
                    state.data?.let { detail ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Box {
                                // 海報圖片
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w500${detail.poster_path}",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(450.dp),
                                    contentScale = ContentScale.Crop
                                )

                                // 收藏按鈕
                                FavoriteButton(
                                    isFavorite = isFavorite,  // 使用共享的收藏狀態
                                    onToggleFavorite = { viewModel.toggleFavorite(movieId) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                )
                            }

                            MovieInfo(
                                title = detail.title,
                                voteAverage = detail.vote_average,
                                overview = detail.overview,
                                titleStyle = MaterialTheme.typography.headlineSmall,
                                maxLines = Int.MAX_VALUE,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}