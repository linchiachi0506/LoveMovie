package com.example.lovemovie.Component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp
) {
    IconButton(
        onClick = onToggleFavorite,
        modifier = modifier
            .size(size)
            .background(
                color = backgroundColor,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "取消收藏" else "加入收藏",
            tint = if (isFavorite) Color.Red else Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}