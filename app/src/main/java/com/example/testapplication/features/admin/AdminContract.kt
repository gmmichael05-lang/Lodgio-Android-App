package com.example.testapplication.features.admin

import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject

interface AdminContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showUsers(users: List<JsonObject>)
        fun showListings(listings: List<ListingDTO>)
        fun showError(message: String)
        fun onUserDeleted()
        fun onListingDeleted()
    }
    interface Presenter {
        fun loadAdminData()
        fun deleteUser(id: String)
        fun deleteListing(id: String)
        fun onDestroy()
    }
}
