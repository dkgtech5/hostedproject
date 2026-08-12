package com.example.safe

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class ScanningActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvPercentage: TextView
    private lateinit var ivChecks: List<ImageView>
    private lateinit var tvChecks: List<TextView>
    private var apiResponse: ScanResponse? = null
    private var isNavigating = false
    private var isAnimationFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanning)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tvAnalyzingTitle)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressBar = findViewById(R.id.progressBarScanning)
        tvPercentage = findViewById(R.id.tvProgressPercentage)

        ivChecks = listOf(
            findViewById(R.id.ivCheckHttps),
            findViewById(R.id.ivCheckDomain),
            findViewById(R.id.ivCheckRedirects),
            findViewById(R.id.ivCheckUrlLength),
            findViewById(R.id.ivCheckContent)
        )

        tvChecks = listOf(
            findViewById(R.id.tvCheckHttps),
            findViewById(R.id.tvCheckDomain),
            findViewById(R.id.tvCheckRedirects),
            findViewById(R.id.tvCheckUrlLength),
            findViewById(R.id.tvCheckContent)
        )

        val url = intent.getStringExtra("URL") ?: ""
        if (url.isNotEmpty()) {
            performScan(url)
        } else {
            Toast.makeText(this, "URL is missing", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun performScan(url: String) {
        val request = ScanRequest(url)
        
        RetrofitClient.apiService.predictUrl(request).enqueue(object : Callback<ScanResponse> {
            override fun onResponse(call: Call<ScanResponse>, response: Response<ScanResponse>) {
                if (response.isSuccessful) {
                    apiResponse = response.body()
                    android.util.Log.d("ScanningActivity", "API Success: ${apiResponse?.prediction}")
                    
                    // If animation is already done, navigate now
                    if (isAnimationFinished) {
                        apiResponse?.let { navigateToResults(it) }
                    }
                } else {
                    android.util.Log.e("ScanningActivity", "API Error: ${response.code()}")
                    Toast.makeText(this@ScanningActivity, "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ScanResponse>, t: Throwable) {
                android.util.Log.e("ScanningActivity", "API Failure: ${t.message}")
                Toast.makeText(this@ScanningActivity, "Connection Failed", Toast.LENGTH_LONG).show()
                finish()
            }
        })

        // Run the 5-second animation
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 5000 
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            progressBar.progress = progress
            tvPercentage.text = String.format(Locale.getDefault(), "%d%%", progress)
            updateChecklist(progress)
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                isAnimationFinished = true
                if (apiResponse != null) {
                    navigateToResults(apiResponse!!)
                } else {
                    findViewById<TextView>(R.id.tvAnalyzingSubtitle).text = "Finalizing analysis..."
                }
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private fun navigateToResults(data: ScanResponse) {
        if (isNavigating || isFinishing) return
        isNavigating = true
        
        android.util.Log.d("ScanningActivity", "Navigating to results for: ${data.url}")
        
        val isSafe = data.prediction == "LEGITIMATE"
        val riskScore = (data.phishingProbability * 100).toInt()

        // Save to database
        try {
            val dbHelper = DatabaseHelper(this)
            dbHelper.saveScan(data.url, isSafe, riskScore.toDouble())
        } catch (e: Exception) {
            android.util.Log.e("ScanningActivity", "DB Error: ${e.message}")
        }

        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("URL", data.url)
            putExtra("IS_SAFE", isSafe)
            putExtra("RISK_SCORE", riskScore)
            putExtra("LEGITIMATE_PROB", data.legitimateProbability)
            putExtra("PHISHING_PROB", data.phishingProbability)
            
            val checks = data.securityChecks
            putExtra("HTTPS", checks?.httpsEnabled ?: data.url.startsWith("https", ignoreCase = true))
            putExtra("DOMAIN", checks?.noIpInUrl ?: isSafe)
            putExtra("REDIRECT", checks?.noSuspiciousRedirect ?: isSafe)
            putExtra("STRUCTURE", !(checks?.shortenedUrl ?: !isSafe))
        }
        
        startActivity(intent)
        finish()
    }

    private fun updateChecklist(progress: Int) {
        val milestones = listOf(20, 40, 60, 80, 100)
        for (i in milestones.indices) {
            if (progress >= milestones[i]) {
                ivChecks[i].setColorFilter(Color.parseColor("#10B981")) 
                tvChecks[i].setTextColor(Color.parseColor("#0F172A")) 
            }
        }
    }
}
