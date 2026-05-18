package com.example.testapplication.core.network

import com.example.testapplication.features.admin.api.AdminApiService
import com.example.testapplication.features.auth.api.AuthApiService
import com.example.testapplication.features.booking.api.BookingApiService
import com.example.testapplication.features.listing.api.ListingApiService
import com.example.testapplication.features.profile.api.UserApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton holding all Retrofit instances.
 * Supabase client for auth, Backend client for Spring Boot API.
 */
object RetrofitClient {

    // Supabase Project URL
    private const val SUPABASE_URL = "https://dzigcwfyyfezvhdffprk.supabase.co/"

    // Spring Boot backend URL — deployed on Render
    private const val BACKEND_URL = "https://lodgio-backend.onrender.com/"

    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6aWdjd2Z5eWZlenZoZGZmcHJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI4MTk1MzEsImV4cCI6MjA4ODM5NTUzMX0.DQW95WwTQS-20MVb3BT573Mg42TTm1LZDoQzRmmZjHQ"

    private lateinit var supabaseRetrofit: Retrofit
    private lateinit var backendRetrofit: Retrofit

    fun init() {
        // Logging interceptor for debugging
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Shared OkHttpClient with generous timeouts for Render cold starts
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        supabaseRetrofit = Retrofit.Builder()
            .baseUrl(SUPABASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        backendRetrofit = Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(okHttpClient)
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
