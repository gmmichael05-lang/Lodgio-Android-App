package com.example.testapplication.features.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import android.view.View
import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.auth.view.LoginActivity
import com.example.testapplication.features.profile.ProfileContract
import com.example.testapplication.features.profile.presenter.ProfilePresenter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testapplication.core.network.SupabaseUploader

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private var presenter: ProfileContract.Presenter? = null
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvRole: TextView
    private lateinit var ivAvatarPhoto: ImageView

    private var currentUserId: String? = null
    private var contactNumbers = mutableListOf<String>()
    private var savedCards = mutableListOf<JsonObject>()

    /** Safely get a string from a JsonObject, handling JsonNull gracefully */
    private fun JsonObject.safeString(key: String): String? {
        val el = this.get(key) ?: return null
        return if (el.isJsonNull) null else try { el.asString } catch (_: Exception) { null }
    }

    // Gallery picker for avatar
    private val avatarPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        uploadAvatar(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val session = LodgioApp.instance.sessionManager
        presenter = ProfilePresenter(this, session)
        currentUserId = session.getUserId()

        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvName = findViewById(R.id.tvNameInfo)
        tvEmail = findViewById(R.id.tvEmailInfo)
        tvMobile = findViewById(R.id.tvMobileInfo)
        tvRole = findViewById(R.id.tvRoleBadge)
        ivAvatarPhoto = findViewById(R.id.ivAvatarPhoto)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        // Avatar photo change
        findViewById<ImageView>(R.id.btnChangePhoto).setOnClickListener {
            avatarPickerLauncher.launch("image/*")
        }
        // Also allow tapping the avatar itself
        findViewById<android.widget.FrameLayout>(R.id.flAvatar).setOnClickListener {
            avatarPickerLauncher.launch("image/*")
        }

        findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            startActivity<UpdateProfileActivity>()
        }

        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            startActivity<ChangePasswordActivity>()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter?.logout()
        }

        // Add Contact button
        findViewById<Button>(R.id.btnAddContact)?.setOnClickListener {
            showAddContactDialog()
        }

        // Add Card button
        findViewById<Button>(R.id.btnAddCard)?.setOnClickListener {
            showAddCardDialog()
        }

        presenter?.loadProfile()
    }

    private fun uploadAvatar(uri: Uri) {
        val userId = currentUserId ?: return
        toast("Uploading photo...")

        // Show preview immediately
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                ivAvatarPhoto.setImageBitmap(bitmap)
                ivAvatarPhoto.visible()
                tvAvatarInitial.gone()
            }
        } catch (_: Exception) { }

        // Determine file extension
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when {
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        val storagePath = "$userId/$userId.$ext"

        SupabaseUploader.uploadImage(
            context = this,
            imageUri = uri,
            storagePath = storagePath,
            onSuccess = { publicUrl ->
                runOnUiThread {
                    toast("Photo uploaded!")
                    // Update backend profile picture URL
                    updateProfilePictureOnBackend(userId, publicUrl)
                }
            },
            onError = { message ->
                runOnUiThread {
                    toast("Upload failed: $message")
                    // Revert preview
                    ivAvatarPhoto.gone()
                    tvAvatarInitial.visible()
                }
            }
        )
    }

    private fun updateProfilePictureOnBackend(userId: String, publicUrl: String) {
        RetrofitClient.userApi.updateProfilePicture(userId, publicUrl).enqueue(object : retrofit2.Callback<JsonObject> {
            override fun onResponse(call: retrofit2.Call<JsonObject>, response: retrofit2.Response<JsonObject>) {
                // Profile picture updated on backend
            }
            override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                runOnUiThread { toast("Backend sync failed") }
            }
        })
    }

    private fun loadProfileImage(url: String) {
        try {
            com.bumptech.glide.Glide.with(this)
                .load(url)
                .circleCrop()
                .into(ivAvatarPhoto)
            ivAvatarPhoto.visible()
            tvAvatarInitial.gone()
        } catch (_: Exception) {
            // If loading fails, keep showing the initial letter
        }
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() { findViewById<ProgressBar>(R.id.progressBar).gone() }

    override fun showProfile(user: JsonObject) {
        val name = user.safeString("fullname") ?: "N/A"
        val email = user.safeString("email") ?: "N/A"
        val mobile = user.safeString("mobileNumber") ?: "N/A"
        val role = user.safeString("role") ?: "GUEST"

        tvAvatarInitial.text = if (name.isNotEmpty() && name != "N/A") name[0].uppercase() else "?"
        tvName.text = name
        tvEmail.text = email
        tvMobile.text = mobile
        tvRole.text = role.uppercase()

        if (role.uppercase() == "HOST") {
            tvRole.setBackgroundResource(R.drawable.badge_host)
            tvRole.setTextColor(getColor(R.color.lodgio_host_badge))
        } else if (role.uppercase() == "ADMIN") {
            tvRole.setBackgroundResource(R.drawable.badge_admin)
            tvRole.setTextColor(getColor(R.color.lodgio_admin_badge))
        } else {
            tvRole.setBackgroundResource(R.drawable.badge_guest)
            tvRole.setTextColor(getColor(R.color.lodgio_guest_badge))
        }

        // Load profile picture if available
        try {
            val profilePicUrl = user.safeString("profilePictureUrl")
            if (!profilePicUrl.isNullOrBlank()) {
                loadProfileImage(profilePicUrl)
            }
        } catch (_: Exception) { }

        // Update detail section
        try {
            findViewById<TextView>(R.id.tvEmailDetail).text = email
        } catch (_: Exception) { }

        // Parse and display contact numbers
        try {
            val contactsStr = user.safeString("contactNumbers") ?: ""
            contactNumbers.clear()
            if (contactsStr.isNotBlank()) {
                contactsStr.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach {
                    contactNumbers.add(it)
                }
            }
            renderContacts()
        } catch (_: Exception) { renderContacts() }

        // Parse and display saved cards
        try {
            val cardsStr = user.safeString("savedCards") ?: ""
            savedCards.clear()
            if (cardsStr.isNotBlank()) {
                val arr = JsonParser().parse(cardsStr).asJsonArray
                for (i in 0 until arr.size()) {
                    savedCards.add(arr.get(i).asJsonObject)
                }
            }
            renderCards()
        } catch (_: Exception) { renderCards() }
    }

    private fun renderContacts() {
        val llContacts = findViewById<LinearLayout>(R.id.llContactNumbers) ?: return
        llContacts.removeAllViews()

        if (contactNumbers.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No additional contacts saved."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 8, 0, 8)
            }
            llContacts.addView(tv)
            return
        }

        contactNumbers.forEachIndexed { index, number ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            val tvNum = TextView(this).apply {
                text = "📞  $number"
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnRemove = ImageView(this).apply {
                setImageResource(R.drawable.ic_delete)
                layoutParams = LinearLayout.LayoutParams(64, 64)
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    contactNumbers.removeAt(index)
                    persistContacts()
                }
            }
            row.addView(tvNum)
            row.addView(btnRemove)
            llContacts.addView(row)
        }
    }

    private fun renderCards() {
        val llCards = findViewById<LinearLayout>(R.id.llSavedCards) ?: return
        llCards.removeAllViews()

        if (savedCards.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No saved payment cards."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 8, 0, 8)
            }
            llCards.addView(tv)
            return
        }

        savedCards.forEachIndexed { index, card ->
            val num = card.safeString("number") ?: ""
            val brand = card.safeString("brand") ?: ""
            val expiry = card.safeString("expiry") ?: ""
            val last4 = if (num.length >= 4) num.takeLast(4) else num

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 12)
            }
            val tvCard = TextView(this).apply {
                text = "💳  $brand •••• $last4  ($expiry)"
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_primary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val btnRemove = ImageView(this).apply {
                setImageResource(R.drawable.ic_delete)
                layoutParams = LinearLayout.LayoutParams(64, 64)
                setPadding(8, 8, 8, 8)
                setOnClickListener {
                    savedCards.removeAt(index)
                    persistCards()
                }
            }
            row.addView(tvCard)
            row.addView(btnRemove)
            llCards.addView(row)
        }
    }

    private fun showAddContactDialog() {
        val input = EditText(this).apply {
            hint = "e.g. 09123456789"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("Add Contact Number")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val number = input.text.toString().trim()
                if (number.isNotBlank()) {
                    contactNumbers.add(number)
                    persistContacts()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddCardDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_card, null)
        AlertDialog.Builder(this)
            .setTitle("Add Payment Card")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val label = view.findViewById<EditText>(R.id.etCardLabel).text.toString().trim()
                val number = view.findViewById<EditText>(R.id.etCardNumber).text.toString().trim()
                val expiry = view.findViewById<EditText>(R.id.etCardExpiry).text.toString().trim()

                if (number.isBlank()) {
                    toast("Card number is required.")
                    return@setPositiveButton
                }

                // Auto-detect brand
                val brand = when {
                    number.startsWith("4") -> "Visa"
                    number.startsWith("5") || number.startsWith("2") -> "Mastercard"
                    number.startsWith("3") -> "Amex"
                    else -> "Card"
                }

                val card = JsonObject().apply {
                    addProperty("label", if (label.isNotBlank()) label else brand)
                    addProperty("number", number)
                    addProperty("expiry", expiry)
                    addProperty("brand", brand)
                }
                savedCards.add(card)
                persistCards()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun persistContacts() {
        val userId = currentUserId ?: return
        val csv = contactNumbers.joinToString(",")
        presenter?.saveContacts(userId, csv)
    }

    private fun persistCards() {
        val userId = currentUserId ?: return
        val arr = JsonArray()
        for (card in savedCards) { arr.add(card) }
        presenter?.saveCards(userId, arr.toString())
    }

    override fun onContactsSaved() {
        toast("Contacts saved!")
        renderContacts()
    }

    override fun onCardsSaved() {
        toast("Cards saved!")
        renderCards()
    }

    override fun showError(message: String) { toast(message) }
    override fun onLogout() {
        startActivityClearTask<LoginActivity>()
    }

    override fun onReviewSubmitted() {
        toast("Review submitted successfully!")
        loadHistory()
    }

    override fun onResume() {
        super.onResume()
        try {
            presenter?.loadProfile()
            loadHistory()
        } catch (_: Exception) { }
    }

    private fun loadHistory() {
        val session = LodgioApp.instance.sessionManager
        val email = session.getEmail() ?: return
        val role = session.getRole()

        if (role == "HOST") {
            // Load listing history
            RetrofitClient.listingApi.getListingsByHostEmail(email).enqueue(object : retrofit2.Callback<List<com.example.testapplication.features.listing.model.ListingDTO>> {
                override fun onResponse(call: retrofit2.Call<List<com.example.testapplication.features.listing.model.ListingDTO>>, response: retrofit2.Response<List<com.example.testapplication.features.listing.model.ListingDTO>>) {
                    if (response.isSuccessful && response.body() != null) {
                        renderListingHistory(response.body()!!)
                    }
                }
                override fun onFailure(call: retrofit2.Call<List<com.example.testapplication.features.listing.model.ListingDTO>>, t: Throwable) { }
            })
        } else {
            // Load booking history and reviewed bookings
            RetrofitClient.listingApi.getReviewedBookings(email).enqueue(object : retrofit2.Callback<List<String>> {
                override fun onResponse(call: retrofit2.Call<List<String>>, response1: retrofit2.Response<List<String>>) {
                    val reviewed = if (response1.isSuccessful) {
                        response1.body()?.filterNotNull()?.map { it.lowercase() } ?: emptyList()
                    } else {
                        emptyList()
                    }
                    RetrofitClient.bookingApi.getBookingsByGuestEmail(email).enqueue(object : retrofit2.Callback<List<com.example.testapplication.features.booking.model.BookingDTO>> {
                        override fun onResponse(call: retrofit2.Call<List<com.example.testapplication.features.booking.model.BookingDTO>>, response: retrofit2.Response<List<com.example.testapplication.features.booking.model.BookingDTO>>) {
                            if (response.isSuccessful && response.body() != null) {
                                renderBookingHistory(response.body()!!, reviewed)
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<List<com.example.testapplication.features.booking.model.BookingDTO>>, t: Throwable) { }
                    })
                }
                override fun onFailure(call: retrofit2.Call<List<String>>, t: Throwable) { }
            })
        }
    }

    private fun renderBookingHistory(bookings: List<com.example.testapplication.features.booking.model.BookingDTO>, reviewedListingIds: List<String>) {
        val llHistory = findViewById<LinearLayout>(R.id.llBookingHistory) ?: return
        val tvHistoryLabel = findViewById<TextView>(R.id.tvHistoryLabel) ?: return
        tvHistoryLabel.text = "📅 Booking History"
        tvHistoryLabel.visible()
        llHistory.visible()
        llHistory.removeAllViews()

        if (bookings.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No bookings yet. Browse listings to make your first reservation."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 16, 0, 16)
            }
            llHistory.addView(tv)
            return
        }

        bookings.forEach { b ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }
            val tvTitle = TextView(this).apply {
                text = b.listing?.title ?: "—"
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val tvDates = TextView(this).apply {
                text = "${b.checkInDate} → ${b.checkOutDate}"
                textSize = 12f
                setTextColor(getColor(R.color.lodgio_text_secondary))
            }
            val tvInfo = TextView(this).apply {
                text = "₱${String.format("%,.0f", b.totalPrice ?: 0.0)}  ·  ${b.status ?: "PENDING"}"
                textSize = 12f
                setTextColor(when(b.status) {
                    "ACCEPTED" -> getColor(R.color.lodgio_success)
                    "REJECTED" -> getColor(R.color.lodgio_error)
                    else -> getColor(R.color.lodgio_warning)
                })
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tvTitle)
            row.addView(tvDates)
            row.addView(tvInfo)

            if (b.status == "ACCEPTED" && b.listing?.id != null && b.id != null && !reviewedListingIds.contains(b.id!!.lowercase())) {
                val btnReview = Button(this).apply {
                    text = "Rate & Review"
                    textSize = 12f
                    isAllCaps = false
                    setTextColor(getColor(R.color.lodgio_primary))
                    setBackgroundResource(R.drawable.btn_outlined_bg)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 100).apply {
                        topMargin = 16
                    }
                    setPadding(32, 0, 32, 0)
                    setOnClickListener { showReviewDialog(b.listing.id, b.id) }
                }
                row.addView(btnReview)
            }

            // Divider
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(getColor(R.color.lodgio_divider))
            }
            llHistory.addView(row)
            llHistory.addView(divider)
        }
    }

    private fun showReviewDialog(listingId: String, bookingId: String) {
        if (listingId.isBlank() || bookingId.isBlank()) {
            Toast.makeText(this, "Invalid booking data for review.", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_leave_review, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val ratingBar = dialogView.findViewById<android.widget.RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etReviewComment)
        
        dialogView.findViewById<Button>(R.id.btnCancelReview).setOnClickListener { dialog.dismiss() }
        dialogView.findViewById<Button>(R.id.btnSubmitReview).setOnClickListener {
            val comment = etComment.textString()
            if (comment.isBlank()) {
                toast("Please enter a comment")
                return@setOnClickListener
            }
            val rating = ratingBar.rating.toInt()
            presenter?.submitReview(listingId, bookingId, rating, comment)
            dialog.dismiss()
        }
        
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun renderListingHistory(listings: List<com.example.testapplication.features.listing.model.ListingDTO>) {
        val llHistory = findViewById<LinearLayout>(R.id.llBookingHistory) ?: return
        val tvHistoryLabel = findViewById<TextView>(R.id.tvHistoryLabel) ?: return
        tvHistoryLabel.text = "🏠 Listing History"
        tvHistoryLabel.visible()
        llHistory.visible()
        llHistory.removeAllViews()

        if (listings.isEmpty()) {
            val tv = TextView(this).apply {
                text = "No listings yet. Create your first listing from the Host Dashboard."
                textSize = 13f
                setTextColor(getColor(R.color.lodgio_text_hint))
                setPadding(0, 16, 0, 16)
            }
            llHistory.addView(tv)
            return
        }

        listings.forEach { lst ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 16, 0, 16)
            }
            val tvTitle = TextView(this).apply {
                text = lst.title ?: "Untitled"
                textSize = 14f
                setTextColor(getColor(R.color.lodgio_text_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            val tvDetails = TextView(this).apply {
                text = "${lst.type ?: "Property"} · ${lst.city ?: ""}"
                textSize = 12f
                setTextColor(getColor(R.color.lodgio_text_secondary))
            }
            val tvPrice = TextView(this).apply {
                text = "₱${String.format("%,.0f", lst.pricePerNight ?: 0.0)}/night  ·  ${lst.status ?: "ACTIVE"}"
                textSize = 12f
                setTextColor(getColor(R.color.lodgio_primary))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            row.addView(tvTitle)
            row.addView(tvDetails)
            row.addView(tvPrice)

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(getColor(R.color.lodgio_divider))
            }
            llHistory.addView(row)
            llHistory.addView(divider)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}

