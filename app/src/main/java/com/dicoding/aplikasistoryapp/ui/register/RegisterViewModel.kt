package com.dicoding.aplikasistoryapp.ui.register


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.aplikasistoryapp.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dicoding.aplikasistoryapp.data.Result

class RegisterViewModel(private val repository: UserRepository) : ViewModel() {
    private val _registerResult = MutableStateFlow<Result<String>?>(null)
    val registerResult: StateFlow<Result<String>?> = _registerResult.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(name, email, password).collect { result ->
                _registerResult.value = result
            }
        }
    }
}