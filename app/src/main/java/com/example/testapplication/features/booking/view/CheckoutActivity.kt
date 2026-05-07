package com.example.testapplication.features.booking.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.booking.CheckoutContract
import com.example.testapplication.features.booking.presenter.CheckoutPresenter
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.view.GuestDashboardActivity

class CheckoutActivity : AppCompatActivity(), CheckoutContract.View {

    private var presenter: CheckoutContract.Presenter? = null

    private var listingId = ""
    private var checkIn = ""
    private var checkOut = ""
    private var totalPrice = 0.0
    private var message = ""
    private var savedCardLabels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val session = LodgioApp.instance.sessionManager
        presenter = CheckoutPresenter(this, session)

        listingId = intent.getStringExtra("LISTING_ID") ?: ""
        checkIn = intent.getStringExtra("CHECK_IN") ?: ""
        checkOut = intent.getStringExtra("CHECK_OUT") ?: ""
        totalPrice = intent.getDoubleExtra("TOTAL_PRICE", 0.0)
        message = intent.getStringExtra("MESSAGE") ?: ""

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvTotal).text = "₱${String.format("%,.0f", totalPrice)}"
        findViewById<TextView>(R.id.tvDates).text = "$checkIn → $checkOut"

        val rgPayment = findViewById<RadioGroup>(R.id.rgPaymentMethod)
        val spSavedCards = findViewById<Spinner>(R.id.spSavedCards)

        // Toggle saved card spinner visibility based on payment method
        rgPayment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbSavedCard && savedCardLabels.isNotEmpty()) {
                spSavedCards?.visible()
            } else {
                spSavedCards?.gone()
            }
        }

        findViewById<Button>(R.id.btnConfirmPayment).setOnClickListener {
            val paymentMethod = when (rgPayment.checkedRadioButtonId) {
                R.id.rbGcash -> "GCash"
                R.id.rbPaymaya -> "PayMaya"
                R.id.rbBankTransfer -> "Bank Transfer"
                R.id.rbSavedCard -> {
                    val selected = spSavedCards?.selectedItem?.toString() ?: ""
                    if (selected.isNotBlank()) "Card: $selected" else "Card"
                }
                else -> "GCash"
            }
            presenter?.confirmPayment(listingId, checkIn, checkOut, message, paymentMethod, totalPrice)
        }

        presenter?.loadListing(listingId)
        presenter?.loadSavedCards()
    }

    override fun showLoading() {
        findViewById<Button>(R.id.btnConfirmPayment).isEnabled = false
        findViewById<ProgressBar>(R.id.progressBar).visible()
    }
    override fun hideLoading() {
        findViewById<Button>(R.id.btnConfirmPayment).isEnabled = true
        findViewById<ProgressBar>(R.id.progressBar).gone()
    }
    override fun showListing(listing: ListingDTO) {
        findViewById<TextView>(R.id.tvListingTitle).text = listing.title
        findViewById<TextView>(R.id.tvListingCity).text = "${listing.city} · ${listing.type}"
    }

    override fun showSavedCards(cards: List<Map<String, String>>) {
        if (cards.isEmpty()) return
        // Show the saved card radio button
        findViewById<RadioButton>(R.id.rbSavedCard)?.visible()

        savedCardLabels.clear()
        cards.forEach { card ->
            val num = card["number"] ?: ""
            val brand = card["brand"] ?: ""
            val last4 = if (num.length >= 4) num.takeLast(4) else num
            val label = if (brand.isNotBlank()) "$brand •••• $last4" else "•••• $last4"
            savedCardLabels.add(label)
        }

        val spSavedCards = findViewById<Spinner>(R.id.spSavedCards)
        if (spSavedCards != null) {
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, savedCardLabels)
            spSavedCards.adapter = adapter
        }
    }

    override fun showError(message: String) { toast(message) }
    override fun onPaymentSuccess() {
        toast("Booking confirmed! 🎉", long = true)
        startActivityClearTask<GuestDashboardActivity>()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
