package com.example.lovemovie.data


import android.content.Context
import android.net.ConnectivityManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class NoConnectivityException : IOException("網路連線不可用，請檢查網路設定")
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(
        val code: Int? = null,
        val message: String? = null,
        val exception: Exception? = null
    ) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

class MovieRepository private constructor(private val context: Context) {
    private val favoritesPrefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val cachePrefs = context.getSharedPreferences("movie_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(createOkHttpClient())
        .build()

    private val tmdbApi = retrofit.create(TmdbApi::class.java)

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (!isNetworkAvailable()) {
                    throw NoConnectivityException()
                }
                chain.proceed(chain.request())
            }
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                if (!response.isSuccessful) {
                    throw coil.network.HttpException(response)
                }
                response
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getPopularMovies(page: Int = 1): Flow<NetworkResult<MoviesResponse>> = flow {
        emit(NetworkResult.Loading)

        // 先嘗試讀取緩存
        val cachedData = getCachedMovies("popular_$page")
        if (cachedData != null) {
            emit(NetworkResult.Success(cachedData))
        }

        try {
            val response = tmdbApi.getPopularMovies(page = page)
            cacheMovies("popular_$page", response)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            // 如果有緩存數據就不發送錯誤
            if (cachedData == null) {
                emit(handleNetworkError(e))
            }
        }
    }

    fun getMovieDetail(movieId: Int): Flow<NetworkResult<MovieDetail>> = flow {
        emit(NetworkResult.Loading)

        // 讀取緩存
        val cachedDetail = getCachedMovieDetail(movieId)
        if (cachedDetail != null) {
            emit(NetworkResult.Success(cachedDetail))
        }

        try {
            val response = tmdbApi.getMovieDetail(movieId)
            cacheMovieDetail(movieId, response)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            if (cachedDetail == null) {
                emit(handleNetworkError(e))
            }
        }
    }

    fun getFavoriteMovies(page: Int = 1): Flow<NetworkResult<MoviesResponse>> = flow {
        emit(NetworkResult.Loading)

        // 讀取本地收藏列表
        val localFavorites = getLocalFavoriteMovies()
        if (localFavorites.isNotEmpty()) {
            emit(
                NetworkResult.Success(
                    MoviesResponse(
                        page = 1,
                        results = localFavorites,
                        total_pages = 1,
                        total_results = localFavorites.size
                    )
                )
            )
        }

        try {
            val response = tmdbApi.getFavoriteMovies(page = page)
            // 更新本地收藏清單
            updateLocalFavorites(response.results)
            emit(NetworkResult.Success(response))
        } catch (e: Exception) {
            if (localFavorites.isEmpty()) {
                emit(handleNetworkError(e))
            }
        }
    }
    suspend fun getPopularMoviesPaging(page: Int): NetworkResult<MoviesResponse> {
        // 先檢查緩存
        val cachedData = getCachedMovies("popular_$page")
        if (cachedData != null) {
            return NetworkResult.Success(cachedData)
        }

        return try {
            val response = tmdbApi.getPopularMovies(page = page)
            // 緩存數據
            cacheMovies("popular_$page", response)
            NetworkResult.Success(response)
        } catch (e: Exception) {
            handleNetworkError(e)
        }
    }
    suspend fun toggleFavorite(movieId: Int, movie: Movie): NetworkResult<Boolean> {
        val isFavorite = isFavorite(movieId)
        return try {
            // 先更新本地狀態
            updateLocalFavoriteStatus(movieId, !isFavorite, movie)

            // 同步到服務器
            val favoriteBody = FavoriteBody(
                media_type = "movie",
                media_id = movieId,
                favorite = !isFavorite
            )
            tmdbApi.markAsFavorite(favoriteBody = favoriteBody)
            NetworkResult.Success(!isFavorite)
        } catch (e: Exception) {
            // 如果服務器同步失敗，回滾本地狀態
            updateLocalFavoriteStatus(movieId, isFavorite, movie)
            handleNetworkError(e)
        }
    }

    fun isFavorite(movieId: Int): Boolean {
        return favoritesPrefs.getBoolean(movieId.toString(), false)
    }

    private fun updateLocalFavoriteStatus(movieId: Int, isFavorite: Boolean, movie: Movie) {
        favoritesPrefs.edit().apply {
            putBoolean(movieId.toString(), isFavorite)
            // 同時保存電影數據
            if (isFavorite) {
                putString("movie_$movieId", gson.toJson(movie))
            } else {
                remove("movie_$movieId")
            }
            apply()
        }
    }

    private fun getLocalFavoriteMovies(): List<Movie> {
        val movies = mutableListOf<Movie>()
        favoritesPrefs.all.forEach { (key, value) ->
            if (!key.startsWith("movie_")) return@forEach
            val movieId = key.removePrefix("movie_")
            if (isFavorite(movieId.toInt())) {
                favoritesPrefs.getString(key, null)?.let {
                    movies.add(gson.fromJson(it, Movie::class.java))
                }
            }
        }
        return movies
    }

    private fun updateLocalFavorites(movies: List<Movie>) {
        favoritesPrefs.edit().apply {
            movies.forEach { movie ->
                putBoolean(movie.id.toString(), true)
                putString("movie_${movie.id}", gson.toJson(movie))
            }
            apply()
        }
    }

    private fun cacheMovies(key: String, response: MoviesResponse) {
        cachePrefs.edit()
            .putString(key, gson.toJson(response))
            .putLong("${key}_timestamp", System.currentTimeMillis())
            .apply()
    }

    private fun getCachedMovies(key: String): MoviesResponse? {
        val json = cachePrefs.getString(key, null) ?: return null
        val timestamp = cachePrefs.getLong("${key}_timestamp", 0)

        // 緩存有效期為 5 分鐘
        if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
            return null
        }

        return try {
            gson.fromJson(json, MoviesResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun cacheMovieDetail(movieId: Int, detail: MovieDetail) {
        cachePrefs.edit()
            .putString("detail_$movieId", gson.toJson(detail))
            .putLong("detail_${movieId}_timestamp", System.currentTimeMillis())
            .apply()
    }

    private fun getCachedMovieDetail(movieId: Int): MovieDetail? {
        val json = cachePrefs.getString("detail_$movieId", null) ?: return null
        val timestamp = cachePrefs.getLong("detail_${movieId}_timestamp", 0)

        // 緩存有效期為 5 分鐘
        if (System.currentTimeMillis() - timestamp > 5 * 60 * 1000) {
            return null
        }

        return try {
            gson.fromJson(json, MovieDetail::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null
    }

    private fun handleNetworkError(e: Exception): NetworkResult.Error {
        return when (e) {
            is IOException -> NetworkResult.Error(
                message = "網路連接錯誤，請檢查網路連接",
                exception = e
            )
            is SocketTimeoutException -> NetworkResult.Error(
                message = "連接超時，請稍後重試",
                exception = e
            )
            is HttpException -> {
                val code = e.code()
                val errorMessage = when (code) {
                    401 -> "認證失敗，請重新登入"
                    404 -> "找不到資源"
                    429 -> "請求次數過多，請稍後再試"
                    500 -> "伺服器錯誤，請稍後重試"
                    503 -> "服務暫時不可用，請稍後重試"
                    else -> "發生錯誤(錯誤碼: $code)"
                }
                NetworkResult.Error(code = code, message = errorMessage, exception = e)
            }
            else -> NetworkResult.Error(
                message = "未知錯誤: ${e.message}",
                exception = e
            )
        }
    }

    companion object {
        @Volatile
        private var instance: MovieRepository? = null

        fun getInstance(context: Context): MovieRepository {
            return instance ?: synchronized(this) {
                instance ?: MovieRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}