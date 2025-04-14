package com.dicoding.aplikasistoryapp.ui.upload

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.aplikasistoryapp.data.Result
import com.dicoding.aplikasistoryapp.data.UserRepository
import com.dicoding.aplikasistoryapp.data.pref.UserModel
import com.dicoding.aplikasistoryapp.data.remote.response.UploadStoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadStoryViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uploadStoryResponse = MediatorLiveData<Result<UploadStoryResponse>>()
    val uploadStoryResponse: LiveData<Result<UploadStoryResponse>> = _uploadStoryResponse

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun uploadStory(
        token: String,
        description: RequestBody,
        file: MultipartBody.Part,
        currentLocation: Location?
    ) {
        val liveData = repository.uploadStory(token,  description, file, currentLocation)
        _uploadStoryResponse.addSource(liveData) { result ->
            _uploadStoryResponse.value = result
        }

    }
}