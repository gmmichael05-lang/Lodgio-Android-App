package com.example.testapplication.features.profile.view

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.LodgioApp
import com.example.testapplication.R
import com.example.testapplication.core.extensions.*
import com.example.testapplication.features.profile.ChangePasswordContract
import com.example.testapplication.features.profile.presenter.ChangePasswordPresenter

class ChangePasswordActivity : AppCompatActivity(), ChangePasswordContract.View {

    private var presenter: ChangePasswordContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        presenter = ChangePasswordPresenter(this, LodgioApp.instance.sessionManager)

        val etNewPwd = findViewById<EditText>(R.id.etNewPwd)
        val etNewPwdConfirm = findViewById<EditText>(R.id.etNewPwdConfirm)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnChangePassword).setOnClickListener {
            presenter?.changePassword(etNewPwd.textString(), etNewPwdConfirm.textString())
        }
    }

    override fun showLoading() {
        findViewById<Button>(R.id.btnChangePassword).isEnabled = false
        findViewById<ProgressBar>(R.id.progressBar).visible()
    }
    override fun hideLoading() {
        findViewById<Button>(R.id.btnChangePassword).isEnabled = true
        findViewById<ProgressBar>(R.id.progressBar).gone()
    }
    override fun showError(message: String) { toast(message) }
    override fun onPasswordChanged() {
        toast("Password changed successfully!")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.onDestroy()
    }
}
