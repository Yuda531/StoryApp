package com.dicoding.aplikasistoryapp.ui.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.aplikasistoryapp.data.UserRepository
import com.dicoding.aplikasistoryapp.data.pref.UserModel
import com.dicoding.aplikasistoryapp.data.remote.response.ListStoryItem
import com.dicoding.aplikasistoryapp.data.Result

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    private val _storyListWithLocation = MediatorLiveData<Result<List<ListStoryItem>>>()
    val storyListWithLocation: LiveData<Result<List<ListStoryItem>>> = _storyListWithLocation

    fun getStoriesWithLocation(token: String) {
        val liveData = repository.getStoriesWithLocation(token)
        _storyListWithLocation.addSource(liveData) { result ->
            _storyListWithLocation.value = result
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }
}