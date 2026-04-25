package com.example.testapplication.features.auth

/**
 * MVP Contract for Login feature.
 */
interface LoginContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onLoginSuccess(role: String)
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun onDestroy()
    }
}
