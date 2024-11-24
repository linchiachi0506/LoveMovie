package com.example.lovemovie.Component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp



@Composable
fun MovieInfo(
    title: String,
    voteAverage: Double,
    modifier: Modifier = Modifier,
    releaseDate: String? = null,
    overview: String? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    maxLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = titleStyle,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (releaseDate != null) Arrangement.SpaceBetween else Arrangement.Start
        ) {
            Text(
                text = "★ $voteAverage",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            releaseDate?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        overview?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}