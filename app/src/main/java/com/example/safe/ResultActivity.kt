package com.example.safe

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)
        
        android.util.Log.d("ResultActivity", "Activity created")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val url = intent.getStringExtra("URL") ?: "https://example.com"
        val isSafe = intent.getBooleanExtra("IS_SAFE", true)
        val riskScore = intent.getIntExtra("RISK_SCORE", 0)
        val legitProb = intent.getDoubleExtra("LEGITIMATE_PROB", 0.0)
        val phishProb = intent.getDoubleExtra("PHISHING_PROB", 0.0)
        
        android.util.Log.d("ResultActivity", "Data: url=$url, safe=$isSafe, score=$riskScore, legit=$legitProb, phish=$phishProb")
        
        val https = intent.getBooleanExtra("HTTPS", true)
        val domain = intent.getBooleanExtra("DOMAIN", true)
        val redirect = intent.getBooleanExtra("REDIRECT", true)
        val structure = intent.getBooleanExtra("STRUCTURE", true)

        setupUI(url, isSafe, riskScore, legitProb, phishProb, https, domain, redirect, structure)
    }

    private fun setupUI(url: String, isSafe: Boolean, riskScore: Int, legitProb: Double, phishProb: Double, https: Boolean, domain: Boolean, redirect: Boolean, structure: Boolean) {
        android.util.Log.d("ResultActivity", "Setting up UI...")
        val ivResultIcon = findViewById<ImageView>(R.id.ivResultIcon)
        val tvResultStatus = findViewById<TextView>(R.id.tvResultStatus)
        val tvResultSubtitle = findViewById<TextView>(R.id.tvResultSubtitle)
        val tvRiskPercentage = findViewById<TextView>(R.id.tvRiskPercentage)
        val pbRisk = findViewById<ProgressBar>(R.id.pbRisk)
        val tvLegitProb = findViewById<TextView?>(R.id.tvLegitProb)
        val tvPhishProb = findViewById<TextView?>(R.id.tvPhishProb)
        val tvResultUrl = findViewById<TextView>(R.id.tvResultUrl)
        val tvDetailsHeader = findViewById<TextView>(R.id.tvDetailsHeader)
        val llChecklist = findViewById<LinearLayout>(R.id.llResultChecklist)
        val btnPrimary = findViewById<Button>(R.id.btnPrimaryAction)
        val btnSecondary = findViewById<Button>(R.id.btnSecondaryAction)
        val btnCopyUrl = findViewById<ImageView>(R.id.btnCopyUrl)

        tvResultUrl.text = url
        tvRiskPercentage.text = "$riskScore%"
        pbRisk.progress = riskScore
        
        tvLegitProb?.text = String.format(java.util.Locale.getDefault(), "Legitimate: %.1f%%", legitProb * 100)
        tvPhishProb?.text = String.format(java.util.Locale.getDefault(), "Phishing: %.1f%%", phishProb * 100)

        android.util.Log.d("ResultActivity", "UI Basic data set. isSafe=$isSafe")

        if (isSafe) {
            ivResultIcon.setImageResource(R.drawable.ic_check_circle)
            ivResultIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_safe_green))
            tvResultStatus.text = "SAFE"
            tvResultStatus.setTextColor(ContextCompat.getColor(this, R.color.status_safe_green))
            tvResultSubtitle.text = "This website appears to be legitimate"
            tvRiskPercentage.setTextColor(ContextCompat.getColor(this, R.color.status_safe_green))
            tvDetailsHeader.text = "Security Checks"
            
            btnPrimary.visibility = View.VISIBLE
            btnPrimary.text = "Visit Website"
            btnPrimary.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visit, 0, 0, 0)
            btnSecondary.text = "Scan Another"
            btnCopyUrl.visibility = View.VISIBLE

            addChecklistItem(llChecklist, "HTTPS / SSL Enabled", https)
            addChecklistItem(llChecklist, "Valid Domain (No IP)", domain)
            addChecklistItem(llChecklist, "No Suspicious Redirects", redirect)
            addChecklistItem(llChecklist, "Standard URL Structure", structure)
        } else {
            ivResultIcon.setImageResource(R.drawable.ic_error)
            ivResultIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_phishing_red))
            tvResultStatus.text = "WARNING"
            tvResultStatus.setTextColor(ContextCompat.getColor(this, R.color.status_phishing_red))
            tvResultSubtitle.text = "This website is likely phishing!"
            tvRiskPercentage.setTextColor(ContextCompat.getColor(this, R.color.status_phishing_red))
            tvDetailsHeader.text = "Analysis Results"
            
            btnPrimary.visibility = View.GONE
            btnSecondary.text = "Go Back"
            btnCopyUrl.visibility = View.GONE

            addChecklistItem(llChecklist, "SSL Check Passed", https)
            addChecklistItem(llChecklist, "Domain Trust Level", domain)
            addChecklistItem(llChecklist, "Redirect Check", redirect)
            addChecklistItem(llChecklist, "Url Pattern Check", structure)
        }

        btnSecondary.setOnClickListener {
            finish()
        }

        val visitWebsiteAction = View.OnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open website", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<View>(R.id.btnCopyUrl).setOnClickListener(visitWebsiteAction)

        btnPrimary.setOnClickListener {
            if (isSafe) {
                visitWebsiteAction.onClick(it)
            }
        }
    }

    private fun addChecklistItem(parent: LinearLayout, text: String, isSuccess: Boolean) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_result_check, parent, false)
        val icon = itemView.findViewById<ImageView>(R.id.ivCheckIcon)
        val tv = itemView.findViewById<TextView>(R.id.tvCheckText)

        tv.text = text
        if (isSuccess) {
            icon.setImageResource(R.drawable.ic_check_circle)
            icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
        } else {
            icon.setImageResource(R.drawable.ic_error)
            icon.imageTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
        }
        parent.addView(itemView)
    }
}