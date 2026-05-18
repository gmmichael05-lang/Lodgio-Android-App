package com.example.testapplication.features.auth.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.features.auth.RegisterContract
import com.example.testapplication.features.auth.model.RegisterRequest
import com.example.testapplication.features.auth.model.SupabaseUserMetadata
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Register Presenter — handles two-step registration:
 * Step 1: Supabase auth signup
 * Step 2: Save user to Spring Boot backend
 */
class RegisterPresenter(
    private var view: RegisterContract.View?
) : RegisterContract.Presenter {

    override fun register(
        role: String,
        fullname: String,
        email: String,
        mobileNumber: String,
        password: String,
        confirmPassword: String
    ) {
        // Validation
        if (fullname.isEmpty() || email.isEmpty() || mobileNumber.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            view?.showError("Please fill all fields.")
            return
        }
        if (password != confirmPassword) {
            view?.showError("Passwords do not match.")
            return
        }
        if (password.length < 6) {
            view?.showError("Password must be at least 6 characters.")
            return
        }

        view?.showLoading()

        // Step 1: Supabase signup
        val metadata = SupabaseUserMetadata(fullname, role, mobileNumber)
        val request = RegisterRequest(email, password, metadata)

        RetrofitClient.authApi.register(request = request).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val userId = try {
                        body.getAsJsonObject("user")?.get("id")?.let { if (it.isJsonNull) null else it.asString }
                            ?: body.get("id")?.let { if (it.isJsonNull) null else it.asString }
                    } catch (_: Exception) { null }

                    if (userId != null) {
                        saveToBackend(userId, email, fullname, role, mobileNumber)
                    } else {
                        view?.hideLoading()
                        view?.showError("Unexpected response from server")
                    }
                } else {
                    view?.hideLoading()
                    when (response.code()) {
                        422 -> view?.showError("Email already registered")
                        429 -> view?.showError("Too many requests. Wait and try again.")
                        else -> view?.showError("Registration failed (${response.code()})")
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    private fun saveToBackend(
        userId: String, email: String, fullname: String, role: String, mobileNumber: String
    ) {
        val backendPayload = JsonObject().apply {
            addProperty("id", userId)
            addProperty("email", email)
            addProperty("fullname", fullname)
            addProperty("role", role)
            addProperty("mobileNumber", mobileNumber)
        }

        RetrofitClient.userApi.registerUser(backendPayload).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful) {
                    view?.onRegisterSuccess()
                } else {
                    // Auth user was created but backend save failed — let them proceed
                    view?.onRegisterSuccess()
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                // Auth created, backend not reachable — still proceed
                view?.onRegisterSuccess()
            }
        })
    }

    override fun onDestroy() {
        view = null
    }
}
