package com.example.testapplication.api

import com.example.testapplication.models.BackendUser
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface BackendApiService {

    // Register user in Spring Boot backend (public.users table)
    @POST("api/users/register")
    fun registerUser(
        @Body user: BackendUser
    ): Call<JsonObject>

    // Update user profile in Spring Boot backend (public.users table)
    @PUT("api/users/{id}")
    fun updateBackendProfile(
        @Path("id") id: String,
        @Body user: BackendUser
    ): Call<JsonObject>
}
