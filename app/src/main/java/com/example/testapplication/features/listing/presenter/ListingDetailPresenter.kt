package com.example.testapplication.features.listing.presenter

import com.example.testapplication.LodgioApp
import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.booking.model.BookedDateRange
import com.example.testapplication.features.listing.ListingDetailContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListingDetailPresenter(
    private var view: ListingDetailContract.View?
) : ListingDetailContract.Presenter {

    private var isFavorited = false
    private val session = LodgioApp.instance.sessionManager

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
        RetrofitClient.listingApi.getReviewSummary(listingId).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val summary = response.body()!!
                    RetrofitClient.listingApi.getReviewsForListing(listingId).enqueue(object : Callback<List<JsonObject>> {
                        override fun onResponse(call: Call<List<JsonObject>>, response2: Response<List<JsonObject>>) {
                            if (response2.isSuccessful && response2.body() != null) {
                                view?.showReviews(summary, response2.body()!!)
                            }
                        }
                        override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) { }
                    })
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) { }
        })
    }

    override fun checkFavorite(listingId: String) {
        val email = session.getEmail() ?: return
        RetrofitClient.listingApi.isFavorited(email, listingId).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    isFavorited = response.body()!!.get("favorited")?.asBoolean ?: false
                    view?.showFavoriteStatus(isFavorited)
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) { }
        })
    }

    override fun toggleFavorite(listingId: String) {
        val email = session.getEmail() ?: return
        if (isFavorited) {
            RetrofitClient.listingApi.removeFavorite(email, listingId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isFavorited = false
                    view?.showFavoriteStatus(false)
                }
                override fun onFailure(call: Call<Void>, t: Throwable) { }
            })
        } else {
            RetrofitClient.listingApi.addFavorite(email, listingId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    isFavorited = true
                    view?.showFavoriteStatus(true)
                }
                override fun onFailure(call: Call<Void>, t: Throwable) { }
            })
        }
    }

    override fun onDestroy() { view = null }
}

