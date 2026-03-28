package com.example.testapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.api.RetrofitClient
import com.example.testapplication.models.SupabaseUser
import com.example.testapplication.utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var tvRoleBadge: TextView
    private lateinit var tvNameInfo: TextView
    private lateinit var tvEmailInfo: TextView
    private lateinit var tvMobileInfo: TextView
    private lateinit var tvEmailDetail: TextView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)
        tvNameInfo = findViewById(R.id.tvNameInfo)
        tvEmailInfo = findViewById(R.id.tvEmailInfo)
        tvMobileInfo = findViewById(R.id.tvMobileInfo)
        tvEmailDetail = findViewById(R.id.tvEmailDetail)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)
        progressBar = findViewById(R.id.progressBar)
        
        val btnUpdateProfile = findViewById<Button>(R.id.btnUpdateProfile)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        loadProfile()

        btnUpdateProfile.setOnClickListener {
            startActivity(Intent(this, UpdateProfileActivity::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val token = sessionManager.getToken()
        if (token == null) {
            handleError("Session Null! Please log in again.")
            return
        }

        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getProfile(token = "Bearer $token").enqueue(object : retrofit2.Callback<SupabaseUser> {
            override fun onResponse(call: retrofit2.Call<SupabaseUser>, response: retrofit2.Response<SupabaseUser>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val user = response.body()
                    val meta = user?.user_metadata
                    
                    val id = user?.id
                    if (id != null) {
                        sessionManager.saveUserId(id)
                    }
                    
                    val name = meta?.fullname ?: "N/A"
                    val email = user?.email ?: "N/A"
                    val mobile = meta?.mobileNumber ?: "N/A"
                    val role = (meta?.role ?: "GUEST").uppercase()
                    
                    // Set avatar initial
                    tvAvatarInitial.text = if (name.isNotEmpty() && name != "N/A") name[0].uppercase() else "?"
                    
                    tvNameInfo.text = name
                    tvEmailInfo.text = email
                    tvEmailDetail.text = email
                    tvMobileInfo.text = mobile
                    
                    // Set role badge styling
                    tvRoleBadge.text = role
                    if (role == "HOST") {
                        tvRoleBadge.setBackgroundResource(R.drawable.badge_host)
                        tvRoleBadge.setTextColor(resources.getColor(R.color.lodgio_host_badge, null))
                    } else {
                        tvRoleBadge.setBackgroundResource(R.drawable.badge_guest)
                        tvRoleBadge.setTextColor(resources.getColor(R.color.lodgio_guest_badge, null))
                    }
                } else if (response.code() == 401) {
                    handleError("Session expired")
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<SupabaseUser>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        sessionManager.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
