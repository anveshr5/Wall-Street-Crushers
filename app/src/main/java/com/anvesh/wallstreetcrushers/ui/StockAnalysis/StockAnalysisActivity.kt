package com.anvesh.wallstreetcrushers.ui.StockAnalysis

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.StockDetails
import com.anvesh.wallstreetcrushers.ui.MainActivity.MainActivity
import com.github.aachartmodel.aainfographics.aachartcreator.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import yahoofinance.Stock
import yahoofinance.histquotes.Interval
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt


class StockAnalysisActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var txtGMean: TextView
    private lateinit var txtStanDev: TextView
    private lateinit var txtAnalytical5: TextView
    private lateinit var txtAnalytical1: TextView
    private lateinit var txtHistorical5: TextView
    private lateinit var txtHistorical1: TextView
    private lateinit var txtMaxDrawDown: TextView
    private lateinit var aaChartView: AAChartView

    private lateinit var txtStockSymbol: TextView
    private lateinit var txtStockPrice: TextView
    private lateinit var txtStockSymbol1: TextView
    private lateinit var txtFinalPrice: TextView
    private lateinit var btnMinus: ImageButton
    private lateinit var txtQuantity: TextView
    private lateinit var btnAdd: ImageButton
    private lateinit var btnBuyStocks: Button

    private lateinit var progressLayout: ConstraintLayout

    private var job: Job = Job()
    private lateinit var stock: Stock
    private lateinit var currStock: Stock
    private lateinit var priceHistory: ArrayList<Any>
    private lateinit var returns: ArrayList<Double>
    private lateinit var rebaseTo100: ArrayList<Double>
    private lateinit var peak: ArrayList<Double>
    private lateinit var drawdowns: ArrayList<Any>
    private lateinit var dates: Array<String>
    private lateinit var maxDrawDown: String
    private var gMean: Double = 1.0
    private var standardDeviation = 0.0
    private var quantity = 0
    private var money = 0.0

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_analysis)
        initiate()
        launch {
            getStock()
        }
        btnMinus.setOnClickListener {
            if (quantity > 0) {
                quantity -= 1
                var temp = "${currStock.symbol} ${quantity} stock's value"
                txtStockSymbol1.text = temp
                temp = "${currStock.currency}${
                    String.format(
                        "%.2f",
                        currStock.quote.price.toDouble() * quantity
                    )
                }"
                txtFinalPrice.text = temp
                txtQuantity.text = quantity.toString()
            }
        }
        btnAdd.setOnClickListener {
            if (checkAmount()) {
                Toast.makeText(this, "Add more money to account!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            quantity += 1
            var temp = "${currStock.symbol} ${quantity} stock's value"
            txtStockSymbol1.text = temp
            temp = "${currStock.currency}${
                String.format(
                    "%.2f",
                    currStock.quote.price.toDouble() * quantity
                )
            }"
            txtFinalPrice.text = temp
            txtQuantity.text = quantity.toString()
        }
        btnBuyStocks.setOnClickListener {
            if (quantity == 0) {
                Toast.makeText(this, "Please add at least 1 stock", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            buyStock()
        }
    }

    private fun checkAmount(): Boolean {
        //Log.e("money", money.toString())
        return money < currStock.quote.price.toDouble() * (quantity + 1)
    }

    private fun buyStock() {
        progressLayout.visibility = View.VISIBLE
        val uid = FirebaseAuth.getInstance().uid!!
        val ref = FirebaseDatabase.getInstance()
            .getReference("/Users/${uid}/Stocks")
        val date = getDate()
        val stockId = ref.push().key!!
        ref.child(stockId).setValue(
            StockDetails(
                stockId,
                stock.symbol,
                stock.name,
                date,
                currStock.quote.price.toString(),
                quantity,
                0,
                "0"
            )
        )
        money -= currStock.quote.price.toDouble() * quantity
        val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE).edit()
        sharedPrefs.putString("Money", String.format("%.2f", money))
        sharedPrefs.apply()
        FirebaseDatabase.getInstance().getReference("/Users/$uid/money")
            .setValue(String.format("%.2f", money))
        Toast.makeText(this, "${currStock.name}'s ${quantity} stocks bought", Toast.LENGTH_LONG)
            .show()
        val intent = Intent(this@StockAnalysisActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun getDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun getStock() {
        val stockSymbol = intent.getStringExtra("stockSymbol")!!
        var temp = "$stockSymbol stock value"
        txtStockSymbol.text = temp
        temp = "$stockSymbol $quantity stock's value"
        txtStockSymbol1.text = temp
        val startDate = intent.getStringExtra("startDate")!!
        val endDate = intent.getStringExtra("endDate")!!
        val sCal = Calendar.getInstance()
        sCal.set(
            startDate.substring(6).toInt(),
            startDate.substring(3, 5).toInt(),
            startDate.substring(0, 2).toInt()
        )
        val eCal = Calendar.getInstance()
        eCal.set(
            endDate.substring(6).toInt(),
            endDate.substring(3, 5).toInt(),
            endDate.substring(0, 2).toInt()
        )
        System.setProperty("http.agent", "")

        val getCSV = GlobalScope.launch {
            try {
                stock = yahoofinance.YahooFinance.get(stockSymbol, sCal, eCal, Interval.DAILY)
                currStock = yahoofinance.YahooFinance.get(stockSymbol)
                Log.e("stock", stock.history.toString())
                val datesTemp = ArrayList<String>()
                priceHistory = ArrayList()
                stock.history.forEach {
                    val date = it.date.time
                    //    Log.e("" + date.date + "-" + date.month + "-" + date.year, it.adjClose.toString())
                    datesTemp.add(
                        "" + date.date + "-" + (date.month + 1) + "-20" + date.year.toString()
                            .substring(1)
                    )
                    priceHistory.add(it.close.toDouble())
                }
                Log.e("currStock", currStock.toString())
                dates = datesTemp.toTypedArray()
            } catch (e: java.lang.Exception) {
                this@StockAnalysisActivity.runOnUiThread(Runnable {
                    Toast.makeText(
                        this@StockAnalysisActivity,
                        "Incorrect Stock Symbol or dates",
                        Toast.LENGTH_LONG
                    ).show()
                    val intent = Intent(this@StockAnalysisActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                })
            }
        }
        getCSV.join()
        fillCurrDetails()
        calculateReturns()
    }

    private fun fillCurrDetails() {
        var temp = "${currStock.currency}${String.format("%.2f", currStock.quote.price.toDouble())}"
        txtStockPrice.text = temp
        temp = "${currStock.currency}${
            String.format(
                "%.2f",
                currStock.quote.price.toDouble() * quantity
            )
        }"
        txtFinalPrice.text = temp
    }

    private fun calculateReturns() {
        toolbar.title = stock.name
        returns = ArrayList<Double>()
        returns.add(0.0)
        for (i in 1 until stock.history.size) {
            returns.add(((stock.history[i].adjClose - stock.history[i - 1].adjClose) / stock.history[i - 1].adjClose).toDouble())
        }
        //returns.forEach {
        //    Log.e("returns", it.toString())
        //}
        calcuateGeometricMean()
    }

    private fun calcuateGeometricMean() {
        for (i in 1 until returns.size) {
            gMean *= (returns[i] + 1.0)
        }
        gMean = gMean.pow(1.0 / (returns.size - 1)) - 1
        Log.e("res", gMean.toString())
        val temp = "${gMean * 100}%"
        txtGMean.text = temp
        calculateStandardDeviation()
    }

    private fun calculateStandardDeviation() {
        for (i in 1 until returns.size) {
            var Ai = 1 + returns[i]
            standardDeviation += ln(Ai / (1 + gMean)).pow(2.0)
        }
        standardDeviation /= (returns.size - 1)
        standardDeviation = sqrt(standardDeviation)
        standardDeviation = exp(standardDeviation) - 1
        var temp = "${standardDeviation * 100}%"
        txtStanDev.text = temp

        //Log.e("stand_dev", standardDeviation.toString())

        calculateAnalyticalAndHistoricalVars()
        calculateRebaseTo100andPeak()
    }

    private fun calculateAnalyticalAndHistoricalVars() {
        var temp = "${String.format("%.6f", (gMean - (1.65 * standardDeviation)) * 100)}%"
        txtAnalytical5.text = temp
        temp = "${String.format("%.6f", (gMean - (2.33 * standardDeviation)) * 100)}%"
        txtAnalytical1.text = temp
        val sortedReturns = returns.sorted()
        val p5 = (returns.size * 5) / 100
        val p1 = returns.size / 100
        temp = "${String.format("%.4f", sortedReturns[p5] * 100)}%"
        txtHistorical5.text = temp
        temp = "${String.format("%.4f", sortedReturns[p1] * 100)}%"
        txtHistorical1.text = temp
    }

    private fun calculateRebaseTo100andPeak() {
        rebaseTo100 = ArrayList()
        peak = ArrayList()
        rebaseTo100.add(100.0)
        peak.add(100.0)
        for (i in 1 until returns.size) {
            rebaseTo100.add(rebaseTo100[i - 1] * (1 + returns[i]))
            peak.add(Math.max(rebaseTo100[i], peak[i - 1]))
        }
        calculateDrawDowns()
    }

    private fun calculateDrawDowns() {
        drawdowns = ArrayList()
        var drawdown = 0.0
        for (i in 1 until rebaseTo100.size) {
            drawdowns.add(Math.min(0.0, rebaseTo100[i] / peak[i] - 1) * 100)
            //Log.e("drawdown", drawdowns[i-1].toString())
            if (drawdown >= drawdowns[i - 1].toString().toDouble()) {
                drawdown = drawdowns[i - 1].toString().toDouble()
                maxDrawDown = "${String.format("%.6f", drawdown)} on ${dates[i]}"
            }
        }
        txtMaxDrawDown.text = maxDrawDown
        createChartModel()
    }

    private fun createChartModel() {
        val aaChartModel: AAChartModel = AAChartModel()
            .chartType(AAChartType.Area)
            .title("${stock.symbol} Price/Drawdown")
            .dataLabelsEnabled(false)
            .zoomType(AAChartZoomType.X)
            .tooltipEnabled(true).categories(
                dates
            )
            .series(
                arrayOf(
                    AASeriesElement()
                        .name("Price")
                        .data(priceHistory.toArray()).color("#00FF00"),
                    AASeriesElement()
                        .name("Drawdown")
                        .data(drawdowns.toArray()).color("#FF0000")
                )
            )
        aaChartView.aa_drawChartWithChartModel(aaChartModel)
        progressLayout.visibility = View.GONE
    }

    private fun initiate() {
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        txtGMean = findViewById(R.id.txtGMean)
        txtStanDev = findViewById(R.id.txtStanDev)
        txtAnalytical5 = findViewById(R.id.txtAnalytical5)
        txtAnalytical1 = findViewById(R.id.txtAnalytical1)
        txtHistorical5 = findViewById(R.id.txtHistorical5)
        txtHistorical1 = findViewById(R.id.txtHistorical1)
        txtMaxDrawDown = findViewById(R.id.txtMaxDrawDown)
        aaChartView = findViewById(R.id.aaChartView)

        txtStockSymbol = findViewById(R.id.txtStockId)
        txtStockPrice = findViewById(R.id.txtStockPrice)
        txtStockSymbol1 = findViewById(R.id.txtStockId1)
        txtFinalPrice = findViewById(R.id.txtFinalPrice)
        btnMinus = findViewById(R.id.imgMinus)
        txtQuantity = findViewById(R.id.txtQuantity)
        btnAdd = findViewById(R.id.imgAdd)
        btnBuyStocks = findViewById(R.id.btnBuyStocks)

        val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE)
        money = sharedPrefs.getString("Money", "0")!!.toDouble()
    }
}