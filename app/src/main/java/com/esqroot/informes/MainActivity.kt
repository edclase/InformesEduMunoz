package com.esqroot.informes

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var spCity: Spinner
    private lateinit var btnGenerateReport: Button
    private lateinit var btnExportPdf: Button
    private lateinit var reportContainer: LinearLayout
    private lateinit var txtSelectedCity: TextView
    private lateinit var txtOfferCount: TextView
    private lateinit var txtTotalSalary: TextView
    private lateinit var txtAverageSalary: TextView
    private lateinit var txtMaxSalary: TextView
    private lateinit var barValencia: ProgressBar
    private lateinit var barMadrid: ProgressBar
    private lateinit var barAlicante: ProgressBar
    private lateinit var txtCountValencia: TextView
    private lateinit var txtCountMadrid: TextView
    private lateinit var txtCountAlicante: TextView
    private lateinit var offersContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep the content away from the status and navigation bars (edge-to-edge on Android 15+).
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val padding = (24 * resources.displayMetrics.density).toInt()
            view.setPadding(bars.left + padding, bars.top + padding, bars.right + padding, bars.bottom + padding)
            insets
        }

        spCity = findViewById(R.id.spCity)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        btnExportPdf = findViewById(R.id.btnExportPdf)
        reportContainer = findViewById(R.id.reportContainer)
        txtSelectedCity = findViewById(R.id.txtSelectedCity)
        txtOfferCount = findViewById(R.id.txtOfferCount)
        txtTotalSalary = findViewById(R.id.txtTotalSalary)
        txtAverageSalary = findViewById(R.id.txtAverageSalary)
        txtMaxSalary = findViewById(R.id.txtMaxSalary)
        barValencia = findViewById(R.id.barValencia)
        barMadrid = findViewById(R.id.barMadrid)
        barAlicante = findViewById(R.id.barAlicante)
        txtCountValencia = findViewById(R.id.txtCountValencia)
        txtCountMadrid = findViewById(R.id.txtCountMadrid)
        txtCountAlicante = findViewById(R.id.txtCountAlicante)
        offersContainer = findViewById(R.id.offersContainer)

        setUpSpinner()
    }

    private fun setUpSpinner() {
        val cities = arrayOf("All", "Valencia", "Madrid", "Alicante")

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCity.adapter = adapter
    }
}
