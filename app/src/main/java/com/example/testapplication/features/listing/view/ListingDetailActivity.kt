package com.example.testapplication.features.listing.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.view.ConfirmBookingActivity
import com.example.testapplication.features.listing.ListingDetailContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.presenter.ListingDetailPresenter

class ListingDetailActivity : AppCompatActivity(), ListingDetailContract.View {

    private var presenter: ListingDetailContract.Presenter? = null
    private var listingId: String = ""
    private var currentListing: ListingDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        presenter = ListingDetailPresenter(this)
        listingId = intent.getStringExtra("LISTING_ID") ?: ""

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Check availability button
        findViewById<Button>(R.id.btnCheckAvailability).setOnClickListener {
            val checkIn = findViewById<EditText>(R.id.etCheckIn).textString()
            val checkOut = findViewById<EditText>(R.id.etCheckOut).textString()
            val guests = findViewById<EditText>(R.id.etGuests).textString()

            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                toast("Please select check-in and check-out dates.")
                return@setOnClickListener
            }

            val intent = Intent(this, ConfirmBookingActivity::class.java).apply {
                putExtra("LISTING_ID", listingId)
                putExtra("CHECK_IN", checkIn)
                putExtra("CHECK_OUT", checkOut)
                putExtra("GUESTS", guests)
            }
            startActivity(intent)
        }

        presenter?.loadListing(listingId)
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() { findViewById<ProgressBar>(R.id.progressBar).gone() }

    override fun showListing(listing: ListingDTO) {
        currentListing = listing
        findViewById<ScrollView>(R.id.scrollContent).visible()

        findViewById<TextView>(R.id.tvTitle).text = listing.title
        findViewById<TextView>(R.id.tvCity).text = "${listing.city ?: ""} ${if (!listing.location.isNullOrBlank()) "• ${listing.location}" else ""}"
        findViewById<TextView>(R.id.tvType).text = listing.type ?: "Property"
        findViewById<TextView>(R.id.tvHostName).text = "Hosted by ${listing.host?.fullname ?: "Unknown"}"
        findViewById<TextView>(R.id.tvDescription).text = listing.description ?: "A wonderful place to stay."
        findViewById<TextView>(R.id.tvPrice).text = "₱${String.format("%,.0f", listing.pricePerNight ?: 0.0)}"

        val details = mutableListOf<String>()
        listing.guestCapacity?.let { details.add("$it guests") }
        listing.beds?.let { details.add("$it bed${if (it != 1) "s" else ""}") }
        listing.baths?.let { details.add("$it bath${if (it != 1) "s" else ""}") }
        findViewById<TextView>(R.id.tvDetails).text = details.joinToString(" · ")

        // Amenities
        val llAmenities = findViewById<LinearLayout>(R.id.llAmenities)
        llAmenities.removeAllViews()
        val amenitiesList = listing.amenities?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        if (amenitiesList.isNotEmpty()) {
            amenitiesList.forEach { amenity ->
                val tv = TextView(this).apply {
                    text = "✓ $amenity"
                    textSize = 14f
                    setPadding(0, 8, 0, 8)
                }
                llAmenities.addView(tv)
            }
        } else {
            val tv = TextView(this).apply { text = "No specific amenities listed."; textSize = 13f }
            llAmenities.addView(tv)
        }

        // Host initial
        val initial = listing.host?.fullname?.firstOrNull()?.uppercase() ?: "?"
        findViewById<TextView>(R.id.tvHostInitial).text = initial
    }

    override fun showError(message: String) {
        toast(message)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
