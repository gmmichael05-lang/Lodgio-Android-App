package com.example.testapplication.features.listing.model

import java.io.Serializable

/**
 * Listing DTO matching Spring Boot's ListingDTO.java response shape.
 */
data class ListingDTO(
    val id: String?,
    val title: String?,
    val description: String?,
    val pricePerNight: Double?,
    val guestCapacity: Int?,
    val amenities: String?,
    val imageUrls: String?,
    val status: String?,
    val type: String?,
    val beds: Int?,
    val baths: Int?,
    val city: String?,
    val location: String?,
    val createdAt: String?,
    val host: HostSummary?
) : Serializable

data class HostSummary(
    val id: String?,
    val fullname: String?,
    val email: String?,
    val profilePictureUrl: String?
) : Serializable
