package com.example.testapplication.features.booking.api

import com.example.testapplication.features.booking.model.BookingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

/**
 * Spring Boot backend → Booking endpoints.
 * Mirrors the web app's bookingApi.js
 */
interface BookingApiService {

    // GET /api/bookings/guest/{email}
    @GET("api/bookings/guest/{email}")
    fun getBookingsByGuestEmail(@Path("email") email: String): Call<List<BookingDTO>>

    // GET /api/bookings/host/{email}
    @GET("api/bookings/host/{email}")
    fun getBookingsByHostEmail(@Path("email") email: String): Call<List<BookingDTO>>

    // POST /api/bookings
    @POST("api/bookings")
    fun createBooking(@Body payload: JsonObject): Call<JsonObject>

    // PUT /api/bookings/{id}/status
    @PUT("api/bookings/{id}/status")
    fun updateBookingStatus(
        @Path("id") id: String,
        @Body statusPayload: JsonObject
    ): Call<JsonObject>
}
