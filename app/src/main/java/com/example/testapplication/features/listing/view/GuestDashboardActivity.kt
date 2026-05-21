package com.example.testapplication.features.listing.view

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.booking.model.BookingDTO
import com.example.testapplication.features.listing.GuestDashboardContract
import com.example.testapplication.features.listing.model.ListingDTO
import com.example.testapplication.features.listing.presenter.GuestDashboardPresenter
import com.example.testapplication.features.profile.view.ProfileActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Guest Dashboard Activity — browse listings, search, view my trips, and manage favorites.
 * Matches web app's GuestDashboard.jsx functionality.
 */
class GuestDashboardActivity : AppCompatActivity(), GuestDashboardContract.View {

    private var presenter: GuestDashboardContract.Presenter? = null
    private lateinit var adapter: ListingAdapter
    private lateinit var rvListings: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var etSearch: EditText
    private lateinit var tvTripsSection: LinearLayout
    private lateinit var swipeRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private val favoriteIds = mutableSetOf<String>()
    
    private lateinit var btnFilterExplore: TextView
    private lateinit var btnFilterSaved: LinearLayout
    private lateinit var tvSavedCount: TextView
    private var isShowingSaved = false
    private var allListings: List<ListingDTO> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_dashboard)

        val session = LodgioApp.instance.sessionManager
        presenter = GuestDashboardPresenter(this, session)

        // Views
        rvListings = findViewById(R.id.rvListings)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        etSearch = findViewById(R.id.etSearch)
        tvTripsSection = findViewById(R.id.llTripsSection)

        val tvWelcome = findViewById<TextView>(R.id.tvWelcomeName)
        tvWelcome.text = "Welcome, ${session.getFullname() ?: "Guest"}!"

        btnFilterExplore = findViewById(R.id.btnFilterExplore)
        btnFilterSaved = findViewById(R.id.btnFilterSaved)
        tvSavedCount = findViewById(R.id.tvSavedCount)

        btnFilterExplore.setOnClickListener {
            isShowingSaved = false
            updateFilterUI()
            filterListings()
        }

        btnFilterSaved.setOnClickListener {
            isShowingSaved = true
            updateFilterUI()
            filterListings()
        }
        
        updateFilterUI()

        // Setup RecyclerView with favorite support (matches web app's GuestDashboard.jsx)
        adapter = ListingAdapter(
            onItemClick = { listing ->
                val intent = Intent(this, ListingDetailActivity::class.java)
                intent.putExtra("LISTING_ID", listing.id)
                startActivity(intent)
            },
            onFavoriteClick = { listing ->
                toggleFavorite(listing.id ?: "")
            }
        )
        rvListings.layoutManager = GridLayoutManager(this, 2)
        rvListings.adapter = adapter

        // Search action listener

        etSearch.setOnEditorActionListener { _, _, _ ->
            presenter?.loadListings(search = etSearch.textString())
            true
        }

        // Profile button
        findViewById<ImageView>(R.id.btnProfile).setOnClickListener {
            startActivity<ProfileActivity>()
        }

        // Swipe to refresh
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener {
            presenter?.loadListings(search = etSearch.textString())
            presenter?.loadMyTrips()
            loadFavorites()
        }

        // Load data
        presenter?.loadListings()
        presenter?.loadMyTrips()
        loadFavorites()
    }

    // ── Favorites Logic (matches web app's fetchFavorites + toggleFavorite) ──

    private fun loadFavorites() {
        val email = LodgioApp.instance.sessionManager.getEmail() ?: return
        RetrofitClient.listingApi.getFavorites(email).enqueue(object : Callback<List<ListingDTO>> {
            override fun onResponse(call: Call<List<ListingDTO>>, response: Response<List<ListingDTO>>) {
                if (response.isSuccessful && response.body() != null) {
                    favoriteIds.clear()
                    response.body()!!.forEach { it.id?.let { id -> favoriteIds.add(id) } }
                    adapter.updateFavorites(favoriteIds.toSet())
                    tvSavedCount.text = "Saved (${favoriteIds.size})"
                    if (isShowingSaved) filterListings()
                }
            }
            override fun onFailure(call: Call<List<ListingDTO>>, t: Throwable) { /* silent */ }
        })
    }

    private fun toggleFavorite(listingId: String) {
        val email = LodgioApp.instance.sessionManager.getEmail() ?: return
        val isFav = favoriteIds.contains(listingId)

        if (isFav) {
            RetrofitClient.listingApi.removeFavorite(email, listingId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    favoriteIds.remove(listingId)
                    adapter.updateFavorites(favoriteIds.toSet())
                    tvSavedCount.text = "Saved (${favoriteIds.size})"
                    if (isShowingSaved) filterListings()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) { /* silent */ }
            })
        } else {
            RetrofitClient.listingApi.addFavorite(email, listingId).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    favoriteIds.add(listingId)
                    adapter.updateFavorites(favoriteIds.toSet())
                    tvSavedCount.text = "Saved (${favoriteIds.size})"
                    if (isShowingSaved) filterListings()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) { /* silent */ }
            })
        }
    }

    // ── Contract View Methods ──

    private fun updateFilterUI() {
        val ivIcon = btnFilterSaved.getChildAt(0) as? ImageView
        if (isShowingSaved) {
            btnFilterExplore.setBackgroundResource(R.drawable.btn_pill_inactive)
            btnFilterExplore.setTextColor(getColor(R.color.lodgio_text_primary))
            
            btnFilterSaved.setBackgroundResource(R.drawable.btn_pill_active)
            tvSavedCount.setTextColor(getColor(R.color.white))
            ivIcon?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.white))
        } else {
            btnFilterExplore.setBackgroundResource(R.drawable.btn_pill_active)
            btnFilterExplore.setTextColor(getColor(R.color.white))
            
            btnFilterSaved.setBackgroundResource(R.drawable.btn_pill_inactive)
            tvSavedCount.setTextColor(getColor(R.color.lodgio_text_primary))
            ivIcon?.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.lodgio_primary))
        }
    }

    private fun filterListings() {
        if (isShowingSaved) {
            adapter.updateData(allListings.filter { favoriteIds.contains(it.id) })
        } else {
            adapter.updateData(allListings)
        }
        if (adapter.itemCount == 0) {
            tvEmpty.visible()
            rvListings.gone()
        } else {
            tvEmpty.gone()
            rvListings.visible()
        }
    }

    override fun showLoading() {
        progressBar.visible()
        tvEmpty.gone()
    }

    override fun hideLoading() {
        progressBar.gone()
        if (::swipeRefresh.isInitialized) {
            swipeRefresh.isRefreshing = false
        }
    }

    override fun showListings(listings: List<ListingDTO>) {
        allListings = listings
        filterListings()
    }

    override fun showTrips(trips: List<BookingDTO>) {
        if (trips.isEmpty()) {
            tvTripsSection.gone()
            return
        }
        tvTripsSection.visible()
        val llTrips = findViewById<LinearLayout>(R.id.llTripsContainer)
        llTrips.removeAllViews()

        trips.forEach { trip ->
            val tripView = layoutInflater.inflate(R.layout.item_trip_card, llTrips, false)
            tripView.findViewById<TextView>(R.id.tvTripTitle).text = trip.listing?.title ?: "—"
            tripView.findViewById<TextView>(R.id.tvTripCity).text = trip.listing?.city ?: ""
            tripView.findViewById<TextView>(R.id.tvTripDates).text = "${trip.checkInDate} → ${trip.checkOutDate}"
            tripView.findViewById<TextView>(R.id.tvTripPrice).text = "₱${String.format("%,.0f", trip.totalPrice ?: 0.0)}"
            tripView.findViewById<TextView>(R.id.tvTripStatus).text = trip.status ?: ""

            val statusView = tripView.findViewById<TextView>(R.id.tvTripStatus)
            when (trip.status) {
                "ACCEPTED" -> statusView.setTextColor(getColor(R.color.lodgio_success))
                "REJECTED" -> statusView.setTextColor(getColor(R.color.lodgio_error))
                else -> statusView.setTextColor(getColor(R.color.lodgio_host_badge))
            }

            tripView.setOnClickListener {
                val intent = Intent(this, ListingDetailActivity::class.java)
                intent.putExtra("LISTING_ID", trip.listing?.id)
                startActivity(intent)
            }
            llTrips.addView(tripView)
        }
    }

    override fun showEmpty() {
        tvEmpty.visible()
        rvListings.gone()
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun onResume() {
        super.onResume()
        presenter?.loadMyTrips()
        loadFavorites()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
