package com.example.testapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)

        val tvDashboardTitle = findViewById<TextView>(R.id.tvDashboardTitle)
        val tvWelcomeText = findViewById<TextView>(R.id.tvWelcomeText)
        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        val tvStatTitle1 = findViewById<TextView>(R.id.tvStatTitle1)
        val tvStatValue1 = findViewById<TextView>(R.id.tvStatValue1)
        val tvStatTitle2 = findViewById<TextView>(R.id.tvStatTitle2)
        val tvStatValue2 = findViewById<TextView>(R.id.tvStatValue2)
        val llHostActions = findViewById<LinearLayout>(R.id.llHostActions)
        val btnViewProfile = findViewById<Button>(R.id.btnViewProfile)
        val btnLogoutAction = findViewById<Button>(R.id.btnLogoutAction)
        val btnProfile = findViewById<ImageView>(R.id.btnProfile)
        val btnLogout = findViewById<ImageView>(R.id.btnLogout)

        val role = sessionManager.getRole()
        
        if (role == "HOST") {
            tvDashboardTitle.text = "Host Dashboard"
            tvWelcomeText.text = "Welcome back! Manage your portfolio."
            tvRoleBadge.text = "🏅 HOST"
            tvRoleBadge.setBackgroundResource(R.drawable.badge_host)
            tvRoleBadge.setTextColor(resources.getColor(R.color.lodgio_host_badge, null))
            llHostActions.visibility = View.VISIBLE
            tvStatTitle1.text = "Your Properties"
            tvStatValue1.text = "Manage listings"
            tvStatTitle2.text = "Account Type"
            tvStatValue2.text = "Host"
        } else {
            tvDashboardTitle.text = "Guest Dashboard"
            tvWelcomeText.text = "Welcome! Discover amazing places to stay."
            tvRoleBadge.text = "🏡 GUEST"
            tvRoleBadge.setBackgroundResource(R.drawable.badge_guest)
            tvRoleBadge.setTextColor(resources.getColor(R.color.lodgio_guest_badge, null))
            llHostActions.visibility = View.GONE
            tvStatTitle1.text = "Explore"
            tvStatValue1.text = "Browse places"
            tvStatTitle2.text = "Account Type"
            tvStatValue2.text = "Guest"
        }

        btnViewProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val logoutAction = View.OnClickListener {
            sessionManager.clear()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnLogout.setOnClickListener(logoutAction)
        btnLogoutAction.setOnClickListener(logoutAction)
    }
}
