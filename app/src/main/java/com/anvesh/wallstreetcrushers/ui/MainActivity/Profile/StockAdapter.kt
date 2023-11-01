package com.anvesh.wallstreetcrushers.ui.MainActivity.Profile

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.datamodels.StockDetails
import com.anvesh.wallstreetcrushers.ui.Sell.SellStockActivity
import com.anvesh.wallstreetcrushers.ui.StockAnalysis.StockAnalysisActivity
import yahoofinance.Stock
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class StockAdapter(val stockDetails: ArrayList<StockDetails>, val context: Context) : RecyclerView.Adapter<StockAdapter.StockView>() {
    class StockView(view: View) : RecyclerView.ViewHolder(view){
        val viewHolder: ConstraintLayout = view.findViewById(R.id.viewHolder)
        val txtStockSymbol: TextView = view.findViewById(R.id.txtStockSymbol)
        val txtStockName: TextView = view.findViewById(R.id.txtStockName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtQuantity: TextView = view.findViewById(R.id.txtQuantity)
        val btnSell: Button = view.findViewById(R.id.btnSell)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockView {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_stock_view, parent, false)

        return StockView(view)
    }

    override fun onBindViewHolder(holder: StockView, position: Int) {
        holder.txtStockSymbol.text = stockDetails[position].stockSymbol
        holder.txtStockName.text = stockDetails[position].stockName
        holder.txtDate.text = stockDetails[position].date
        holder.txtPrice.text = stockDetails[position].price
        holder.txtQuantity.text = stockDetails[position].quantity.toString()

        holder.btnSell.setOnClickListener {
            val intent = Intent(context, SellStockActivity::class.java)
            intent.putExtra("stockDetails", stockDetails[position])
            context.startActivity(intent)
        }

        holder.viewHolder.setOnClickListener {
            val stockSymbol = stockDetails[position].stockSymbol.trim().uppercase()
            val dateStart = LocalDate.now()
            val startDate = addZero(dateStart.dayOfMonth) + "-" + addZero(dateStart.monthValue-1) + "-" + (dateStart.year-1).toString()
            val dateEnd = LocalDate.now()
            val endDate = addZero(dateEnd.dayOfMonth) + "-" + addZero(dateEnd.monthValue-1) + "-" + dateEnd.year.toString()
            if(stockSymbol.isEmpty()){
                Toast.makeText(context, "Please input proper info", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.e("p-startDate", startDate)
            Log.e("e-endDate", endDate)
            val intent = Intent(context, StockAnalysisActivity::class.java)
            intent.putExtra("stockSymbol", stockSymbol)
            intent.putExtra("startDate", startDate)
            intent.putExtra("endDate", endDate)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return stockDetails.size
    }

    private fun addZero(num: Int): String {
        return if(num/10 == 0) "0$num"
        else "$num"
    }
}