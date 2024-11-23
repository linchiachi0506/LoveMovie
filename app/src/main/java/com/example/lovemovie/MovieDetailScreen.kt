package com.example.lovemovie

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
@Composable
fun MovieDetailScreen(
    viewModel: MovieViewModel,
    movieId: Int
) {
    val movieDetail by viewModel.movieDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isFavorite by remember { derivedStateOf { viewModel.isFavorite(movieId) } }

    LaunchedEffect(movieId) {
        viewModel.getMovieDetail(movieId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        movieDetail?.let { detail ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box {
                    // 封面圖片
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${detail.poster_path}",
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(450.dp),
                        contentScale = ContentScale.Crop
                    )

                    // 收藏按鈕
                    IconButton(
                        onClick = { viewModel.toggleFavorite(detail.id) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            },
                            contentDescription = "收藏",
                            tint = if (isFavorite) {
                                Color.Red
                            } else {
                                Color.White
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // 內容區域
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 標題
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 評分
                    Text(
                        text = "★ ${detail.vote_average}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 簡介
                    Text(
                        text = detail.overview,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}