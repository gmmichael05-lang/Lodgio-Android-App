package com.example.testapplication.features.booking

import com.example.testapplication.features.listing.model.ListingDTO

interface ConfirmBookingContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showListing(listing: ListingDTO)
        fun showUserInfo(fullname: String, phone: String)
        fun showError(message: String)
    }
    interface Presenter {
        fun loadData(listingId: String)
        fun onDestroy()
    }
}

interface CheckoutContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showListing(listing: ListingDTO)
        fun showError(message: String)
        fun onPaymentSuccess()
    }
    interface Presenter {
        fun loadListing(listingId: String)
        fun confirmPayment(
            listingId: String, checkIn: String, checkOut: String,
            messageToHost: String, paymentMethod: String, totalPrice: Double
        )
        fun onDestroy()
    }
}
