package com.example.testapplication.features.auth.presenter

import com.example.testapplication.core.network.RetrofitClient
import com.example.testapplication.core.session.SessionManager
import com.example.testapplication.features.auth.LoginContract
import com.example.testapplication.features.auth.model.AuthResponse
import com.example.testapplication.features.auth.model.LoginRequest
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Login Presenter — contains ALL business logic for login.
 * Does NOT know about Android (Context, Toast, etc.).
 */
class LoginPresenter(
    private var view: LoginContract.View?,
    private val sessionManager: SessionManager
) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            view?.showError("Please fill in all fields.")
            return
        }

        view?.showLoading()

        // Step 1: Supabase Auth login
        val request = LoginRequest(email, password)
        RetrofitClient.authApi.login(request = request).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    val token = authBody.access_token
                    val role = authBody.user?.user_metadata?.role ?: "GUEST"
                    val userId = authBody.user?.id

                    if (token != null) {
                        sessionManager.saveToken(token)
                        sessionManager.saveRole(role)
                        sessionManager.saveEmail(email)
                        if (userId != null) sessionManager.saveUserId(userId)

                        // Step 2: Fetch backend user info (matches web app's loginGetUser)
                        fetchBackendUser(email, role)
                    } else {
                        view?.hideLoading()
                        view?.showError("Login failed: no token received")
                    }
                } else {
                    view?.hideLoading()
                    when (response.code()) {
                        400 -> view?.showError("Bad Request: Invalid format")
                        401 -> view?.showError("Invalid credentials")
                        500 -> view?.showError("Server error. Try again later.")
                        else -> view?.showError("Login failed: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                view?.hideLoading()
                view?.showError("Network error: ${t.message}")
            }
        })
    }

    private fun fetchBackendUser(email: String, supabaseRole: String) {
        RetrofitClient.userApi.getUserByEmail(email).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                view?.hideLoading()
                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!
                    val backendRole = try { userData.get("role")?.let { if (it.isJsonNull) null else it.asString } } catch (_: Exception) { null } ?: supabaseRole
                    val fullname = try { userData.get("fullname")?.let { if (it.isJsonNull) null else it.asString } } catch (_: Exception) { null } ?: ""
                    val backendId = try { userData.get("id")?.let { if (it.isJsonNull) null else it.asString } } catch (_: Exception) { null }

                    sessionManager.saveRole(backendRole)
                    sessionManager.saveFullname(fullname)
                    if (backendId != null) sessionManager.saveUserId(backendId)

                    view?.onLoginSuccess(backendRole)
                } else {
                    // Backend user not found — proceed with Supabase role
                    view?.onLoginSuccess(supabaseRole)
                }
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                view?.hideLoading()
                // Backend unavailable — still proceed with login
                view?.onLoginSuccess(supabaseRole)
            }
        })
    }

    override fun onDestroy() {
        view = null
    }
}
