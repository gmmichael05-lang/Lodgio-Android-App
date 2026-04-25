package com.example.testapplication.features.booking.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.booking.ConfirmBookingContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfirmBookingPresenter(
    private var view: ConfirmBookingContract.View?,
    private val session: SessionManager
) : ConfirmBookingContract.Presenter {

    override fun loadData(listingId: String) {
        view?.showLoading()
        RetrofitClient.listingApi.getListingById(listingId).enqueue(object : Callback<ListingDTO> {
            override fun onResponse(call: Call<ListingDTO>, response: Response<ListingDTO>) {
                if (response.isSuccessful && response.body() != null) {
                    view?.showListing(response.body()!!)
                }
            }
            override fun onFailure(call: Call<ListingDTO>, t: Throwable) {
                view?.showError("Failed to load listing")
            }
        })

        val email = session.getEmail() ?: return
        RetrofitClient.userApi.getUserByEmail(email).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val u = response.body()!!
                    view?.showUserInfo(
                        u.get("fullname")?.asString ?: "",
                        u.get("mobileNumber")?.asString ?: ""
                    )
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) { view?.hideLoading() }
        })
    }

    override fun onDestroy() { view = null }
}
