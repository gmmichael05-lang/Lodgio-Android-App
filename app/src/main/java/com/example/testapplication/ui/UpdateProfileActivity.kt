package com.example.testapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.api.RetrofitClient
import com.example.testapplication.models.BackendUser
import com.example.testapplication.models.SupabaseUser
import com.example.testapplication.models.SupabaseUserMetadata
import com.example.testapplication.models.UpdateProfileRequest
import com.example.testapplication.utils.SessionManager
import com.google.gson.JsonObject

class UpdateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        val sessionManager = SessionManager(this)
        val etName = findViewById<EditText>(R.id.etName)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnUpdate.setOnClickListener {
            val name = etName.text.toString().trim()
            val mobile = etMobile.text.toString().trim()

            if (name.isEmpty() || mobile.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = sessionManager.getToken() ?: return@setOnClickListener
            val role = sessionManager.getRole() ?: "GUEST"

            btnUpdate.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val metadata = SupabaseUserMetadata(name, role, mobile)
            val request = UpdateProfileRequest(metadata)
            
            // Step 1: Update in Supabase
            RetrofitClient.instance.updateProfile(token = "Bearer $token", request = request).enqueue(object : retrofit2.Callback<SupabaseUser> {
                override fun onResponse(call: retrofit2.Call<SupabaseUser>, response: retrofit2.Response<SupabaseUser>) {
                    if (response.isSuccessful) {
                        // Step 2: Sync update to custom Spring Boot backend
                        val userId = sessionManager.getUserId()
                        if (userId != null) {
                            // Dummy email, Backend doesn't strictly need email to update name/mobile
                            val backendUser = BackendUser(userId, "", name, role, mobile)
                            
                            RetrofitClient.backendInstance.updateBackendProfile(userId, backendUser).enqueue(object : retrofit2.Callback<JsonObject> {
                                override fun onResponse(call: retrofit2.Call<JsonObject>, backendRes: retrofit2.Response<JsonObject>) {
                                    btnUpdate.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    if (backendRes.isSuccessful) {
                                        Toast.makeText(this@UpdateProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                                        finish() // Reloads automatically on Profile UI
                                    } else {
                                        Toast.makeText(this@UpdateProfileActivity, "Profile updated but backend sync failed", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }

                                override fun onFailure(call: retrofit2.Call<JsonObject>, t: Throwable) {
                                    btnUpdate.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this@UpdateProfileActivity, "Backend Network Error", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            })
                        } else {
                            btnUpdate.isEnabled = true
                            progressBar.visibility = View.GONE
                            Toast.makeText(this@UpdateProfileActivity, "Profile Updated (Supabase Only)", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        btnUpdate.isEnabled = true
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@UpdateProfileActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<SupabaseUser>, t: Throwable) {
                    btnUpdate.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@UpdateProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
