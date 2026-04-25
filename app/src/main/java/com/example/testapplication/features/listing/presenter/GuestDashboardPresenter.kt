package com.example.testapplication.features.listing.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.GuestDashboardContract
import com.example.testapplication.features.listing.model.ListingDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuestDashboardPresenter(
    private var view: GuestDashboardContract.View?,
    private val session: SessionManager
) : GuestDashboardContract.Presenter {

    override fun loadListings(search: String?, city: String?, type: String?, maxPrice: Int?, guests: Int?, amenities: String?) {
        view?.showLoading()

        val call = if (!search.isNullOrBlank()) {
            RetrofitClient.listingApi.getActiveListings(search)
        } else {
            RetrofitClient.listingApi.searchListings(
                city = if (city.isNullOrBlank()) null else city,
                type = if (type.isNullOrBlank() || type == "All Types") null else type,
                maxPrice = maxPrice,
                guests = if (guests != null && guests > 1) guests else null,
                amenities = if (amenities.isNullOrBlank()) null else amenities
            )
        }

        call.enqueue(object : Callback<List<ListingDTO>> {
            override fun onResponse(call: Call<List<ListingDTO>>, response: Response<List<ListingDTO>>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val listings = response.body()!!
                    if (listings.isEmpty()) view?.showEmpty()
                    else view?.showListings(listings)
                } else {
                    view?.showError("Failed to load listings")
                }
            }
            override fun onFailure(call: Call<List<ListingDTO>>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    override fun loadMyTrips() {
        val email = session.getEmail() ?: return
        RetrofitClient.bookingApi.getBookingsByGuestEmail(email).enqueue(object : Callback<List<BookingDTO>> {
            override fun onResponse(call: Call<List<BookingDTO>>, response: Response<List<BookingDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    view?.showTrips(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<BookingDTO>>, t: Throwable) { /* silent */ }
        })
    }

    override fun onDestroy() { view = null }
}
