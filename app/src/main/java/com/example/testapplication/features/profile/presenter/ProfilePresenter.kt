package com.example.testapplication.features.profile.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.profile.ProfileContract
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfilePresenter(
    private var view: ProfileContract.View?,
    private val session: SessionManager
) : ProfileContract.Presenter {

    override fun loadProfile() {
        val email = session.getEmail() ?: run {
            view?.showError("Session expired")
            view?.onLogout()
            return
        }

        view?.showLoading()
        RetrofitClient.userApi.getUserByEmail(email).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    view?.showProfile(response.body()!!)
                } else {
                    view?.showError("Failed to load profile")
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun logout() {
        session.clear()
        view?.onLogout()
    }

    override fun onDestroy() { view = null }
}
