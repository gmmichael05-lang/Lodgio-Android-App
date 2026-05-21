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

    override fun saveContacts(userId: String, contacts: String) {
        view?.showLoading()
        RetrofitClient.userApi.updateContactNumbers(userId, contacts).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onContactsSaved()
                } else {
                    view?.showError("Failed to save contacts")
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun saveCards(userId: String, cards: String) {
        view?.showLoading()
        RetrofitClient.userApi.updateSavedCards(userId, cards).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onCardsSaved()
                } else {
                    view?.showError("Failed to save cards")
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

    override fun submitReview(listingId: String, bookingId: String, rating: Int, comment: String) {
        val email = session.getEmail() ?: return

        // Payload must match backend ReviewController.createReview() expectations:
        // required: email, listingId, bookingId, rating, comment
        val payload = com.example.testapplication.features.listing.api.ReviewPayload(
            email = email,
            listingId = listingId,
            bookingId = bookingId,
            rating = rating,
            comment = comment
        )

        view?.showLoading()
        RetrofitClient.listingApi.submitReview(payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onReviewSubmitted()
                } else {
                    val code = response.code()
                    if (code == 400) {
                        view?.showError("You may have already reviewed this booking.")
                    } else {
                        view?.showError("Failed to submit review (Code $code)")
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun onDestroy() { view = null }
}
