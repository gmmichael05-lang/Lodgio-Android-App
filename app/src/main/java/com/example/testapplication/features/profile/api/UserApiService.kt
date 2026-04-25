package com.example.testapplication.features.profile.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

/**
 * Spring Boot backend → User endpoints.
 * Mirrors the web app's userApi.js
 */
interface UserApiService {

    // GET /api/users/{email}
    @GET("api/users/{email}")
    fun getUserByEmail(@Path("email") email: String): Call<JsonObject>

    // POST /api/users/register
    @POST("api/users/register")
    fun registerUser(@Body user: JsonObject): Call<JsonObject>

    // PUT /api/users/{id}
    @PUT("api/users/{id}")
    fun updateUser(@Path("id") id: String, @Body user: JsonObject): Call<JsonObject>

    // PUT /api/users/{id}/profile-picture
    @PUT("api/users/{id}/profile-picture")
    fun updateProfilePicture(
        @Path("id") id: String,
        @Body imageUrl: String
    ): Call<JsonObject>

    // GET /api/users/all
    @GET("api/users/all")
    fun getAllUsers(): Call<List<JsonObject>>

    // DELETE /api/users/{id}
    @DELETE("api/users/{id}")
    fun deleteUser(@Path("id") id: String): Call<Void>
}
