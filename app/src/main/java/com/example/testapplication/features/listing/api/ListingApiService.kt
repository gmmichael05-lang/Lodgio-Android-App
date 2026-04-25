package com.example.testapplication.features.listing.api

import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

/**
 * Spring Boot backend → Listing endpoints.
 * Mirrors the web app's listingApi.js
 */
interface ListingApiService {

    // GET /api/listings  — active listings (with optional search param)
    @GET("api/listings")
    fun getActiveListings(
        @Query("search") search: String? = null
    ): Call<List<ListingDTO>>

    // GET /api/listings/search  — advanced filter search
    @GET("api/listings/search")
    fun searchListings(
        @Query("city") city: String? = null,
        @Query("type") type: String? = null,
        @Query("maxPrice") maxPrice: Int? = null,
        @Query("guests") guests: Int? = null,
        @Query("amenities") amenities: String? = null
    ): Call<List<ListingDTO>>

    // GET /api/listings/all  — all listings (admin)
    @GET("api/listings/all")
    fun getAllListings(): Call<List<ListingDTO>>

    // GET /api/listings/{id}
    @GET("api/listings/{id}")
    fun getListingById(@Path("id") id: String): Call<ListingDTO>

    // GET /api/listings/host/{email}
    @GET("api/listings/host/{email}")
    fun getListingsByHostEmail(@Path("email") email: String): Call<List<ListingDTO>>

    // POST /api/listings
    @POST("api/listings")
    fun createListing(@Body payload: JsonObject): Call<JsonObject>

    // PUT /api/listings/{id}
    @PUT("api/listings/{id}")
    fun updateListing(@Path("id") id: String, @Body payload: JsonObject): Call<JsonObject>

    // DELETE /api/listings/{id}
    @DELETE("api/listings/{id}")
    fun deleteListing(@Path("id") id: String): Call<Void>
}
