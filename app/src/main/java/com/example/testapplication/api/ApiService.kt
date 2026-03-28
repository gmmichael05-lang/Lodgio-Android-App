package com.example.testapplication.api

import com.example.testapplication.models.*
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    companion object {
        const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImR6aWdjd2Z5eWZlenZoZGZmcHJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI4MTk1MzEsImV4cCI6MjA4ODM5NTUzMX0.DQW95WwTQS-20MVb3BT573Mg42TTm1LZDoQzRmmZjHQ"
    }

    // Register - use JsonObject to see raw response from Supabase
    @POST("auth/v1/signup")
    fun register(
        @Header("apikey") apiKey: String = SUPABASE_KEY,
        @Body request: RegisterRequest
    ): Call<JsonObject>

    @POST("auth/v1/token?grant_type=password")
    fun login(
        @Header("apikey") apiKey: String = SUPABASE_KEY,
        @Body request: LoginRequest
    ): Call<AuthResponse>

    @GET("auth/v1/user")
    fun getProfile(
        @Header("apikey") apiKey: String = SUPABASE_KEY,
        @Header("Authorization") token: String
    ): Call<SupabaseUser>

    @PUT("auth/v1/user")
    fun updateProfile(
        @Header("apikey") apiKey: String = SUPABASE_KEY,
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Call<SupabaseUser>

    @PUT("auth/v1/user")
    fun changePassword(
        @Header("apikey") apiKey: String = SUPABASE_KEY,
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Call<SupabaseUser>
}
