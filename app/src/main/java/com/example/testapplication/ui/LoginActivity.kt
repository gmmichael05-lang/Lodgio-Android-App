package com.example.testapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.api.RetrofitClient
import com.example.testapplication.models.AuthResponse
import com.example.testapplication.models.LoginRequest
import com.example.testapplication.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // Check if already logged in
        if (sessionManager.getToken() != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoToRegister = findViewById<TextView>(R.id.btnGoToRegister)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = LoginRequest(email, password)
            RetrofitClient.instance.login(request = request).enqueue(object : retrofit2.Callback<AuthResponse> {
                override fun onResponse(call: retrofit2.Call<AuthResponse>, response: retrofit2.Response<AuthResponse>) {
                    btnLogin.isEnabled = true
                    progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful && response.body() != null) {
                        val authBody = response.body()!!
                        val token = authBody.access_token
                        val role = authBody.user?.user_metadata?.role ?: "GUEST"
                        val userId = authBody.user?.id
                        
                        if (token != null) {
                            sessionManager.saveToken(token)
                            sessionManager.saveRole(role)
                            if (userId != null) sessionManager.saveUserId(userId)
                            
                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login failed: no token received", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        when (response.code()) {
                            400 -> Toast.makeText(this@LoginActivity, "Bad Request: Invalid format", Toast.LENGTH_SHORT).show()
                            401 -> Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            500 -> Toast.makeText(this@LoginActivity, "Server error. Try again later.", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@LoginActivity, "Login failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                    btnLogin.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "No internet connection or API failure: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
