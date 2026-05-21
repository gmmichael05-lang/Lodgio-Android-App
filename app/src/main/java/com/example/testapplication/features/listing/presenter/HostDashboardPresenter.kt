package com.example.testapplication.features.listing.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.HostDashboardContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HostDashboardPresenter(
    private var view: HostDashboardContract.View?,
    private val session: SessionManager
) : HostDashboardContract.Presenter {

    override fun loadHostData() {
        val email = session.getEmail() ?: return
        view?.showLoading()

        // Load host's listings
        RetrofitClient.listingApi.getListingsByHostEmail(email).enqueue(object : Callback<List<ListingDTO>> {
            override fun onResponse(call: Call<List<ListingDTO>>, response: Response<List<ListingDTO>>) {
                val listings = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                view?.showListings(listings)

                // After listings loaded, load bookings (needs listings for revenue analytics)
                RetrofitClient.bookingApi.getBookingsByHostEmail(email).enqueue(object : Callback<List<BookingDTO>> {
                    override fun onResponse(call: Call<List<BookingDTO>>, response: Response<List<BookingDTO>>) {
                        view?.hideLoading()
                        if (response.isSuccessful) {
                            val all = response.body() ?: emptyList()
                            val active = all.filter { it.status == "PENDING" || it.status == "ACCEPTED" }
                            view?.showBookingRequests(active)
                            // Pass all bookings + listings for revenue analytics (matches web app pattern)
                            view?.showRevenueAnalytics(all, listings)
                        }
                    }
                    override fun onFailure(call: Call<List<BookingDTO>>, t: Throwable) {
                        view?.hideLoading()
                    }
                })
            }
            override fun onFailure(call: Call<List<ListingDTO>>, t: Throwable) {
                view?.showError("Failed to load listings")
            }
        })
    }

    override fun deleteListing(id: String) {
        RetrofitClient.listingApi.deleteListing(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                view?.onListingDeleted()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                view?.showError("Failed to delete listing")
            }
        })
    }

    override fun toggleListingActive(id: String) {
        RetrofitClient.listingApi.toggleListingActive(id).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.onListingToggled()
                loadHostData() // Reload data to sync UI — matches web app behavior
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.showError("Failed to toggle listing status")
            }
        })
    }

    override fun updateBookingStatus(id: String, status: String) {
        val payload = JsonObject().apply { addProperty("status", status) }
        RetrofitClient.bookingApi.updateBookingStatus(id, payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.onBookingStatusUpdated()
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.showError("Failed to update booking")
            }
        })
    }

    override fun onDestroy() { view = null }
}
