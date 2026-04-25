package com.example.testapplication.features.admin.api

import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

/**
 * Spring Boot backend → Admin endpoints.
 * Mirrors the web app's adminApi.js
 */
interface AdminApiService {

    @GET("api/users/all")
    fun getAllUsers(): Call<List<JsonObject>>

    @GET("api/listings/all")
    fun getAllListings(): Call<List<ListingDTO>>

    @DELETE("api/users/{id}")
    fun deleteUser(@Path("id") id: String): Call<Void>

    @DELETE("api/listings/{id}")
    fun deleteListing(@Path("id") id: String): Call<Void>
}
