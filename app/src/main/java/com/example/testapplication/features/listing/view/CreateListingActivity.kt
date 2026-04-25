package com.example.testapplication.features.listing.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.listing.CreateListingContract
import com.example.testapplication.features.listing.presenter.CreateListingPresenter

class CreateListingActivity : AppCompatActivity(), CreateListingContract.View {

    private var presenter: CreateListingContract.Presenter? = null
    private val PROPERTY_TYPES = arrayOf("Apartment", "Villa", "House", "Condo", "Studio", "Loft", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        presenter = CreateListingPresenter(this, LodgioApp.instance.sessionManager)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etCity = findViewById<EditText>(R.id.etCity)
        val spType = findViewById<Spinner>(R.id.spType)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etGuests = findViewById<EditText>(R.id.etGuests)
        val etBeds = findViewById<EditText>(R.id.etBeds)
        val etBaths = findViewById<EditText>(R.id.etBaths)
        val etImageUrls = findViewById<EditText>(R.id.etImageUrls)
        val etAmenities = findViewById<EditText>(R.id.etAmenities)

        // Spinner setup
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, PROPERTY_TYPES)
        spType.adapter = typeAdapter

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnPublish).setOnClickListener {
            presenter?.createListing(
                title = etTitle.textString(),
                city = etCity.textString(),
                type = spType.selectedItem as String,
                description = etDescription.textString(),
                pricePerNight = etPrice.textString(),
                guestCapacity = etGuests.textString(),
                beds = etBeds.textString(),
                baths = etBaths.textString(),
                imageUrls = etImageUrls.textString(),
                amenities = etAmenities.textString()
            )
        }
    }

    override fun showLoading() {
        findViewById<Button>(R.id.btnPublish).isEnabled = false
        findViewById<ProgressBar>(R.id.progressBar).visible()
    }
    override fun hideLoading() {
        findViewById<Button>(R.id.btnPublish).isEnabled = true
        findViewById<ProgressBar>(R.id.progressBar).gone()
    }
    override fun showError(message: String) { toast(message) }
    override fun onListingCreated() {
        toast("Listing published!")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
