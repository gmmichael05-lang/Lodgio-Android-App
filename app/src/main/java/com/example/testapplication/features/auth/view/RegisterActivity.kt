package com.example.testapplication.features.auth.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.auth.RegisterContract
import com.example.testapplication.features.auth.presenter.RegisterPresenter

/**
 * Register Activity — View layer of MVP.
 */
class RegisterActivity : AppCompatActivity(), RegisterContract.View {

    private var presenter: RegisterContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        presenter = RegisterPresenter(this)

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
            presenter?.register(
                role = role,
                fullname = etName.textString(),
                email = etEmail.textString(),
                mobileNumber = etMobile.textString(),
                password = etPassword.textString(),
                confirmPassword = etPasswordConfirm.textString()
            )
        }

        btnGoToLogin.setOnClickListener { finish() }
    }

    override fun showLoading() {
        findViewById<Button>(R.id.btnRegister).isEnabled = false
        findViewById<ProgressBar>(R.id.progressBar).visible()
    }

    override fun hideLoading() {
        findViewById<Button>(R.id.btnRegister).isEnabled = true
        findViewById<ProgressBar>(R.id.progressBar).gone()
    }

    override fun showError(message: String) {
        toast(message)
    }

    override fun onRegisterSuccess() {
        toast("Account created! Please log in.", long = true)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
