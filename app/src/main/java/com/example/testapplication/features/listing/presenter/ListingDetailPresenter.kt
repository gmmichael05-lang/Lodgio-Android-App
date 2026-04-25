package com.example.testapplication.features.listing.presenter

import com.example.testapplication.core.network.RetrofitClient
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

    override fun onDestroy() { view = null }
}
