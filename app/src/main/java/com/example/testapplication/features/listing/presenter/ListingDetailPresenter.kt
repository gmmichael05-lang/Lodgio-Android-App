package com.example.testapplication.features.listing.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.booking.model.BookedDateRange
import com.example.testapplication.features.listing.ListingDetailContract
import com.example.testapplication.features.listing.model.ListingDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListingDetailPresenter(
    private var view: ListingDetailContract.View?
) : ListingDetailContract.Presenter {

    override fun loadListing(id: String) {
        view?.showLoading()
        RetrofitClient.listingApi.getListingById(id).enqueue(object : Callback<ListingDTO> {
            override fun onResponse(call: Call<ListingDTO>, response: Response<ListingDTO>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    view?.showListing(response.body()!!)
                } else {
                    view?.showError("Listing not found")
                }
            }
            override fun onFailure(call: Call<ListingDTO>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun loadBookedDates(listingId: String) {
        RetrofitClient.bookingApi.getBookedDates(listingId).enqueue(object : Callback<List<BookedDateRange>> {
            override fun onResponse(call: Call<List<BookedDateRange>>, response: Response<List<BookedDateRange>>) {
                if (response.isSuccessful && response.body() != null) {
                    view?.showBookedDates(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<BookedDateRange>>, t: Throwable) {
                // Silent fail — calendar blocking is optional
            }
        })
    }

    override fun loadReviews(listingId: String) {
        RetrofitClient.listingApi.getReviewSummary(listingId).enqueue(object : Callback<com.google.gson.JsonObject> {
            override fun onResponse(call: Call<com.google.gson.JsonObject>, response: Response<com.google.gson.JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    RetrofitClient.listingApi.getReviewsForListing(listingId).enqueue(object : Callback<List<com.google.gson.JsonObject>> {
                        override fun onResponse(call: Call<List<com.google.gson.JsonObject>>, response2: Response<List<com.google.gson.JsonObject>>) {
                            if (response2.isSuccessful && response2.body() != null) {
                                view?.showReviews(summary, response2.body()!!)
                            }
                        }
                        override fun onFailure(call: Call<List<com.google.gson.JsonObject>>, t: Throwable) { }
                    })
                }
            }
            override fun onFailure(call: Call<com.google.gson.JsonObject>, t: Throwable) { }
        })
    }

    override fun onDestroy() { view = null }
}
