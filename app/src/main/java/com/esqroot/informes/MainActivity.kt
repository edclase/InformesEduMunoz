package com.esqroot.informes

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import kotlin.math.min

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

    private val offers = ArrayList<Offer>()
    private val createPdfRequestCode = 100

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
        offers.addAll(loadOffers())

        btnGenerateReport.setOnClickListener {
            val selectedCity = spCity.selectedItem.toString()
            val filteredOffers = filterOffersByCity(selectedCity)
            showResult(selectedCity, filteredOffers)
        }

        btnExportPdf.setOnClickListener {
            val selectedCity = spCity.selectedItem.toString()
            val filteredOffers = filterOffersByCity(selectedCity)

            showResult(selectedCity, filteredOffers)

            // post waits until Android has measured the report before it is drawn into the PDF.
            reportContainer.post {
                openSavePdfPicker()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == createPdfRequestCode && resultCode == RESULT_OK) {
            val pdfUri = data?.data

            if (pdfUri != null) {
                exportReportPdf(pdfUri)
            }
        }
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

    private fun loadOffers(): ArrayList<Offer> {
        val offerList = ArrayList<Offer>()

        // Read the JSON file from assets and turn it into a JSONArray.
        val jsonText = assets.open("offers.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonText)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)

            val offer = Offer(
                jsonObject.getInt("id"),
                jsonObject.getString("position"),
                jsonObject.getString("company"),
                jsonObject.getString("city"),
                jsonObject.getInt("salary")
            )

            offerList.add(offer)
        }

        return offerList
    }

    private fun filterOffersByCity(city: String): List<Offer> {
        // filter returns a new list with only the offers from the chosen city.
        val result = if (city == "All") {
            offers
        } else {
            offers.filter {
                it.city == city
            }
        }

        return result
    }

    private fun showResult(city: String, filteredOffers: List<Offer>) {
        // Report figures calculated from the filtered list.
        val offerCount = filteredOffers.size
        val totalSalary = filteredOffers.sumOf { it.salary }
        val averageSalary = if (filteredOffers.size > 0) {
            filteredOffers.map { it.salary }.average()
        } else {
            0.0
        }
        val maxSalary = filteredOffers.maxOfOrNull { it.salary } ?: 0

        txtSelectedCity.text = "Selected city: $city"
        txtOfferCount.text = "Number of offers: $offerCount"
        txtTotalSalary.text = "Total salary: ${formatSalary(totalSalary)}"
        txtAverageSalary.text = "Average salary: ${formatSalary(averageSalary.toInt())}"
        txtMaxSalary.text = "Maximum salary: ${formatSalary(maxSalary)}"

        showChart(filteredOffers)

        offersContainer.removeAllViews()

        filteredOffers.forEachIndexed { index, offer ->
            val txtOffer = TextView(this)
            txtOffer.text = "${index + 1}. ${offer.position}\n" +
                    "${offer.company} · ${offer.city} · ${formatSalary(offer.salary)}"
            txtOffer.textSize = 16f
            txtOffer.setPadding(0, 0, 0, 24)

            offersContainer.addView(txtOffer)
        }
    }

    private fun openSavePdfPicker() {
        // The system file picker lets the user choose where to save the PDF (no storage permission needed).
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_TITLE, "offers_report.pdf")

        @Suppress("DEPRECATION")
        startActivityForResult(intent, createPdfRequestCode)
    }

    private fun exportReportPdf(pdfUri: Uri) {
        // PdfDocument creates a page and gives us a Canvas to draw the report on.
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val pdfPage = pdfDocument.startPage(pageInfo)
        val canvas = pdfPage.canvas

        val titlePaint = Paint()
        titlePaint.color = Color.BLACK
        titlePaint.textSize = 24f
        titlePaint.isFakeBoldText = true

        canvas.drawColor(Color.WHITE)
        canvas.drawText("Job offers report", 32f, 48f, titlePaint)

        // Reuse the report that is already on screen: draw reportContainer onto the PDF canvas.
        val availableWidth = 531f
        val availableHeight = 760f
        val reportWidth = reportContainer.width
        val reportHeight = reportContainer.height

        if (reportWidth > 0 && reportHeight > 0) {
            val scale = min(availableWidth / reportWidth, availableHeight / reportHeight)

            canvas.save()
            canvas.translate(32f, 72f)
            canvas.scale(scale, scale)
            reportContainer.draw(canvas)
            canvas.restore()
        }

        pdfDocument.finishPage(pdfPage)

        try {
            val output = contentResolver.openOutputStream(pdfUri)

            if (output != null) {
                output.use {
                    pdfDocument.writeTo(output)
                }
                Toast.makeText(this, "PDF saved", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Could not save the PDF", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not save the PDF", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun showChart(filteredOffers: List<Offer>) {
        // Chart: first count the data, then represent it with Views.
        val offersValencia = filteredOffers.count { it.city == "Valencia" }
        val offersMadrid = filteredOffers.count { it.city == "Madrid" }
        val offersAlicante = filteredOffers.count { it.city == "Alicante" }

        // 1 is included so the maximum can never be 0.
        val maxValue = maxOf(offersValencia, offersMadrid, offersAlicante, 1)

        barValencia.max = maxValue
        barMadrid.max = maxValue
        barAlicante.max = maxValue

        barValencia.progress = offersValencia
        barMadrid.progress = offersMadrid
        barAlicante.progress = offersAlicante

        txtCountValencia.text = offersValencia.toString()
        txtCountMadrid.text = offersMadrid.toString()
        txtCountAlicante.text = offersAlicante.toString()
    }

    private fun formatSalary(salary: Int): String {
        return "%,d".format(salary).replace(",", ".") + " €"
    }
}
