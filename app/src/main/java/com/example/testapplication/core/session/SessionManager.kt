package com.example.testapplication.core.session

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences-based session manager.
 * Stores auth token, user profile info, and role after login.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("LODGIO_SESSION", Context.MODE_PRIVATE)

    // ── Token ──
    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    // ── Role ──
    fun saveRole(role: String) = prefs.edit().putString(KEY_ROLE, role).apply()
    fun getRole(): String = prefs.getString(KEY_ROLE, "GUEST") ?: "GUEST"

    // ── User ID ──
    fun saveUserId(id: String) = prefs.edit().putString(KEY_USER_ID, id).apply()
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    // ── Email ──
    fun saveEmail(email: String) = prefs.edit().putString(KEY_EMAIL, email).apply()
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    // ── Full Name ──
    fun saveFullname(name: String) = prefs.edit().putString(KEY_FULLNAME, name).apply()
    fun getFullname(): String? = prefs.getString(KEY_FULLNAME, null)

    // ── Is Logged In ──
    fun isLoggedIn(): Boolean = getToken() != null

    // ── Clear Session ──
    fun clear() = prefs.edit().clear().apply()

    companion object {
        private const val KEY_TOKEN = "TOKEN"
        private const val KEY_ROLE = "ROLE"
        private const val KEY_USER_ID = "USER_ID"
        private const val KEY_EMAIL = "EMAIL"
        private const val KEY_FULLNAME = "FULLNAME"
    }
}
