package com.example.testapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.api.RetrofitClient
import com.example.testapplication.models.BackendUser
import com.example.testapplication.models.RegisterRequest
import com.example.testapplication.models.SupabaseUserMetadata
import com.google.gson.JsonObject

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val rgRole = findViewById<RadioGroup>(R.id.rgRole)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPasswordConfirm = findViewById<EditText>(R.id.etPasswordConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnGoToLogin = findViewById<TextView>(R.id.btnGoToLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnRegister.setOnClickListener {
            val role = if (rgRole.checkedRadioButtonId == R.id.rbHost) "HOST" else "GUEST"
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val mobile = etMobile.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val passConfirm = etPasswordConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || passConfirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != passConfirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val metadata = SupabaseUserMetadata(name, role, mobile)
            val request = RegisterRequest(email, password, metadata)
            
            Log.d("LODGIO_REGISTER", "Step 1: Sending Supabase signup for: $email with role: $role")
            
            // STEP 1: Create auth user in Supabase (same as web app's supabase.auth.signUp)
            RetrofitClient.instance.register(request = request).enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onResponse(call: retrofit2.Call<JsonObject>, response: retrofit2.Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("LODGIO_REGISTER", "Step 1 SUCCESS: $body")
                        
                        // Extract user ID from Supabase response
                        val userId = body?.getAsJsonObject("user")?.get("id")?.asString
                            ?: body?.get("id")?.asString
                        
                        if (userId != null) {
                            Log.d("LODGIO_REGISTER", "Step 2: Saving to backend with userId: $userId")
                            
                            // STEP 2: Save user to Spring Boot backend (public.users table)
                            // This is what the web app does: fetch("http://localhost:8080/api/users/register", ...)
                            val backendUser = BackendUser(userId, email, name, role, mobile)
                            
                            RetrofitClient.backendInstance.registerUser(backendUser).enqueue(object : retrofit2.Callback<JsonObject> {
                                override fun onResponse(call: retrofit2.Call<JsonObject>, backendResponse: retrofit2.Response<JsonObject>) {
                                    btnRegister.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    
                                    if (backendResponse.isSuccessful) {
                                        Log.d("LODGIO_REGISTER", "Step 2 SUCCESS: User saved to public.users table")
                                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()
                                        finish()
                                    } else {
                                        val errBody = backendResponse.errorBody()?.string()
                                        Log.e("LODGIO_REGISTER", "Step 2 FAILED (${backendResponse.code()}): $errBody")
                                        // Auth user was created but backend save failed - still let them proceed
                                        Toast.makeText(this@RegisterActivity, "Account created but profile sync failed. Please Login.", Toast.LENGTH_LONG).show()
                                        finish()
                                    }
                                }

                                override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                                    btnRegister.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    Log.e("LODGIO_REGISTER", "Step 2 NETWORK ERROR: ${t.message}")
                                    // Auth was created successfully, backend just not reachable
                                    Toast.makeText(this@RegisterActivity, "Account created! Backend sync failed (is Spring Boot running?). Please Login.", Toast.LENGTH_LONG).show()
                                    finish()
                                }
                            })
                        } else {
                            btnRegister.isEnabled = true
                            progressBar.visibility = View.GONE
                            Log.e("LODGIO_REGISTER", "No user ID in Supabase response: $body")
                            Toast.makeText(this@RegisterActivity, "Unexpected response from server", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        btnRegister.isEnabled = true
                        progressBar.visibility = View.GONE
                        val errorBody = response.errorBody()?.string()
                        Log.e("LODGIO_REGISTER", "Step 1 FAILED (${response.code()}): $errorBody")
                        
                        when (response.code()) {
                            400 -> Toast.makeText(this@RegisterActivity, "Bad Request: $errorBody", Toast.LENGTH_LONG).show()
                            422 -> Toast.makeText(this@RegisterActivity, "Email already registered", Toast.LENGTH_LONG).show()
                            429 -> Toast.makeText(this@RegisterActivity, "Too many requests. Wait and try again.", Toast.LENGTH_LONG).show()
                            500 -> Toast.makeText(this@RegisterActivity, "Server error. Try again later.", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this@RegisterActivity, "Registration failed (${response.code()}): $errorBody", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                    btnRegister.isEnabled = true
                    progressBar.visibility = View.GONE
                    Log.e("LODGIO_REGISTER", "Step 1 NETWORK ERROR", t)
                    Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        btnGoToLogin.setOnClickListener {
            finish()
        }
    }
}
