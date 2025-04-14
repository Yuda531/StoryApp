package com.dicoding.aplikasistoryapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.aplikasistoryapp.data.UserRepository
import com.dicoding.aplikasistoryapp.data.pref.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.dicoding.aplikasistoryapp.data.Result
import kotlinx.coroutines.flow.Flow

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MutableStateFlow<Result<UserModel>?>(null)
    val loginResult: StateFlow<Result<UserModel>?> = _loginResult.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).collect { result ->
                _loginResult.value = result
            }
        }
    }

    fun getSession(): Flow<UserModel> {
        return repository.getSession()
    }
}