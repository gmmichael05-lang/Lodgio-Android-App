package com.example.testapplication.features.listing

import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.model.ListingDTO

interface GuestDashboardContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showListings(listings: List<ListingDTO>)
        fun showTrips(trips: List<BookingDTO>)
        fun showError(message: String)
        fun showEmpty()
    }
    interface Presenter {
        fun loadListings(search: String? = null, city: String? = null, type: String? = null, maxPrice: Int? = null, guests: Int? = null, amenities: String? = null)
        fun loadMyTrips()
        fun onDestroy()
    }
}

interface HostDashboardContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showListings(listings: List<ListingDTO>)
        fun showBookingRequests(requests: List<BookingDTO>)
        fun showError(message: String)
        fun onListingDeleted()
        fun onBookingStatusUpdated()
    }
    interface Presenter {
        fun loadHostData()
        fun deleteListing(id: String)
        fun updateBookingStatus(id: String, status: String)
        fun onDestroy()
    }
}

interface ListingDetailContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showListing(listing: ListingDTO)
        fun showError(message: String)
    }
    interface Presenter {
        fun loadListing(id: String)
        fun onDestroy()
    }
}

interface CreateListingContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showError(message: String)
        fun onListingCreated()
    }
    interface Presenter {
        fun createListing(
            title: String, city: String, type: String, description: String,
            pricePerNight: String, guestCapacity: String, beds: String, baths: String,
            imageUrls: String, amenities: String
        )
        fun onDestroy()
    }
}
