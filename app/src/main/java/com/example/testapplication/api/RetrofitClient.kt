package com.example.testapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Supabase Project URL
    private const val BASE_URL = "https://dzigcwfyyfezvhdffprk.supabase.co/"

    // Spring Boot backend URL
    // 10.0.2.2 is the Android emulator alias for host machine's localhost
    private const val BACKEND_URL = "http://10.0.2.2:8080/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val backendInstance: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
    }
}
