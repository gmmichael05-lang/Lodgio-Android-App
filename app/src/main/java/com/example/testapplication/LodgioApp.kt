package com.example.testapplication

import android.app.Application
import android.util.Log
import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager

/**
 * Custom Application class for the Lodgio Android App.
 * Initializes global singletons (RetrofitClient, SessionManager) at app startup.
 */
class LodgioApp : Application() {

    lateinit var sessionManager: SessionManager
        private set

    override fun onCreate() {
        super.onCreate()
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
        sessionManager = SessionManager(this)
        try {
            RetrofitClient.init()
        } catch (e: Exception) {
            Log.e("LodgioApp", "RetrofitClient init failed", e)
        }
    }

    companion object {
        lateinit var instance: LodgioApp
            private set
    }
}

