package com.example.testapplication.features.profile.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.auth.model.ChangePasswordRequest
import com.example.testapplication.features.auth.model.SupabaseUser
import com.example.testapplication.features.profile.ChangePasswordContract
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordPresenter(
    private var view: ChangePasswordContract.View?,
    private val session: SessionManager
) : ChangePasswordContract.Presenter {

    override fun changePassword(newPassword: String, confirmPassword: String) {
        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            view?.showError("Please fill all fields.")
            return
        }
        if (newPassword.length < 6) {
            view?.showError("Password must be at least 6 characters.")
            return
        }
        if (newPassword != confirmPassword) {
            view?.showError("Passwords do not match.")
            return
        }

        val token = session.getToken() ?: return

        view?.showLoading()
        val request = ChangePasswordRequest(newPassword)
        RetrofitClient.authApi.changePassword(token = "Bearer $token", request = request)
            .enqueue(object : Callback<SupabaseUser> {
                override fun onResponse(call: Call<SupabaseUser>, response: Response<SupabaseUser>) {
                    view?.hideLoading()
                    if (response.isSuccessful) {
                        view?.onPasswordChanged()
                    } else {
                        view?.showError("Failed: ${response.message()}")
                    }
                }
                override fun onFailure(call: Call<SupabaseUser>, t: Throwable) {
                    view?.hideLoading()
                    view?.showError("Network error: ${t.message}")
                }
            })
    }

    override fun onDestroy() { view = null }
}
