package com.example.testapplication.features.listing.view

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.core.network.SupabaseUploader
import com.example.testapplication.features.listing.CreateListingContract
import com.example.testapplication.features.listing.presenter.CreateListingPresenter

class CreateListingActivity : AppCompatActivity(), CreateListingContract.View {

    private var presenter: CreateListingContract.Presenter? = null
    private val PROPERTY_TYPES = arrayOf("Apartment", "Villa", "House", "Condo", "Studio", "Loft", "Other")
    private val AMENITY_OPTIONS = arrayOf("Wifi", "Parking", "Pool", "AC", "Gym", "Pet-friendly", "Kitchen", "TV", "Parking Space")
    private val selectedAmenities = mutableSetOf<String>()

    // Image upload state
    private val selectedImageUris = mutableListOf<Uri>()
    private val uploadedImageUrls = mutableListOf<String>()
    private var isUploading = false

    // Gallery picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        if (selectedImageUris.size >= 10) {
            showImageError("Maximum 10 images allowed.")
            return@registerForActivityResult
        }
        selectedImageUris.add(uri)
        addImagePreview(uri)
        clearImageError()
    }

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

        // Spinner setup
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, PROPERTY_TYPES)
        spType.adapter = typeAdapter

        // Amenities chip-like selector
        val llAmenityChips = findViewById<LinearLayout>(R.id.llAmenityChips)
        if (llAmenityChips != null) {
            AMENITY_OPTIONS.forEach { amenity ->
                val chip = CheckBox(this).apply {
                    text = amenity
                    textSize = 13f
                    setTextColor(getColor(R.color.lodgio_text_primary))
                    setPadding(8, 4, 8, 4)
                    setOnCheckedChangeListener { _, checked ->
                        if (checked) selectedAmenities.add(amenity) else selectedAmenities.remove(amenity)
                    }
                }
                llAmenityChips.addView(chip)
            }
        }

        // Upload zone tap
        findViewById<LinearLayout>(R.id.llUploadZone).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnPublish).setOnClickListener {
            // Validate: need at least one image selected
            if (selectedImageUris.isEmpty()) {
                showImageError("Please select at least one photo of your property.")
                return@setOnClickListener
            }

            val amenitiesStr = if (selectedAmenities.isNotEmpty()) {
                selectedAmenities.joinToString(",")
            } else {
                findViewById<EditText>(R.id.etAmenities)?.textString() ?: ""
            }

            // Start uploading images to Supabase, then create listing
            uploadAllImagesAndPublish(
                title = etTitle.textString(),
                city = etCity.textString(),
                type = spType.selectedItem as String,
                description = etDescription.textString(),
                pricePerNight = etPrice.textString(),
                guestCapacity = etGuests.textString(),
                beds = etBeds.textString(),
                baths = etBaths.textString(),
                amenities = amenitiesStr
            )
        }
    }

    private fun uploadAllImagesAndPublish(
        title: String, city: String, type: String, description: String,
        pricePerNight: String, guestCapacity: String, beds: String,
        baths: String, amenities: String
    ) {
        if (isUploading) return
        isUploading = true
        showLoading()
        toast("Uploading images...")

        val userId = LodgioApp.instance.sessionManager.getUserId() ?: "unknown"
        uploadedImageUrls.clear()
        uploadNextImage(0, userId, title, city, type, description, pricePerNight, guestCapacity, beds, baths, amenities)
    }

    private fun uploadNextImage(
        index: Int, userId: String,
        title: String, city: String, type: String, description: String,
        pricePerNight: String, guestCapacity: String, beds: String,
        baths: String, amenities: String
    ) {
        if (index >= selectedImageUris.size) {
            // All images uploaded — create the listing
            isUploading = false
            val imageUrlsStr = uploadedImageUrls.joinToString(",")

            presenter?.createListing(
                title = title, city = city, type = type, description = description,
                pricePerNight = pricePerNight, guestCapacity = guestCapacity,
                beds = beds, baths = baths, imageUrls = imageUrlsStr, amenities = amenities
            )
            return
        }

        val uri = selectedImageUris[index]
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when {
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val storagePath = "listing-images/$userId/${System.currentTimeMillis()}_$index.$ext"

        SupabaseUploader.uploadImage(
            context = this,
            imageUri = uri,
            storagePath = storagePath,
            onSuccess = { publicUrl ->
                uploadedImageUrls.add(publicUrl)
                runOnUiThread {
                    // Upload next
                    uploadNextImage(index + 1, userId, title, city, type, description, pricePerNight, guestCapacity, beds, baths, amenities)
                }
            },
            onError = { message ->
                runOnUiThread {
                    isUploading = false
                    hideLoading()
                    showImageError("Failed to upload image ${index + 1}: $message")
                }
            }
        )
    }

    private fun addImagePreview(uri: Uri) {
        val llPreviews = findViewById<LinearLayout>(R.id.llImagePreviews)
        val hsvPreviews = findViewById<HorizontalScrollView>(R.id.hsvImagePreviews)
        hsvPreviews.visible()

        val container = FrameLayout(this).apply {
            val size = (100 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (8 * resources.displayMetrics.density).toInt()
            }
        }

        val iv = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Load bitmap preview
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            iv.setImageBitmap(bitmap)
        } catch (_: Exception) {
            iv.setBackgroundColor(getColor(R.color.lodgio_bg_surface))
        }

        // Remove button overlay
        val btnRemove = TextView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                (24 * resources.displayMetrics.density).toInt(),
                (24 * resources.displayMetrics.density).toInt(),
                android.view.Gravity.TOP or android.view.Gravity.END
            )
            text = "✕"
            setTextColor(getColor(R.color.white))
            textSize = 11f
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(0x99000000.toInt())
            setOnClickListener {
                val idx = llPreviews.indexOfChild(container)
                if (idx >= 0 && idx < selectedImageUris.size) {
                    selectedImageUris.removeAt(idx)
                }
                llPreviews.removeView(container)
                if (selectedImageUris.isEmpty()) {
                    hsvPreviews.gone()
                }
            }
        }

        // Cover badge for first image
        if (selectedImageUris.size == 1) {
            val coverBadge = TextView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    android.view.Gravity.BOTTOM or android.view.Gravity.START
                ).apply {
                    marginStart = (4 * resources.displayMetrics.density).toInt()
                    bottomMargin = (4 * resources.displayMetrics.density).toInt()
                }
                text = "COVER"
                textSize = 9f
                setTextColor(getColor(R.color.white))
                setBackgroundColor(getColor(R.color.lodgio_primary))
                setPadding(8, 2, 8, 2)
            }
            container.addView(iv)
            container.addView(coverBadge)
        } else {
            container.addView(iv)
        }

        container.addView(btnRemove)
        llPreviews.addView(container)
    }

    private fun showImageError(msg: String) {
        val tv = findViewById<TextView>(R.id.tvImageError)
        tv.text = "⚠️ $msg"
        tv.visible()
    }

    private fun clearImageError() {
        findViewById<TextView>(R.id.tvImageError).gone()
    }

    override fun showLoading() {
        findViewById<Button>(R.id.btnPublish).isEnabled = false
        findViewById<Button>(R.id.btnPublish).text = "Uploading & Publishing..."
        findViewById<ProgressBar>(R.id.progressBar).visible()
    }
    override fun hideLoading() {
        findViewById<Button>(R.id.btnPublish).isEnabled = true
        findViewById<Button>(R.id.btnPublish).text = "Publish Listing"
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
