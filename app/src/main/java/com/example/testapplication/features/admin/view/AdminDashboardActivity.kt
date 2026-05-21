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
    private lateinit var swipeRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

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

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            presenter?.loadAdminData()
        }

        presenter?.loadAdminData()
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() {
        findViewById<ProgressBar>(R.id.progressBar).gone()
        if (::swipeRefresh.isInitialized) {
            swipeRefresh.isRefreshing = false
        }
    }

    override fun showUsers(users: List<JsonObject>) {
        llUsers.removeAllViews()
        findViewById<TextView>(R.id.tvUserCount).text = "${users.size} users"

        // Safe getter to handle JsonNull
        fun JsonObject.safe(key: String): String? {
            val el = this.get(key) ?: return null
            return if (el.isJsonNull) null else try { el.asString } catch (_: Exception) { null }
        }

        // Populate stats
        val guests = users.count { it.safe("role") == "GUEST" }
        val hosts = users.count { it.safe("role") == "HOST" }
        findViewById<TextView>(R.id.tvStatUsers).text = "${users.size}"
        findViewById<TextView>(R.id.tvStatGuests).text = "$guests"
        findViewById<TextView>(R.id.tvStatHosts).text = "$hosts"

        users.forEach { user ->
            val v = layoutInflater.inflate(R.layout.item_admin_user, llUsers, false)
            val fullname = user.safe("fullname") ?: "Unknown"
            val role = user.safe("role") ?: "GUEST"
            v.findViewById<TextView>(R.id.tvUserName).text = fullname
            v.findViewById<TextView>(R.id.tvUserEmail).text = user.safe("email") ?: ""
            v.findViewById<TextView>(R.id.tvUserRole).text = role

            // Avatar initial
            val tvInitial = v.findViewById<TextView>(R.id.tvUserInitial)
            tvInitial.text = if (fullname.isNotBlank()) fullname[0].uppercase() else "?"

            // Role badge color
            val tvRole = v.findViewById<TextView>(R.id.tvUserRole)
            when (role) {
                "HOST" -> {
                    tvRole.setBackgroundResource(R.drawable.badge_host)
                    tvRole.setTextColor(getColor(R.color.lodgio_host_badge))
                }
                "ADMIN" -> {
                    tvRole.setBackgroundResource(R.drawable.badge_admin)
                    tvRole.setTextColor(getColor(R.color.lodgio_admin_badge))
                }
                else -> {
                    tvRole.setBackgroundResource(R.drawable.badge_guest)
                    tvRole.setTextColor(getColor(R.color.lodgio_guest_badge))
                }
            }

            v.findViewById<ImageView>(R.id.btnDeleteUser).setOnClickListener {
                val uid = user.safe("id") ?: return@setOnClickListener
                AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Delete ${fullname}?")
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
        findViewById<TextView>(R.id.tvStatListings).text = "${listings.size}"
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
