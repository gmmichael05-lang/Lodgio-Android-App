package com.example.testapplication.features.admin.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.admin.AdminContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminPresenter(
    private var view: AdminContract.View?
) : AdminContract.Presenter {

    override fun loadAdminData() {
        view?.showLoading()
        RetrofitClient.adminApi.getAllUsers().enqueue(object : Callback<List<JsonObject>> {
            override fun onResponse(call: Call<List<JsonObject>>, response: Response<List<JsonObject>>) {
                if (response.isSuccessful) view?.showUsers(response.body() ?: emptyList())
            }
            override fun onFailure(call: Call<List<JsonObject>>, t: Throwable) {
                view?.showError("Failed to load users")
            }
        })

        RetrofitClient.adminApi.getAllListings().enqueue(object : Callback<List<ListingDTO>> {
            override fun onResponse(call: Call<List<ListingDTO>>, response: Response<List<ListingDTO>>) {
                view?.hideLoading()
                if (response.isSuccessful) view?.showListings(response.body() ?: emptyList())
            }
            override fun onFailure(call: Call<List<ListingDTO>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Failed to load listings")
            }
        })
    }

    override fun deleteUser(id: String) {
        RetrofitClient.adminApi.deleteUser(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) { view?.onUserDeleted() }
            override fun onFailure(call: Call<Void>, t: Throwable) { view?.showError("Failed to delete user") }
        })
    }

    override fun deleteListing(id: String) {
        RetrofitClient.adminApi.deleteListing(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) { view?.onListingDeleted() }
            override fun onFailure(call: Call<Void>, t: Throwable) { view?.showError("Failed to delete listing") }
        })
    }

    override fun onDestroy() { view = null }
}
