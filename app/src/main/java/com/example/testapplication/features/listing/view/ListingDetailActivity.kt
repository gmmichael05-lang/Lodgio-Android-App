package com.example.testapplication.features.listing.view

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.model.BookedDateRange
import com.example.testapplication.features.booking.view.ConfirmBookingActivity
import com.example.testapplication.features.listing.ListingDetailContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.presenter.ListingDetailPresenter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ListingDetailActivity : AppCompatActivity(), ListingDetailContract.View {

    private var presenter: ListingDetailContract.Presenter? = null
    private var listingId: String = ""
    private var currentListing: ListingDTO? = null
    private var bookedDates: List<BookedDateRange> = emptyList()
    private var selectedCheckIn: String = ""
    private var selectedCheckOut: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        presenter = ListingDetailPresenter(this)
        listingId = intent.getStringExtra("LISTING_ID") ?: ""

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Date pickers with validation
        findViewById<EditText>(R.id.etCheckIn).apply {
            isFocusable = false
            setOnClickListener { showDatePicker(true) }
        }
        findViewById<EditText>(R.id.etCheckOut).apply {
            isFocusable = false
            setOnClickListener { showDatePicker(false) }
        }

        // Check availability button
        findViewById<Button>(R.id.btnCheckAvailability).setOnClickListener {
            val checkIn = selectedCheckIn
            val checkOut = selectedCheckOut
            val guests = findViewById<EditText>(R.id.etGuests).textString()

            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                toast("Please select check-in and check-out dates.")
                return@setOnClickListener
            }

            // Past date check
            val today = LocalDate.now()
            val ciDate = LocalDate.parse(checkIn, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val coDate = LocalDate.parse(checkOut, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

            if (ciDate.isBefore(today)) {
                toast("Check-in date cannot be in the past.")
                return@setOnClickListener
            }
            if (!coDate.isAfter(ciDate)) {
                toast("Check-out must be after check-in.")
                return@setOnClickListener
            }

            // Calendar blocking check
            if (hasDateConflict(checkIn, checkOut)) {
                toast("These dates are already booked. Please select different dates.")
                return@setOnClickListener
            }

            // Guest capacity check
            val guestCount = guests.toIntOrNull() ?: 1
            val maxGuests = currentListing?.guestCapacity ?: 99
            if (guestCount > maxGuests) {
                toast("Maximum $maxGuests guests allowed.")
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
        presenter?.loadBookedDates(listingId)
        presenter?.loadReviews(listingId)
    }

    private fun showDatePicker(isCheckIn: Boolean) {
        val today = java.util.Calendar.getInstance()
        val dialog = DatePickerDialog(this, { _, year, month, day ->
            val date = String.format("%04d-%02d-%02d", year, month + 1, day)
            if (isCheckIn) {
                selectedCheckIn = date
                findViewById<EditText>(R.id.etCheckIn).setText(date)
            } else {
                selectedCheckOut = date
                findViewById<EditText>(R.id.etCheckOut).setText(date)
            }
            updatePriceSummary()
        }, today.get(java.util.Calendar.YEAR), today.get(java.util.Calendar.MONTH), today.get(java.util.Calendar.DAY_OF_MONTH))
        dialog.datePicker.minDate = System.currentTimeMillis() - 1000
        dialog.show()
    }

    private fun updatePriceSummary() {
        if (selectedCheckIn.isNotEmpty() && selectedCheckOut.isNotEmpty() && currentListing != null) {
            try {
                val ci = LocalDate.parse(selectedCheckIn)
                val co = LocalDate.parse(selectedCheckOut)
                val nights = java.time.temporal.ChronoUnit.DAYS.between(ci, co).coerceAtLeast(1)
                val total = (currentListing!!.pricePerNight ?: 0.0) * nights
                findViewById<TextView>(R.id.tvPriceSummary)?.apply {
                    visible()
                    text = "$nights night(s) · ₱${String.format("%,.0f", total)}"
                }

                // Show conflict warning if dates overlap
                if (hasDateConflict(selectedCheckIn, selectedCheckOut)) {
                    findViewById<TextView>(R.id.tvDateConflict)?.apply {
                        visible()
                        text = "⚠️ Selected dates conflict with existing bookings"
                    }
                } else {
                    findViewById<TextView>(R.id.tvDateConflict)?.gone()
                }
            } catch (_: Exception) { }
        }
    }

    private fun hasDateConflict(checkIn: String, checkOut: String): Boolean {
        val ci = LocalDate.parse(checkIn)
        val co = LocalDate.parse(checkOut)
        for (bd in bookedDates) {
            val bci = LocalDate.parse(bd.checkIn)
            val bco = LocalDate.parse(bd.checkOut)
            if (ci.isBefore(bco) && co.isAfter(bci)) return true
        }
        return false
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

        // Load listing images with Glide
        val images = listing.imageUrls?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        val ivMainImage = findViewById<ImageView>(R.id.ivMainImage)
        if (ivMainImage != null && images.isNotEmpty()) {
            Glide.with(this)
                .load(images[0])
                .transform(CenterCrop(), RoundedCorners(32))
                .placeholder(R.drawable.bg_listing_placeholder)
                .error(R.drawable.bg_listing_placeholder)
                .into(ivMainImage)
            ivMainImage.visible()
            findViewById<TextView>(R.id.tvNoImage)?.gone()
        }

        // Load secondary images if they exist
        val ivImage2 = findViewById<ImageView>(R.id.ivImage2)
        if (ivImage2 != null && images.size > 1) {
            Glide.with(this)
                .load(images[1])
                .transform(CenterCrop(), RoundedCorners(24))
                .placeholder(R.drawable.bg_listing_placeholder)
                .into(ivImage2)
            ivImage2.visible()
        }

        val ivImage3 = findViewById<ImageView>(R.id.ivImage3)
        if (ivImage3 != null && images.size > 2) {
            Glide.with(this)
                .load(images[2])
                .transform(CenterCrop(), RoundedCorners(24))
                .placeholder(R.drawable.bg_listing_placeholder)
                .into(ivImage3)
            ivImage3.visible()
        }

        // Load host profile picture
        val ivHostAvatar = findViewById<ImageView>(R.id.ivHostAvatar)
        if (ivHostAvatar != null && !listing.host?.profilePictureUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(listing.host?.profilePictureUrl)
                .circleCrop()
                .into(ivHostAvatar)
            ivHostAvatar.visible()
            findViewById<TextView>(R.id.tvHostInitial)?.gone()
        } else {
            // Host initial
            val initial = listing.host?.fullname?.firstOrNull()?.uppercase() ?: "?"
            findViewById<TextView>(R.id.tvHostInitial).text = initial
        }

        // Amenities
        val llAmenities = findViewById<LinearLayout>(R.id.llAmenities)
        llAmenities.removeAllViews()
        val amenitiesList = listing.amenities?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        if (amenitiesList.isNotEmpty()) {
            amenitiesList.forEach { amenity ->
                val tv = TextView(this).apply {
                    text = "✓  $amenity"
                    textSize = 14f
                    setTextColor(getColor(R.color.lodgio_text_primary))
                    setPadding(0, 12, 0, 12)
                }
                llAmenities.addView(tv)
            }
        } else {
            val tv = TextView(this).apply {
                text = "No specific amenities listed."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
            }
            llAmenities.addView(tv)
        }

        // Set guest capacity hint
        findViewById<EditText>(R.id.etGuests).hint = "Guests (max ${listing.guestCapacity ?: "N/A"})"
    }

    override fun showBookedDates(dates: List<BookedDateRange>) {
        bookedDates = dates
        // Show unavailable dates section
        val llBookedDates = findViewById<LinearLayout>(R.id.llBookedDates)
        val tvBookedLabel = findViewById<TextView>(R.id.tvBookedDatesLabel)
        if (dates.isNotEmpty()) {
            tvBookedLabel?.visible()
            llBookedDates?.visible()
            llBookedDates?.removeAllViews()
            dates.forEach { bd ->
                val tv = TextView(this).apply {
                    text = "  ${bd.checkIn} → ${bd.checkOut}  (${if (bd.status == "ACCEPTED") "Confirmed" else "Pending"})"
                    textSize = 13f
                    setTextColor(getColor(R.color.lodgio_error))
                    setPadding(0, 8, 0, 8)
                }
                llBookedDates?.addView(tv)
            }
        } else {
            tvBookedLabel?.gone()
            llBookedDates?.gone()
        }
    }

    override fun showError(message: String) {
        toast(message)
        finish()
    }

    override fun showReviews(summary: com.google.gson.JsonObject, reviews: List<com.google.gson.JsonObject>) {
        val tvSummary = findViewById<TextView>(R.id.tvReviewSummary)
        val llReviews = findViewById<LinearLayout>(R.id.llReviews)

        val averageRating = summary.get("averageRating")?.asDouble ?: 0.0
        val totalReviews = summary.get("totalReviews")?.asInt ?: 0
        tvSummary.text = "★ ${String.format("%.1f", averageRating)} ($totalReviews reviews)"

        llReviews.removeAllViews()
        if (reviews.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No reviews yet."
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_hint))
            }
            llReviews.addView(tv)
            return
        }

        reviews.forEach { r ->
            val reviewerName = try { r.get("reviewerName")?.asString } catch (e: Exception) { "Guest" } ?: "Guest"
            val rating = try { r.get("rating")?.asInt } catch (e: Exception) { 5 } ?: 5
            val comment = try { r.get("comment")?.asString } catch (e: Exception) { "" } ?: ""
            val createdAt = try { r.get("createdAt")?.asString?.substring(0, 10) } catch (e: Exception) { "" } ?: ""

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }
            
            val headerRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            val tvName = TextView(this).apply {
                text = reviewerName
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(getColor(R.color.lodgio_text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvRating = TextView(this).apply {
                text = "★ $rating"
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            headerRow.addView(tvName)
            headerRow.addView(tvRating)

            val tvDate = TextView(this).apply {
                text = createdAt
                textSize = 11f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 4, 0, 8)
            }

            val tvComment = TextView(this).apply {
                text = comment
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_secondary))
            }

            row.addView(headerRow)
            row.addView(tvDate)
            row.addView(tvComment)

            val divider = android.view.View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(getColor(R.color.lodgio_divider))
                setPadding(0, 8, 0, 8)
            }

            llReviews.addView(row)
            llReviews.addView(divider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
