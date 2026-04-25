package com.example.testapplication.features.listing.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.GuestDashboardContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.presenter.GuestDashboardPresenter
import com.example.testapplication.features.profile.view.ProfileActivity

/**
 * Guest Dashboard Activity — browse listings, search, view my trips.
 * Matches web app's GuestDashboard.jsx functionality.
 */
class GuestDashboardActivity : AppCompatActivity(), GuestDashboardContract.View {

    private var presenter: GuestDashboardContract.Presenter? = null
    private lateinit var adapter: ListingAdapter
    private lateinit var rvListings: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvTripsSection: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_dashboard)

        val session = LodgioApp.instance.sessionManager
        presenter = GuestDashboardPresenter(this, session)

        // Views
        rvListings = findViewById(R.id.rvListings)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        etSearch = findViewById(R.id.etSearch)
        tvTripsSection = findViewById(R.id.llTripsSection)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeName)
        tvWelcome.text = "Welcome, ${session.getFullname() ?: "Guest"}!"

        // Setup RecyclerView
        adapter = ListingAdapter { listing ->
            val intent = Intent(this, ListingDetailActivity::class.java)
            intent.putExtra("LISTING_ID", listing.id)
            startActivity(intent)
        }
        rvListings.layoutManager = GridLayoutManager(this, 2)
        rvListings.adapter = adapter

        // Search
        val btnSearch = findViewById<ImageView>(R.id.btnSearch)
        btnSearch.setOnClickListener {
            presenter?.loadListings(search = etSearch.textString())
        }

        etSearch.setOnEditorActionListener { _, _, _ ->
            presenter?.loadListings(search = etSearch.textString())
            true
        }

        // Profile button
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity<ProfileActivity>()
        }

        // Load data
        presenter?.loadListings()
        presenter?.loadMyTrips()
    }

    override fun showLoading() {
        progressBar.visible()
        tvEmpty.gone()
    }

    override fun hideLoading() {
        progressBar.gone()
    }

    override fun showListings(listings: List<ListingDTO>) {
        tvEmpty.gone()
        rvListings.visible()
        adapter.updateData(listings)
    }

    override fun showTrips(trips: List<BookingDTO>) {
        if (trips.isEmpty()) {
            tvTripsSection.gone()
            return
        }
        tvTripsSection.visible()
        val llTrips = findViewById<LinearLayout>(R.id.llTripsContainer)
        llTrips.removeAllViews()

        trips.forEach { trip ->
            val tripView = layoutInflater.inflate(R.layout.item_trip_card, llTrips, false)
            tripView.findViewById<TextView>(R.id.tvTripTitle).text = trip.listing?.title ?: "—"
            tripView.findViewById<TextView>(R.id.tvTripCity).text = trip.listing?.city ?: ""
            tripView.findViewById<TextView>(R.id.tvTripDates).text = "${trip.checkInDate} → ${trip.checkOutDate}"
            tripView.findViewById<TextView>(R.id.tvTripPrice).text = "₱${String.format("%,.0f", trip.totalPrice ?: 0.0)}"
            tripView.findViewById<TextView>(R.id.tvTripStatus).text = trip.status ?: ""

            val statusView = tripView.findViewById<TextView>(R.id.tvTripStatus)
            when (trip.status) {
                "ACCEPTED" -> statusView.setTextColor(getColor(R.color.lodgio_success))
                "REJECTED" -> statusView.setTextColor(getColor(R.color.lodgio_error))
                else -> statusView.setTextColor(getColor(R.color.lodgio_host_badge))
            }

            tripView.setOnClickListener {
                val intent = Intent(this, ListingDetailActivity::class.java)
                intent.putExtra("LISTING_ID", trip.listing?.id)
                startActivity(intent)
            }
            llTrips.addView(tripView)
        }
    }

    override fun showEmpty() {
        tvEmpty.visible()
        rvListings.gone()
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun onResume() {
        super.onResume()
        presenter?.loadMyTrips()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
