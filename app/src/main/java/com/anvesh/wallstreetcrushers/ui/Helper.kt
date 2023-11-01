package com.anvesh.wallstreetcrushers.ui

import android.text.TextUtils
import android.util.Patterns

class Helper {
    companion object{
        fun isValidEmail(target: String): Boolean {
            return if (TextUtils.isEmpty(target)) {
                false
            } else {
                Patterns.EMAIL_ADDRESS.matcher(target).matches()
            }
        }

        fun isPhoneNumber(string: String): Boolean{
            if(string.length != 10)
                return false
            for(c in string){
                if(c < '0' || c > '9')
                    return false
            }
            return true
        }
        fun getMonthString(month: String): String{
            return when(month){
                "01" -> "January"
                "02" -> "February"
                "03" -> "March"
                "04" -> "April"
                "05" -> "May"
                "06" -> "June"
                "07" -> "July"
                "08" -> "August"
                "09" -> "September"
                "10" -> "October"
                "11" -> "November"
                "12" -> "December"
                else -> ""
            }
        }
        fun getMonthNumber(month: String): Int{
            return when(month){
                "January" -> 1
                "February" -> 2
                "March" -> 3
                "April" -> 4
                "May" -> 5
                "June" -> 6
                "July" -> 7
                "August" -> 8
                "September" -> 9
                "October" -> 10
                "November" -> 11
                "December" -> 12
                else -> 0
            }
        }
        fun checkPasswordStrength(password: String): Boolean {
            if (password.length < 8) return false
            if(!password.matches(Regex(".*[0-9].*"))) return false
            if(!password.matches(Regex(".*[a-z].*"))) return false
            if(!password.matches(Regex(".*[A-Z].*"))) return false
            if(!password.matches(Regex("^(?=.*[_.()\$&@]).*\$"))) return false

            return true
        }
    }
}