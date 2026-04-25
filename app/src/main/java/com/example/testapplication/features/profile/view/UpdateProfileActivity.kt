package com.example.testapplication.features.profile.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.auth.model.SupabaseUser
import com.example.testapplication.features.auth.model.SupabaseUserMetadata
import com.example.testapplication.features.auth.model.UpdateProfileRequest
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        val session = LodgioApp.instance.sessionManager
        val etName = findViewById<EditText>(R.id.etName)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnUpdate.setOnClickListener {
            val name = etName.textString()
            val mobile = etMobile.textString()

            if (name.isEmpty() || mobile.isEmpty()) {
                toast("Fields cannot be empty.")
                return@setOnClickListener
            }

            val token = session.getToken() ?: return@setOnClickListener
            val role = session.getRole()

            btnUpdate.isEnabled = false
            progressBar.visible()

            // Step 1: Update Supabase
            val metadata = SupabaseUserMetadata(name, role, mobile)
            val request = UpdateProfileRequest(metadata)

            RetrofitClient.authApi.updateProfile(token = "Bearer $token", request = request)
                .enqueue(object : Callback<SupabaseUser> {
                    override fun onResponse(call: Call<SupabaseUser>, response: Response<SupabaseUser>) {
                        if (response.isSuccessful) {
                            session.saveFullname(name)
                            // Step 2: Sync to backend
                            val userId = session.getUserId()
                            if (userId != null) {
                                val payload = JsonObject().apply {
                                    addProperty("id", userId)
                                    addProperty("email", session.getEmail() ?: "")
                                    addProperty("fullname", name)
                                    addProperty("role", role)
                                    addProperty("mobileNumber", mobile)
                                }
                                RetrofitClient.userApi.updateUser(userId, payload).enqueue(object : Callback<JsonObject> {
                                    override fun onResponse(call: Call<JsonObject>, res: Response<JsonObject>) {
                                        btnUpdate.isEnabled = true
                                        progressBar.gone()
                                        toast("Profile updated!")
                                        finish()
                                    }
                                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                                        btnUpdate.isEnabled = true
                                        progressBar.gone()
                                        toast("Profile updated (backend sync failed)")
                                        finish()
                                    }
                                })
                            } else {
                                btnUpdate.isEnabled = true
                                progressBar.gone()
                                toast("Profile updated!")
                                finish()
                            }
                        } else {
                            btnUpdate.isEnabled = true
                            progressBar.gone()
                            toast("Update failed: ${response.message()}")
                        }
                    }
                    override fun onFailure(call: Call<SupabaseUser>, t: Throwable) {
                        btnUpdate.isEnabled = true
                        progressBar.gone()
                        toast("Network error: ${t.message}")
                    }
                })
        }
    }
}
