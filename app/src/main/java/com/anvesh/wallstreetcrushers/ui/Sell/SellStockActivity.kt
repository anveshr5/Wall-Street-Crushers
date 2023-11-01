package com.anvesh.wallstreetcrushers.ui.Sell

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.StockDetails
import com.anvesh.wallstreetcrushers.ui.MainActivity.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import yahoofinance.Stock
import kotlin.coroutines.CoroutineContext
import kotlin.text.Typography.quote

class SellStockActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var txtStockSymbol: TextView
    private lateinit var txtCurrStockPrice: TextView
    private lateinit var txtPurchasePrice: TextView
    private lateinit var txtProfitLossSoFar: TextView
    private lateinit var txtCurrProfitLoss: TextView
    private lateinit var btnMinus: ImageButton
    private lateinit var txtQuantity: TextView
    private lateinit var btnAdd: ImageButton
    private lateinit var btnSell: Button
    private lateinit var progressLayout: ConstraintLayout

    private lateinit var stockDetails: StockDetails
    private lateinit var stock: Stock
    private var job: Job = Job()
    private var diff = 0.0
    private var quantity = 0
    private var money = 0.0
    private var soldStock = false

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell_stock)
        initiate()
        launch {
            getStock()
        }
        btnMinus.setOnClickListener {
            if(quantity == 0){
                if(resources.configuration.isNightModeActive)
                    txtCurrProfitLoss.setTextColor(resources.getColor(R.color.white))
                else
                    txtCurrProfitLoss.setTextColor(resources.getColor(R.color.black))
                return@setOnClickListener
            }
            quantity-=1
            txtQuantity.text = quantity.toString()
            txtCurrProfitLoss.text = "${stock.currency} ${String.format("%.2f", diff*quantity)}"
        }

        btnAdd.setOnClickListener {
            if(quantity == stockDetails.quantity - stockDetails.sold) return@setOnClickListener
            quantity+=1
            txtQuantity.text = quantity.toString()
            txtCurrProfitLoss.text = "${stock.currency} ${String.format("%.2f", diff*quantity)}"
            if(diff < 0){
                txtCurrProfitLoss.setTextColor(resources.getColor(R.color.red))
            } else {
                txtCurrProfitLoss.setTextColor(resources.getColor(R.color.green))
            }
        }
        btnSell.setOnClickListener {
            progressLayout.visibility = View.VISIBLE
            if(quantity == 0){
                progressLayout.visibility = View.GONE
                return@setOnClickListener
            }
            val uid = FirebaseAuth.getInstance().uid!!
            val ref = FirebaseDatabase.getInstance().getReference("Users/$uid/Stocks/${stockDetails.stockId}")
            stockDetails.sold += quantity
            stockDetails.profit = String.format("%.2f", stockDetails.profit.toDouble() + diff*quantity)
            ref.child("sold").setValue(stockDetails.sold)
            ref.child("profit").setValue(stockDetails.profit)
            if(stockDetails.profit.toDouble() < 0.0){
                txtProfitLossSoFar.setTextColor(resources.getColor(R.color.red))
            } else {
                txtProfitLossSoFar.setTextColor(resources.getColor(R.color.green))
            }
            val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE).edit()
            if(stock.currency != "INR"){
                //convert currency here
            }
            money += stock.quote.price.toDouble() * quantity
            sharedPrefs.putString("Money", String.format("%.2f", money))
            sharedPrefs.apply()
            FirebaseDatabase.getInstance().getReference("Users/$uid/money").setValue(String.format("%.2f", money))
            quantity = 0
            txtQuantity.text = "${stock.currency} 0"
            txtCurrProfitLoss.text = "${stock.currency} 0"
            txtProfitLossSoFar.text = "${stock.currency} ${stockDetails.profit}"
            soldStock = true
            progressLayout.visibility = View.GONE
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun getStock() {
        System.setProperty("http.agent", "")
        val getCSV = GlobalScope.launch {
            stock = yahoofinance.YahooFinance.get(stockDetails.stockSymbol)
        }
        getCSV.join()
        progressLayout.visibility = View.GONE
        fillDetails()
    }

    private fun fillDetails() {
        toolbar.title = stockDetails.stockName
        txtStockSymbol.text = stockDetails.stockSymbol
        txtCurrStockPrice.text = "${stock.currency} ${stock.quote.price}"
        txtPurchasePrice.text = "${stock.currency} ${stockDetails.price}"
        txtProfitLossSoFar.text = "${stock.currency} ${stockDetails.profit}"
        txtCurrProfitLoss.text = "${stock.currency} 0"

        if(stockDetails.profit.toDouble() < 0.0){
            txtProfitLossSoFar.setTextColor(resources.getColor(R.color.red))
        } else {
            txtProfitLossSoFar.setTextColor(resources.getColor(R.color.green))
        }

        diff = stock.quote.price.toDouble() - stockDetails.price.toDouble()
    }

    private fun initiate() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        txtStockSymbol = findViewById(R.id.txtStockSymbol)
        txtCurrStockPrice = findViewById(R.id.txtCurrStockPrice)
        txtPurchasePrice = findViewById(R.id.txtPurchasePrice)
        txtProfitLossSoFar = findViewById(R.id.txtProfitLossSoFar)
        txtCurrProfitLoss = findViewById(R.id.txtCurrProfitLoss)
        btnMinus = findViewById(R.id.imgMinus)
        txtQuantity = findViewById(R.id.txtQuantity)
        btnAdd = findViewById(R.id.imgAdd)
        btnSell = findViewById(R.id.btnSell)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        val sharedPrefs = this.getSharedPreferences("User", Context.MODE_PRIVATE)
        money = sharedPrefs.getString("Money", "0")!!.toDouble()

        stockDetails = intent.getSerializableExtra("stockDetails") as StockDetails
    }

    override fun onBackPressed() {
        if(soldStock) {
            val intent = Intent(this@SellStockActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        } else {
            super.onBackPressed()
        }
    }
}