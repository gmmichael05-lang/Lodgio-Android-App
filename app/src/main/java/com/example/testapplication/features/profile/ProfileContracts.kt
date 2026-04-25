package com.example.testapplication.features.profile

import com.google.gson.JsonObject

interface ProfileContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showProfile(user: JsonObject)
        fun showError(message: String)
        fun onLogout()
    }
    interface Presenter {
        fun loadProfile()
        fun logout()
        fun onDestroy()
    }
}

interface ChangePasswordContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onPasswordChanged()
    }
    interface Presenter {
        fun changePassword(newPassword: String, confirmPassword: String)
        fun onDestroy()
    }
}
