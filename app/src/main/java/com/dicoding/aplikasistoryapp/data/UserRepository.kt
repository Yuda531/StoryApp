package com.dicoding.aplikasistoryapp.data

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.aplikasistoryapp.R
import com.dicoding.aplikasistoryapp.data.pref.UserModel
import com.dicoding.aplikasistoryapp.data.pref.UserPreference
import com.dicoding.aplikasistoryapp.data.remote.response.ListStoryItem
import com.dicoding.aplikasistoryapp.data.remote.response.UploadStoryResponse
import com.dicoding.aplikasistoryapp.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class UserRepository private constructor(
    private val context: Context,
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    fun login(email: String, password: String): Flow<Result<UserModel>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            if (!response.error!!) {
                val user = UserModel(
                    email = email,
                    token = response.loginResult?.token.orEmpty(),
                    isLogin = true
                )
                userPreference.saveSession(user)
                emit(Result.Success(user))
            } else {
                emit(Result.Error(response.message ?: context.getString(R.string.failure_try_again)))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is HttpException -> {
                    val errorResponse = e.response()?.errorBody()?.string()
                    parseErrorMessage(errorResponse)
                }

                is IOException -> context.getString(R.string.error_connection_failed)
                else -> context.getString(R.string.error_unknown)
            }
            emit(Result.Error(errorMessage))
        }
    }

    fun register(name: String, email: String, password: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)
            if (!response.error!!) {
                emit(Result.Success(response.message.orEmpty()))
            } else {
                emit(Result.Error(response.message ?: context.getString(R.string.failure_try_again)))
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is HttpException -> {
                    val errorResponse = e.response()?.errorBody()?.string()
                    parseErrorMessage(errorResponse)
                }

                is IOException -> context.getString(R.string.error_connection_failed)
                else -> context.getString(R.string.error_unknown)
            }
            emit(Result.Error(errorMessage))
        }
    }

    fun getStoriesPaging(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                StoryPagingSource("Bearer $token", apiService)
            }
        ).liveData
    }

    fun uploadStory(
        token: String,
        description: RequestBody,
        file: MultipartBody.Part,
        currentLocation: Location?
    ): LiveData<Result<UploadStoryResponse>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = if (currentLocation != null) {
                    apiService.uploadStory(
                        "Bearer $token",
                        description,
                        file,
                        currentLocation.latitude.toString()
                            .toRequestBody("text/plain".toMediaType()),
                        currentLocation.longitude.toString()
                            .toRequestBody("text/plain".toMediaType())
                    )
                } else {
                    apiService.uploadStory("Bearer $token", description, file )
                }
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }

    fun getStoriesWithLocation(token: String): LiveData<Result<List<ListStoryItem>>> =
        liveData(Dispatchers.IO) {
            emit(Result.Loading)
            try {
                val response = apiService.getStoriesWithLocation("Bearer $token")
                val storyList = response.listStory
                emit(Result.Success(storyList))
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
            }
        }


    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            context: Context,
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(context, userPreference, apiService)
            }.also { instance = it }
    }

    private fun parseErrorMessage(response: String?): String {
        return try {
            val jsonObject = JSONObject(response ?: "")
            jsonObject.getString("message")
        } catch (e: JSONException) {
            context.getString(R.string.error_unknown)
        }
    }
}