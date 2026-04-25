package com.example.testapplication.features.listing.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.HostDashboardContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.presenter.HostDashboardPresenter
import com.example.testapplication.features.profile.view.ProfileActivity

class HostDashboardActivity : AppCompatActivity(), HostDashboardContract.View {

    private var presenter: HostDashboardContract.Presenter? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var llBookingRequests: LinearLayout
    private lateinit var llMyListings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_dashboard)

        val session = LodgioApp.instance.sessionManager
        presenter = HostDashboardPresenter(this, session)

        progressBar = findViewById(R.id.progressBar)
        llBookingRequests = findViewById(R.id.llBookingRequests)
        llMyListings = findViewById(R.id.llMyListings)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeName)
        tvWelcome.text = "Welcome back, ${session.getFullname() ?: "Host"}!"

        findViewById<Button>(R.id.btnCreateListing).setOnClickListener {
            startActivity<CreateListingActivity>()
        }

        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity<ProfileActivity>()
        }

        presenter?.loadHostData()
    }

    override fun showLoading() { progressBar.visible() }
    override fun hideLoading() { progressBar.gone() }

    override fun showListings(listings: List<ListingDTO>) {
        llMyListings.removeAllViews()
        if (listings.isEmpty()) {
            val tv = TextView(this).apply { text = "No listings yet."; setPadding(16, 16, 16, 16) }
            llMyListings.addView(tv)
            return
        }
        listings.forEach { lst ->
            val v = layoutInflater.inflate(R.layout.item_host_listing, llMyListings, false)
            v.findViewById<TextView>(R.id.tvListingTitle).text = lst.title
            v.findViewById<TextView>(R.id.tvListingType).text = lst.type ?: "Property"
            v.findViewById<TextView>(R.id.tvListingPrice).text = "₱${String.format("%,.0f", lst.pricePerNight ?: 0.0)}/night"

            v.findViewById<ImageView>(R.id.btnDeleteListing).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Listing")
                    .setMessage("Delete \"${lst.title}\"?")
                    .setPositiveButton("Delete") { _, _ -> presenter?.deleteListing(lst.id ?: "") }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            llMyListings.addView(v)
        }
    }

    override fun showBookingRequests(requests: List<BookingDTO>) {
        llBookingRequests.removeAllViews()
        if (requests.isEmpty()) {
            val tv = TextView(this).apply { text = "No pending booking requests."; setPadding(16, 16, 16, 16) }
            llBookingRequests.addView(tv)
            return
        }
        requests.forEach { br ->
            val v = layoutInflater.inflate(R.layout.item_booking_request, llBookingRequests, false)
            v.findViewById<TextView>(R.id.tvBookingTitle).text = br.listing?.title ?: ""
            v.findViewById<TextView>(R.id.tvBookingGuest).text = "Guest: ${br.guest?.fullname ?: "Unknown"}"
            v.findViewById<TextView>(R.id.tvBookingDates).text = "${br.checkInDate} → ${br.checkOutDate}"
            v.findViewById<TextView>(R.id.tvBookingPrice).text = "₱${String.format("%,.0f", br.totalPrice ?: 0.0)}"
            v.findViewById<TextView>(R.id.tvBookingStatus).text = br.status ?: "PENDING"

            if (br.status == "PENDING") {
                v.findViewById<Button>(R.id.btnAccept).apply {
                    visible()
                    setOnClickListener { presenter?.updateBookingStatus(br.id ?: "", "ACCEPTED") }
                }
                v.findViewById<Button>(R.id.btnReject).apply {
                    visible()
                    setOnClickListener { presenter?.updateBookingStatus(br.id ?: "", "REJECTED") }
                }
            } else {
                v.findViewById<Button>(R.id.btnAccept).gone()
                v.findViewById<Button>(R.id.btnReject).gone()
            }

            if (!br.messageToHost.isNullOrBlank()) {
                v.findViewById<TextView>(R.id.tvBookingMessage).apply {
                    visible()
                    text = "\"${br.messageToHost}\""
                }
            }
            llBookingRequests.addView(v)
        }
    }

    override fun showError(message: String) { toast(message) }
    override fun onListingDeleted() { toast("Listing deleted"); presenter?.loadHostData() }
    override fun onBookingStatusUpdated() { toast("Booking updated"); presenter?.loadHostData() }

    override fun onResume() {
        super.onResume()
        presenter?.loadHostData()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
