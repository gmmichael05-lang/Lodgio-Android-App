package com.example.testapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.testapplication.R
import com.example.testapplication.api.RetrofitClient
import com.example.testapplication.models.ChangePasswordRequest
import com.example.testapplication.models.SupabaseUser
import com.example.testapplication.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val sessionManager = SessionManager(this)
        val etCurrentPwd = findViewById<EditText>(R.id.etCurrentPwd)
        val etNewPwd = findViewById<EditText>(R.id.etNewPwd)
        val etNewPwdConfirm = findViewById<EditText>(R.id.etNewPwdConfirm)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnChangePassword.setOnClickListener {
            // Note: Supabase doesn't natively require you to post the "current" password for the v1/user route if you are authenticated via Bearer,
            // but we can leave the frontend validation in for UX layout purposes.
            val current = etCurrentPwd.text.toString()
            val newPwd = etNewPwd.text.toString()
            val confirm = etNewPwdConfirm.text.toString()

            if (current.isEmpty() || newPwd.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPwd != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = sessionManager.getToken() ?: return@setOnClickListener

            btnChangePassword.isEnabled = false
            progressBar.visibility = View.VISIBLE

            val request = ChangePasswordRequest(newPwd)
            RetrofitClient.instance.changePassword(token = "Bearer $token", request = request).enqueue(object : retrofit2.Callback<SupabaseUser> {
                override fun onResponse(call: retrofit2.Call<SupabaseUser>, response: retrofit2.Response<SupabaseUser>) {
                    btnChangePassword.isEnabled = true
                    progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Password Changed Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<SupabaseUser>, t: Throwable) {
                    btnChangePassword.isEnabled = true
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ChangePasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
