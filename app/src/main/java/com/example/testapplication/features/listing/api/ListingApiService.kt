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

    // PUT /api/listings/{id}/toggle-active — pause/resume listing
    @PUT("api/listings/{id}/toggle-active")
    fun toggleListingActive(@Path("id") id: String): Call<Void>

    // ── Favorites ──

    // GET /api/favorites/{email} — get user's saved listings
    @GET("api/favorites/{email}")
    fun getFavorites(@Path("email") email: String): Call<List<ListingDTO>>

    // GET /api/favorites/{email}/{listingId} — check if favorited
    @GET("api/favorites/{email}/{listingId}")
    fun isFavorited(
        @Path("email") email: String,
        @Path("listingId") listingId: String
    ): Call<JsonObject>

    // POST /api/favorites/{email}/{listingId} — add favorite
    @POST("api/favorites/{email}/{listingId}")
    fun addFavorite(
        @Path("email") email: String,
        @Path("listingId") listingId: String
    ): Call<Void>

    // DELETE /api/favorites/{email}/{listingId} — remove favorite
    @DELETE("api/favorites/{email}/{listingId}")
    fun removeFavorite(
        @Path("email") email: String,
        @Path("listingId") listingId: String
    ): Call<Void>

    // ── Reviews ──

    // GET /api/reviews/listing/{listingId} — reviews for a listing
    @GET("api/reviews/listing/{listingId}")
    fun getReviewsForListing(@Path("listingId") listingId: String): Call<List<JsonObject>>

    // GET /api/reviews/listing/{listingId}/summary — rating summary
    @GET("api/reviews/listing/{listingId}/summary")
    fun getReviewSummary(@Path("listingId") listingId: String): Call<JsonObject>

    // POST /api/reviews — submit review
    @POST("api/reviews")
    fun submitReview(@Body payload: JsonObject): Call<JsonObject>

    // GET /api/reviews/guest/{email}/reviewed-bookings — which bookings have been reviewed
    @GET("api/reviews/guest/{email}/reviewed-bookings")
    fun getReviewedBookings(@Path("email") email: String): Call<List<String>>
}
