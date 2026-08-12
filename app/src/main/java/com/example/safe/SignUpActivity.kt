package com.example.safe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUpActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.pbSignUp)

        btnSignUp.setOnClickListener {
            val fullName = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (fullName.length < 4) {
                Toast.makeText(this, "Name must be at least 4 characters", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if (!password.any { it.isUpperCase() }) {
                Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            } else if (!password.any { !it.isLetterOrDigit() }) {
                Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show()
            } else {
                if (dbHelper.checkEmail(email)) {
                    Toast.makeText(this, "Email already registered.", Toast.LENGTH_SHORT).show()
                } else {
                    btnSignUp.isEnabled = false
                    btnSignUp.text = ""
                    progressBar.visibility = android.view.View.VISIBLE
                    val result = dbHelper.registerUser(fullName, email, password)
                    if (result != -1L) {
                        Log.d(TAG, "User registered locally: $fullName")
                        sendOtpAndVerify(email, btnSignUp, progressBar)
                    } else {
                        btnSignUp.isEnabled = true
                        btnSignUp.text = getString(R.string.sign_up)
                        progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<TextView>(R.id.tvLogin).setOnClickListener {
            finish() // Go back to Login
        }
    }

    private fun sendOtpAndVerify(email: String, button: Button, progressBar: android.widget.ProgressBar) {
        val request = OtpRequest(email)
        RetrofitClient.apiService.sendOtp(request).enqueue(object : retrofit2.Callback<OtpResponse> {
            override fun onResponse(call: retrofit2.Call<OtpResponse>, response: retrofit2.Response<OtpResponse>) {
                button.isEnabled = true
                button.text = getString(R.string.sign_up)
                progressBar.visibility = android.view.View.GONE

                if (response.isSuccessful) {
                    Toast.makeText(this@SignUpActivity, "OTP sent to your email", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignUpActivity, OtpVerificationActivity::class.java)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SignUpActivity, "Failed to send OTP", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<OtpResponse>, t: Throwable) {
                button.isEnabled = true
                button.text = getString(R.string.sign_up)
                progressBar.visibility = android.view.View.GONE
                Toast.makeText(this@SignUpActivity, "Connection to server failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}