package com.example.testapplication.features.auth.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.admin.view.AdminDashboardActivity
import com.example.testapplication.features.auth.LoginContract
import com.example.testapplication.features.auth.presenter.LoginPresenter
import com.example.testapplication.features.listing.view.GuestDashboardActivity
import com.example.testapplication.features.listing.view.HostDashboardActivity
import android.widget.*

/**
 * Login Activity — View layer of MVP.
 * Displays UI, delegates all logic to LoginPresenter.
 */
class LoginActivity : AppCompatActivity(), LoginContract.View {

    private var presenter: LoginContract.Presenter? = null

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToRegister: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val session = LodgioApp.instance.sessionManager
        // Auto-login if session exists
        if (session.isLoggedIn()) {
            try {
                navigateByRole(session.getRole())
                finish()
                return
            } catch (e: Exception) {
                // If navigation fails, clear stale session and show login
                session.clear()
            }
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)
        progressBar = findViewById(R.id.progressBar)

        presenter = LoginPresenter(this, session)

        btnLogin.setOnClickListener {
            presenter?.login(etEmail.textString(), etPassword.textString())
        }

        btnGoToRegister.setOnClickListener {
            startActivity<RegisterActivity>()
        }
    }

    override fun showLoading() {
        btnLogin.isEnabled = false
        progressBar.visible()
    }

    override fun hideLoading() {
        btnLogin.isEnabled = true
        progressBar.gone()
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun onLoginSuccess(role: String) {
        toast("Login Successful")
        navigateByRole(role)
        finish()
    }

    private fun navigateByRole(role: String) {
        when (role.uppercase()) {
            "HOST" -> startActivityClearTask<HostDashboardActivity>()
            "ADMIN" -> startActivityClearTask<AdminDashboardActivity>()
            else -> startActivityClearTask<GuestDashboardActivity>()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
