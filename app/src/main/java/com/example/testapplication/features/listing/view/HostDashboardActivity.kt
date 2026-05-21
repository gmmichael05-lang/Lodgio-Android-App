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
import com.bumptech.glide.Glide

class HostDashboardActivity : AppCompatActivity(), HostDashboardContract.View {

    private var presenter: HostDashboardContract.Presenter? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var llBookingRequests: LinearLayout
    private lateinit var llMyListings: LinearLayout
    private lateinit var swipeRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            presenter?.loadHostData()
        }

        presenter?.loadHostData()
    }

    override fun showLoading() { progressBar.visible() }
    override fun hideLoading() {
        progressBar.gone()
        if (::swipeRefresh.isInitialized) {
            swipeRefresh.isRefreshing = false
        }
    }

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

            // Show PAUSED badge when listing is inactive (matches web app)
            val isActive = lst.isActive != false
            if (!isActive) {
                v.findViewById<TextView>(R.id.tvListingType).text = "PAUSED"
                v.findViewById<TextView>(R.id.tvListingType).setTextColor(getColor(R.color.lodgio_warning))
                v.alpha = 0.6f
            }

            val ivListingImage = v.findViewById<ImageView>(R.id.ivListingImage)
            val imageUrls = lst.imageUrls?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
            if (imageUrls.isNotEmpty()) {
                Glide.with(this)
                    .load(imageUrls[0])
                    .placeholder(R.drawable.ic_property)
                    .error(R.drawable.ic_property)
                    .centerCrop()
                    .into(ivListingImage)
            }

            val swListingActive = v.findViewById<Switch>(R.id.swListingActive)
            swListingActive.setOnCheckedChangeListener(null)
            swListingActive.isChecked = isActive
            swListingActive.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.isPressed) {
                    presenter?.toggleListingActive(lst.id ?: "")
                }
            }

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
    override fun onListingToggled() { /* Data auto-reloads via presenter */ }
    override fun onBookingStatusUpdated() { toast("Booking updated"); presenter?.loadHostData() }

    override fun showRevenueAnalytics(allBookings: List<BookingDTO>, listings: List<ListingDTO>) {
        val llRevenueSection = findViewById<LinearLayout>(R.id.llRevenueSection)
        val accepted = allBookings.filter { it.status == "ACCEPTED" }
        val totalRevenue = accepted.sumOf { it.totalPrice ?: 0.0 }
        val totalBookings = accepted.size
        val avgBooking = if (totalBookings > 0) Math.round(totalRevenue / totalBookings) else 0L
        val occupancyRate = if (listings.isNotEmpty()) Math.round((totalBookings.toDouble() / listings.size) * 100) else 0L

        llRevenueSection.visible()

        // Summary stats
        findViewById<TextView>(R.id.tvTotalRevenue).text = "₱${String.format("%,.0f", totalRevenue)}"
        findViewById<TextView>(R.id.tvTotalBookings).text = "From $totalBookings booking${if (totalBookings != 1) "s" else ""}"
        findViewById<TextView>(R.id.tvAvgBooking).text = "₱${String.format("%,d", avgBooking)}"
        findViewById<TextView>(R.id.tvOccupancy).text = "$occupancyRate% occupancy"

        // Per-listing revenue breakdown (matches web app's HostDashboard.jsx)
        val llBreakdown = findViewById<LinearLayout>(R.id.llRevenueBreakdown)
        llBreakdown.removeAllViews()

        if (totalBookings == 0) {
            val tvEmpty = TextView(this).apply {
                text = "📊 No revenue data yet.\nRevenue will appear here once you accept bookings."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 24, 0, 24)
                gravity = android.view.Gravity.CENTER
            }
            llBreakdown.addView(tvEmpty)
            return
        }

        // Build per-listing map
        val listingRevMap = mutableMapOf<String, Triple<String, String, Pair<Int, Double>>>()
        accepted.forEach { b ->
            val key = b.listing?.id ?: return@forEach
            val existing = listingRevMap[key]
            if (existing != null) {
                listingRevMap[key] = Triple(
                    existing.first,
                    existing.second,
                    Pair(existing.third.first + 1, existing.third.second + (b.totalPrice ?: 0.0))
                )
            } else {
                listingRevMap[key] = Triple(
                    b.listing.title ?: "Untitled",
                    b.listing.city ?: "",
                    Pair(1, b.totalPrice ?: 0.0)
                )
            }
        }

        // Sort by revenue descending
        val sortedListings = listingRevMap.values.sortedByDescending { it.third.second }

        // Header
        val tvHeader = TextView(this).apply {
            text = "REVENUE BY LISTING"
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(getColor(R.color.lodgio_text_primary))
            letterSpacing = 0.05f
            setPadding(0, 16, 0, 16)
        }
        llBreakdown.addView(tvHeader)

        sortedListings.forEach { (title, city, data) ->
            val (count, total) = data
            val pct = if (totalRevenue > 0) (total / totalRevenue) * 100 else 0.0

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 16, 16, 16)
                setBackgroundResource(R.drawable.card_background)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 12
                layoutParams = lp
            }

            // Title row
            val titleRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val tvTitle = TextView(this).apply {
                text = title
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.lodgio_text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvRevValue = TextView(this).apply {
                text = "₱${String.format("%,.0f", total)}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.lodgio_text_primary))
            }
            titleRow.addView(tvTitle)
            titleRow.addView(tvRevValue)

            // Detail row
            val detailRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            val tvDetail = TextView(this).apply {
                text = "$city · $count booking${if (count != 1) "s" else ""}"
                textSize = 12f
                setTextColor(getColor(R.color.lodgio_text_hint))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvPct = TextView(this).apply {
                text = "${String.format("%.1f", pct)}% of total"
                textSize = 11f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.lodgio_text_secondary))
            }
            detailRow.addView(tvDetail)
            detailRow.addView(tvPct)

            // Progress bar
            val progressBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                progress = pct.toInt().coerceIn(0, 100)
                val lp2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 8)
                lp2.topMargin = 12
                layoutParams = lp2
                progressDrawable = android.graphics.drawable.ClipDrawable(
                    android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = 99f
                        setColor(getColor(R.color.lodgio_primary))
                    },
                    android.view.Gravity.START,
                    android.graphics.drawable.ClipDrawable.HORIZONTAL
                )
                setBackgroundResource(R.drawable.input_background)
            }

            row.addView(titleRow)
            row.addView(detailRow)
            row.addView(progressBar)
            llBreakdown.addView(row)
        }
    }

    override fun onResume() {
        super.onResume()
        presenter?.loadHostData()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
