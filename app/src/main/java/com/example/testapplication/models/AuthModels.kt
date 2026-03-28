package com.example.testapplication.models

import com.google.gson.annotations.SerializedName

// Supabase GoTrue Auth Payload structures

data class SupabaseUserMetadata(
    val fullname: String,
    val role: String,
    val mobileNumber: String?
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val data: SupabaseUserMetadata // GoTrue saves extra info in 'user_metadata'
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

// AuthResponse can have a session (access_token) or just a user (when email confirm is on)
data class AuthResponse(
    val access_token: String?,
    val token_type: String?,
    val expires_in: Int?,
    val refresh_token: String?,
    val user: SupabaseUser?
)

// Separate signup response — Supabase returns just the user object at the top level when no session
data class SignupResponse(
    val id: String?,
    val email: String?,
    val user_metadata: SupabaseUserMetadata?,
    // If auto-confirm is on, these will be present
    val access_token: String?,
    val user: SupabaseUser?
)

data class UpdateProfileRequest(
    val data: SupabaseUserMetadata
)

data class ChangePasswordRequest(
    val password: String
)

// Model matching the Spring Boot backend User entity (public.users table)
data class BackendUser(
    val id: String,
    val email: String,
    val fullname: String,
    val role: String,
    val mobileNumber: String?
)
