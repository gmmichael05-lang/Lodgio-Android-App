package com.example.testapplication.features.booking.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.booking.CheckoutContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutPresenter(
    private var view: CheckoutContract.View?,
    private val session: SessionManager
) : CheckoutContract.Presenter {

    override fun loadListing(listingId: String) {
        view?.showLoading()
        RetrofitClient.listingApi.getListingById(listingId).enqueue(object : Callback<ListingDTO> {
            override fun onResponse(call: Call<ListingDTO>, response: Response<ListingDTO>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    view?.showListing(response.body()!!)
                } else {
                    view?.showError("Failed to load listing")
                }
            }
            override fun onFailure(call: Call<ListingDTO>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error")
            }
        })
    }

    override fun loadSavedCards() {
        val email = session.getEmail() ?: return
        RetrofitClient.userApi.getUserByEmail(email).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    val cardsStr = user.get("savedCards")?.asString ?: ""
                    if (cardsStr.isNotBlank()) {
                        try {
                            val cardsArray = JsonParser().parse(cardsStr).asJsonArray
                            val cards = mutableListOf<Map<String, String>>()
                            for (i in 0 until cardsArray.size()) {
                                val obj = cardsArray.get(i).asJsonObject
                                val card = mutableMapOf<String, String>()
                                card["label"] = obj.get("label")?.asString ?: ""
                                card["number"] = obj.get("number")?.asString ?: ""
                                card["expiry"] = obj.get("expiry")?.asString ?: ""
                                card["brand"] = obj.get("brand")?.asString ?: ""
                                cards.add(card)
                            }
                            view?.showSavedCards(cards)
                        } catch (_: Exception) { }
                    }
                }
            }
            override fun onFailure(call: Call<JsonObject>, t: Throwable) { /* silent */ }
        })
    }

    override fun confirmPayment(
        listingId: String, checkIn: String, checkOut: String,
        messageToHost: String, paymentMethod: String, totalPrice: Double
    ) {
        val userId = session.getUserId() ?: return

        view?.showLoading()

        val payload = JsonObject().apply {
            val guestObj = JsonObject().apply { addProperty("id", userId) }
            val listingObj = JsonObject().apply { addProperty("id", listingId) }
            add("guest", guestObj)
            add("listing", listingObj)
            addProperty("checkInDate", checkIn)
            addProperty("checkOutDate", checkOut)
            addProperty("totalPrice", totalPrice)
            addProperty("messageToHost", messageToHost)
            addProperty("paymentMethod", paymentMethod)
            addProperty("status", "PENDING")
        }

        RetrofitClient.bookingApi.createBooking(payload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onPaymentSuccess()
                } else {
                    val errorBody = try { response.errorBody()?.string() ?: "" } catch (e: Exception) { "" }
                    if (response.code() == 400 || errorBody.contains("conflict", ignoreCase = true)) {
                        view?.showError("These dates are already booked. Please choose different dates.")
                    } else {
                        view?.showError("Payment failed. Please try again.")
                    }
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
