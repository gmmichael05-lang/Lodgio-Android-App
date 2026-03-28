package com.example.testapplication.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("APP", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("TOKEN", token).apply()
    }

    fun saveRole(role: String) {
        prefs.edit().putString("ROLE", role).apply()
    }

    fun getToken(): String? {
        return prefs.getString("TOKEN", null)
    }

    fun getRole(): String? {
        return prefs.getString("ROLE", "GUEST")
    }

    fun saveUserId(id: String) {
        prefs.edit().putString("USER_ID", id).apply()
    }

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
