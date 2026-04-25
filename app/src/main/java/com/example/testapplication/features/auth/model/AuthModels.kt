package com.example.testapplication.features.auth.model

import com.google.gson.annotations.SerializedName

// ── Supabase GoTrue Auth models ──

data class SupabaseUserMetadata(
    val fullname: String,
    val role: String,
    val mobileNumber: String?
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val data: SupabaseUserMetadata
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SupabaseUser(
    val id: String?,
    val email: String?,
    val user_metadata: SupabaseUserMetadata?
)

data class AuthResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Int?,
    val refresh_token: String?,
    val user: SupabaseUser?
)

data class UpdateProfileRequest(
    val data: SupabaseUserMetadata
)

data class ChangePasswordRequest(
    val password: String
)

// ── Spring Boot Backend User Model ──

data class BackendUser(
    val id: String,
    val email: String,
    val fullname: String,
    val role: String,
    val mobileNumber: String?,
    val profilePictureUrl: String? = null,
    val contactNumbers: String? = null,
    val savedCards: String? = null
)
