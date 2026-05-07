package com.example.testapplication.features.booking.model

import java.io.Serializable

/**
 * Booking DTO matching Spring Boot's BookingDTO.java response shape.
 */
data class BookingDTO(
    val id: String?,
    val checkInDate: String?,
    val checkOutDate: String?,
    val totalPrice: Double?,
    val status: String?,
    val messageToHost: String?,
    val paymentMethod: String?,
    val createdAt: String?,
    val guest: GuestSummary?,
    val listing: ListingSummary?
) : Serializable

data class GuestSummary(
    val id: String?,
    val fullname: String?,
    val email: String?
) : Serializable

data class ListingSummary(
    val id: String?,
    val title: String?,
    val city: String?,
    val type: String?,
    val guestCapacity: Int?,
    val pricePerNight: Double?,
    val imageUrls: String?
) : Serializable

/**
 * Booked date range for calendar blocking.
 * Matches backend GET /api/bookings/listing/{id}/dates response.
 */
data class BookedDateRange(
    val checkIn: String?,
    val checkOut: String?,
    val status: String?
) : Serializable
