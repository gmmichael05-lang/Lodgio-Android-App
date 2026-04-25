package com.example.testapplication.features.admin.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.admin.AdminContract
import com.example.testapplication.features.admin.presenter.AdminPresenter
import com.example.testapplication.features.auth.view.LoginActivity
import com.example.testapplication.features.listing.model.ListingDTO
import com.google.gson.JsonObject

class AdminDashboardActivity : AppCompatActivity(), AdminContract.View {

    private var presenter: AdminContract.Presenter? = null
    private lateinit var llUsers: LinearLayout
    private lateinit var llListings: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        presenter = AdminPresenter(this)
        llUsers = findViewById(R.id.llUsers)
        llListings = findViewById(R.id.llAdminListings)

        val session = LodgioApp.instance.sessionManager
        findViewById<TextView>(R.id.tvAdminName).text = "Admin: ${session.getFullname() ?: session.getEmail() ?: ""}"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.clear()
            startActivityClearTask<LoginActivity>()
        }

        presenter?.loadAdminData()
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() { findViewById<ProgressBar>(R.id.progressBar).gone() }

    override fun showUsers(users: List<JsonObject>) {
        llUsers.removeAllViews()
        findViewById<TextView>(R.id.tvUserCount).text = "${users.size} users"
        users.forEach { user ->
            val v = layoutInflater.inflate(R.layout.item_admin_user, llUsers, false)
            v.findViewById<TextView>(R.id.tvUserName).text = user.get("fullname")?.asString ?: "Unknown"
            v.findViewById<TextView>(R.id.tvUserEmail).text = user.get("email")?.asString ?: ""
            v.findViewById<TextView>(R.id.tvUserRole).text = user.get("role")?.asString ?: "GUEST"

            v.findViewById<ImageView>(R.id.btnDeleteUser).setOnClickListener {
                val uid = user.get("id")?.asString ?: return@setOnClickListener
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Delete ${user.get("fullname")?.asString}?")
                    .setPositiveButton("Delete") { _, _ -> presenter?.deleteUser(uid) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            llUsers.addView(v)
        }
    }

    override fun showListings(listings: List<ListingDTO>) {
        llListings.removeAllViews()
        findViewById<TextView>(R.id.tvListingCount).text = "${listings.size} listings"
        listings.forEach { lst ->
            val v = layoutInflater.inflate(R.layout.item_admin_listing, llListings, false)
            v.findViewById<TextView>(R.id.tvListingTitle).text = lst.title ?: "Untitled"
            v.findViewById<TextView>(R.id.tvListingHost).text = "Host: ${lst.host?.fullname ?: "Unknown"}"
            v.findViewById<TextView>(R.id.tvListingPrice).text = "₱${String.format("%,.0f", lst.pricePerNight ?: 0.0)}/night"
            v.findViewById<TextView>(R.id.tvListingStatus).text = lst.status ?: "ACTIVE"

            v.findViewById<ImageView>(R.id.btnDeleteListing).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete Listing")
                    .setMessage("Delete \"${lst.title}\"?")
                    .setPositiveButton("Delete") { _, _ -> presenter?.deleteListing(lst.id ?: "") }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            llListings.addView(v)
        }
    }

    override fun showError(message: String) { toast(message) }
    override fun onUserDeleted() { toast("User deleted"); presenter?.loadAdminData() }
    override fun onListingDeleted() { toast("Listing deleted"); presenter?.loadAdminData() }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
