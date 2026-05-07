package com.example.testapplication.features.booking.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.ConfirmBookingContract
import com.example.testapplication.features.booking.presenter.ConfirmBookingPresenter
import com.example.testapplication.features.listing.model.ListingDTO
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ConfirmBookingActivity : AppCompatActivity(), ConfirmBookingContract.View {

    private var presenter: ConfirmBookingContract.Presenter? = null
    private var listingId = ""
    private var checkIn = ""
    private var checkOut = ""
    private var guests = ""
    private var currentListing: ListingDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_booking)

        val session = LodgioApp.instance.sessionManager
        presenter = ConfirmBookingPresenter(this, session)

        listingId = intent.getStringExtra("LISTING_ID") ?: ""
        checkIn = intent.getStringExtra("CHECK_IN") ?: ""
        checkOut = intent.getStringExtra("CHECK_OUT") ?: ""
        guests = intent.getStringExtra("GUESTS") ?: "1"

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvCheckIn).text = checkIn
        findViewById<TextView>(R.id.tvCheckOut).text = checkOut
        findViewById<TextView>(R.id.tvGuests).text = "$guests guest(s)"

        findViewById<Button>(R.id.btnProceedToPayment).setOnClickListener {
            // Validate dates
            if (checkIn.isEmpty() || checkOut.isEmpty()) {
                toast("Please select check-in and check-out dates.")
                return@setOnClickListener
            }
            try {
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
            } catch (e: Exception) {
                toast("Invalid date format.")
                return@setOnClickListener
            }

            // Validate guest capacity
            val guestCount = guests.toIntOrNull() ?: 1
            val maxGuests = currentListing?.guestCapacity ?: 99
            if (guestCount > maxGuests) {
                toast("Maximum $maxGuests guests allowed.")
                return@setOnClickListener
            }

            // Get selected contact
            val spContact = findViewById<Spinner>(R.id.spContactNumber)
            val selectedContact = spContact?.selectedItem?.toString() ?: ""

            val intent = Intent(this, CheckoutActivity::class.java).apply {
                putExtra("LISTING_ID", listingId)
                putExtra("CHECK_IN", checkIn)
                putExtra("CHECK_OUT", checkOut)
                putExtra("GUESTS", guests)
                putExtra("TOTAL_PRICE", calculateTotal())
                putExtra("MESSAGE", findViewById<EditText>(R.id.etMessage).textString())
                putExtra("CONTACT", selectedContact)
            }
            startActivity(intent)
        }

        presenter?.loadData(listingId)
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() { findViewById<ProgressBar>(R.id.progressBar).gone() }

    override fun showListing(listing: ListingDTO) {
        currentListing = listing
        findViewById<TextView>(R.id.tvListingTitle).text = listing.title
        findViewById<TextView>(R.id.tvListingCity).text = "${listing.city} · ${listing.type}"
        findViewById<TextView>(R.id.tvPricePerNight).text = "₱${String.format("%,.0f", listing.pricePerNight ?: 0.0)}/night"

        // Calculate total
        val total = calculateTotal()
        val nights = calculateNights()
        findViewById<TextView>(R.id.tvNights).text = "$nights night(s)"
        findViewById<TextView>(R.id.tvTotal).text = "₱${String.format("%,.0f", total)}"
    }

    override fun showUserInfo(fullname: String, phone: String) {
        findViewById<TextView>(R.id.tvGuestName).text = fullname
        findViewById<TextView>(R.id.tvGuestPhone).text = phone
    }

    override fun showContactNumbers(contacts: List<String>) {
        val spContact = findViewById<Spinner>(R.id.spContactNumber)
        if (spContact != null && contacts.isNotEmpty()) {
            spContact.visible()
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, contacts)
            spContact.adapter = adapter
        }
    }

    override fun showError(message: String) { toast(message) }

    private fun calculateNights(): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(checkIn, formatter)
            val end = LocalDate.parse(checkOut, formatter)
            ChronoUnit.DAYS.between(start, end).coerceAtLeast(1)
        } catch (e: Exception) { 1 }
    }

    private fun calculateTotal(): Double {
        val nights = calculateNights()
        return (currentListing?.pricePerNight ?: 0.0) * nights
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
