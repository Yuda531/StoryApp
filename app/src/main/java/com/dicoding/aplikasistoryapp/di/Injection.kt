package com.dicoding.aplikasistoryapp.di

import android.content.Context
import com.dicoding.aplikasistoryapp.data.UserRepository
import com.dicoding.aplikasistoryapp.data.pref.UserPreference
import com.dicoding.aplikasistoryapp.data.pref.dataStore
import com.dicoding.aplikasistoryapp.data.remote.retrofit.ApiConfig

object Injection {
    fun provideRepository(context: Context): UserRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return UserRepository.getInstance(context, pref, apiService)
    }
}