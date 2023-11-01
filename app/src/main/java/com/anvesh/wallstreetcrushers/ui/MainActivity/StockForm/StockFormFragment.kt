package com.anvesh.wallstreetcrushers.ui.MainActivity.StockForm

import android.content.Intent
import android.icu.util.LocaleData
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import com.anvesh.wallstreetcrushers.R
import com.anvesh.wallstreetcrushers.ui.StockAnalysis.StockAnalysisActivity
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.util.*

class StockFormFragment : Fragment() {

    lateinit var etStockId: TextInputEditText
    lateinit var dateStart: DatePicker
    lateinit var dateEnd: DatePicker
    lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stock_form, container, false)
        initiate(view)

        btnSubmit.setOnClickListener {
            val stockId = etStockId.text.toString().trim().uppercase()
            Log.e("stockId", stockId)
            val startDate = addZero(dateStart.dayOfMonth) + "-" + addZero(dateStart.month) + "-" + dateStart.year.toString()
            Log.e("DateStart", startDate)
            val endDate = addZero(dateEnd.dayOfMonth) + "-" + addZero(dateEnd.month) + "-" + dateEnd.year.toString()
            Log.e("EndDate", endDate)
            if(stockId.isEmpty() || !checkDates(startDate, endDate)){
                Toast.makeText(requireContext(), "Please input proper info", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(requireActivity(), StockAnalysisActivity::class.java)
            intent.putExtra("stockSymbol", stockId)
            intent.putExtra("startDate", startDate)
            intent.putExtra("endDate", endDate)
            startActivity(intent)
        }

        return view
    }

    private fun addZero(num: Int): String {
        return if(num/10 == 0) "0$num"
        else "$num"
    }

    private fun checkDates(startDate: String, endDate: String): Boolean {
        if(startDate == endDate) return false
        return true
    }

    private fun initiate(view: View) {
        etStockId = view.findViewById(R.id.etStockId)
        dateStart = view.findViewById(R.id.dateStart)
        dateEnd = view.findViewById(R.id.dateEnd)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        dateStart.updateDate(2022, LocalDate.now().monthValue-1, LocalDate.now().dayOfMonth)
    }
}