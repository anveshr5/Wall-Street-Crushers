package com.anvesh.wallstreetcrushers.datamodels

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class User(
    val userId: String,
    val name: String,
    val email: String,
    val phone: String,
    var money: String,
    var totalMoney: String
) : Parcelable, java.io.Serializable{
    constructor() : this("", "", "", "", "", "")
}
