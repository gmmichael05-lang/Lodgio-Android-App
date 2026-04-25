package com.example.testapplication.features.listing.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.listing.CreateListingContract
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateListingPresenter(
    private var view: CreateListingContract.View?,
    private val session: SessionManager
) : CreateListingContract.Presenter {

    override fun createListing(
        title: String, city: String, type: String, description: String,
        pricePerNight: String, guestCapacity: String, beds: String, baths: String,
        imageUrls: String, amenities: String
    ) {
        if (title.isEmpty() || city.isEmpty() || pricePerNight.isEmpty()) {
            view?.showError("Title, city, and price are required.")
            return
        }

        val userId = session.getUserId() ?: run {
            view?.showError("User session not found.")
            return
        }

        view?.showLoading()

        val payload = JsonObject().apply {
            addProperty("title", title)
            addProperty("description", description)
            addProperty("pricePerNight", pricePerNight.toDoubleOrNull() ?: 0.0)
            addProperty("guestCapacity", guestCapacity.toIntOrNull() ?: 2)
            addProperty("beds", beds.toIntOrNull() ?: 1)
            addProperty("baths", baths.toIntOrNull() ?: 1)
            addProperty("city", city)
            addProperty("type", type)
            addProperty("location", city)
            addProperty("imageUrls", imageUrls)
            addProperty("amenities", amenities)
            val hostObj = JsonObject().apply { addProperty("id", userId) }
            add("host", hostObj)
        }

        RetrofitClient.listingApi.createListing(payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onListingCreated()
                } else {
                    view?.showError("Failed to create listing. Please try again.")
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
