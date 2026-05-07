package com.example.testapplication.core.network

import com.example.testapplication.features.admin.api.AdminApiService
import com.example.testapplication.features.auth.api.AuthApiService
import com.example.testapplication.features.booking.api.BookingApiService
import com.example.testapplication.features.listing.api.ListingApiService
import com.example.testapplication.features.profile.api.UserApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton holding all Retrofit instances.
 * Supabase client for auth, Backend client for Spring Boot API.
 */
object RetrofitClient {

    // Supabase Project URL
    private const val SUPABASE_URL = "https://dzigcwfyyfezvhdffprk.supabase.co/"

    // Spring Boot backend URL (10.0.2.2 = host localhost from Android emulator)
    private const val BACKEND_URL = "http://10.0.2.2:8080/"

    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6aWdjd2Z5eWZlenZoZGZmcHJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI4MTk1MzEsImV4cCI6MjA4ODM5NTUzMX0.DQW95WwTQS-20MVb3BT573Mg42TTm1LZDoQzRmmZjHQ"

    private lateinit var supabaseRetrofit: Retrofit
    private lateinit var backendRetrofit: Retrofit

    fun init() {
        supabaseRetrofit = Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        backendRetrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ── Feature API Services ──

    val authApi: AuthApiService by lazy {
        supabaseRetrofit.create(AuthApiService::class.java)
    }

    val listingApi: ListingApiService by lazy {
        backendRetrofit.create(ListingApiService::class.java)
    }

    val bookingApi: BookingApiService by lazy {
        backendRetrofit.create(BookingApiService::class.java)
    }

    val userApi: UserApiService by lazy {
        backendRetrofit.create(UserApiService::class.java)
    }

    val adminApi: AdminApiService by lazy {
        backendRetrofit.create(AdminApiService::class.java)
    }

    // ── Supabase Storage API ──

    val supabaseStorageApi: SupabaseStorageApi by lazy {
        supabaseRetrofit.create(SupabaseStorageApi::class.java)
    }
}
