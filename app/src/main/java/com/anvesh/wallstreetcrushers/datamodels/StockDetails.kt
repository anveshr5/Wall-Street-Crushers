package com.anvesh.wallstreetcrushers.datamodels

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import yahoofinance.Stock

@IgnoreExtraProperties
data class StockDetails(
    val stockId: String,
    val stockSymbol: String,
    val stockName: String,
    val date: String,
    val price: String,
    val quantity: Int,
    var sold: Int,
    var profit: String
) : java.io.Serializable{
    constructor() : this("", "", "", "", "", 0, 0, "")
}
