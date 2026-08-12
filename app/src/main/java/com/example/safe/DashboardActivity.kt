package com.example.safe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashboardActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        
        dbHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            findViewById<View>(R.id.bottomNav).setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBackDashboard).setOnClickListener {
            finish()
        }

        setupNavigation()
        highlightStats()
        updateStats()
    }

    private fun highlightStats() {
        findViewById<ImageView>(R.id.ivNavStats).setColorFilter(Color.parseColor("#3B82F6"))
        findViewById<TextView>(R.id.tvNavStats).setTextColor(Color.parseColor("#3B82F6"))
    }

    private fun updateStats() {
        val stats = dbHelper.getScanStats()
        val total = stats["total"] ?: 0
        val safe = stats["safe"] ?: 0
        val threats = stats["threats"] ?: 0
        
        findViewById<TextView>(R.id.tvDashTotal).text = total.toString()
        findViewById<TextView>(R.id.tvDashSafe).text = safe.toString()
        findViewById<TextView>(R.id.tvDashThreats).text = threats.toString()
        
        val risk = if (total > 0) (threats * 100) / total else 0
        findViewById<TextView>(R.id.tvDashRisk).text = getString(R.string.percent_format, risk)

        // Update Circular Chart
        val safePercent = if (total > 0) (safe * 100) / total else 0
        findViewById<ProgressBar>(R.id.pbSafeRatio).progress = safePercent
        findViewById<TextView>(R.id.tvSafePercent).text = getString(R.string.percent_format, safePercent)

        // Update Bar Chart
        updateBarChart(total)
    }

    private fun updateBarChart(total: Int) {
        val bars = listOf(
            findViewById<View>(R.id.bar1),
            findViewById<View>(R.id.bar2),
            findViewById<View>(R.id.bar3),
            findViewById<View>(R.id.bar4),
            findViewById<View>(R.id.bar5),
            findViewById<View>(R.id.bar6),
            findViewById<View>(R.id.bar7)
        )

        // Max possible height in dp is approx 130
        val maxHeightPx = (130 * resources.displayMetrics.density).toInt()
        val baseHeightPx = (20 * resources.displayMetrics.density).toInt()
        
        for (bar in bars) {
            val randomVal = if (total > 0) (1..total).random() else 0
            val height = if (total > 0) {
                baseHeightPx + (randomVal.toFloat() / total * (maxHeightPx - baseHeightPx)).toInt()
            } else {
                baseHeightPx
            }
            
            val params = bar.layoutParams
            params.height = height
            bar.layoutParams = params
        }
    }

    private fun setupNavigation() {
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<LinearLayout>(R.id.navHistory).setOnClickListener {
            startActivity(Intent(this, ScanHistoryActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.fabScan).setOnClickListener {
            startActivity(Intent(this, ScanWebsiteActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }
    }
}
