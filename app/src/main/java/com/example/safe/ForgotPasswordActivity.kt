package com.example.safe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etForgotEmail)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.pbForgot)

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (email.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (newPass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (!newPass.any { it.isUpperCase() }) {
                Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            } else if (!newPass.any { !it.isLetterOrDigit() }) {
                Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show()
            } else if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                if (dbHelper.checkEmail(email)) {
                    btnReset.isEnabled = false
                    btnReset.text = ""
                    progressBar.visibility = android.view.View.VISIBLE
                    sendOtpAndVerify(email, newPass, btnReset, progressBar)
                } else {
                    Toast.makeText(this, "Email not registered.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<TextView>(R.id.tvBackToLogin).setOnClickListener {
            finish()
        }
    }

    private fun sendOtpAndVerify(email: String, newPass: String, button: Button, progressBar: android.widget.ProgressBar) {
        val request = OtpRequest(email)
        RetrofitClient.apiService.sendOtp(request).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                button.isEnabled = true
                button.text = "Reset Password"
                progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    intent.putExtra("IS_FORGOT_PASSWORD", true)
                    intent.putExtra("NEW_PASSWORD", newPass)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Failed to send OTP. Try again.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                button.isEnabled = true
                button.text = "Reset Password"
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@ForgotPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
