package com.example.testapplication.features.auth

/**
 * MVP Contract for Register feature.
 */
interface RegisterContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onRegisterSuccess()
    }

    interface Presenter {
        fun register(
            role: String,
            fullname: String,
            email: String,
            mobileNumber: String,
            password: String,
            confirmPassword: String
        )
        fun onDestroy()
    }
}
