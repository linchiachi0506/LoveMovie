package com.example.lovemovie
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lovemovie.Screen.MovieDetailScreen
import com.example.lovemovie.Screen.MovieListScreen
import com.example.lovemovie.ui.theme.LoveMovieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoveMovieTheme {
                MovieNavigation()
            }
        }
    }
}
@Composable
fun MovieNavigation() {
    val navController = rememberNavController()
    // 在導航的最頂層創建共享的 ViewModel
    val viewModel: MovieViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "movie_list"
    ) {
        composable("movie_list") {
            MovieListScreen(
                viewModel = viewModel,  // 將共享的 ViewModel 傳遞給 ListScreen
                onMovieClick = { movieId ->
                    navController.navigate("movie_detail/$movieId")
                }
            )
        }

        composable(
            route = "movie_detail/{movieId}",
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                movieId = movieId,
                viewModel = viewModel,  // 將相同的 ViewModel 實例傳遞給 DetailScreen
                onBackPress = {
                    navController.navigateUp()
                }
            )
        }
    }
}