package com.example.testapplication.features.profile.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.auth.view.LoginActivity
import com.example.testapplication.features.profile.ProfileContract
import com.example.testapplication.features.profile.presenter.ProfilePresenter
import com.google.gson.JsonObject

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private var presenter: ProfileContract.Presenter? = null
    private lateinit var tvAvatarInitial: TextView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvRole: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val session = LodgioApp.instance.sessionManager
        presenter = ProfilePresenter(this, session)

        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        tvName = findViewById(R.id.tvNameInfo)
        tvEmail = findViewById(R.id.tvEmailInfo)
        tvMobile = findViewById(R.id.tvMobileInfo)
        tvRole = findViewById(R.id.tvRoleBadge)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnUpdateProfile).setOnClickListener {
            startActivity<UpdateProfileActivity>()
        }

        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            startActivity<ChangePasswordActivity>()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            presenter?.logout()
        }

        presenter?.loadProfile()
    }

    override fun showLoading() { findViewById<ProgressBar>(R.id.progressBar).visible() }
    override fun hideLoading() { findViewById<ProgressBar>(R.id.progressBar).gone() }

    override fun showProfile(user: JsonObject) {
        val name = user.get("fullname")?.asString ?: "N/A"
        val email = user.get("email")?.asString ?: "N/A"
        val mobile = user.get("mobileNumber")?.asString ?: "N/A"
        val role = user.get("role")?.asString ?: "GUEST"

        tvAvatarInitial.text = if (name.isNotEmpty() && name != "N/A") name[0].uppercase() else "?"
        tvName.text = name
        tvEmail.text = email
        tvMobile.text = mobile
        tvRole.text = role.uppercase()

        if (role.uppercase() == "HOST") {
            tvRole.setBackgroundResource(R.drawable.badge_host)
            tvRole.setTextColor(getColor(R.color.lodgio_host_badge))
        } else {
            tvRole.setBackgroundResource(R.drawable.badge_guest)
            tvRole.setTextColor(getColor(R.color.lodgio_guest_badge))
        }

        // Also update detail section
        try {
            findViewById<TextView>(R.id.tvEmailDetail).text = email
        } catch (_: Exception) { }
    }

    override fun showError(message: String) { toast(message) }
    override fun onLogout() {
        startActivityClearTask<LoginActivity>()
    }

    override fun onResume() {
        super.onResume()
        presenter?.loadProfile()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
