package com.hhkv.rustitok.utils

import android.content.Context
import androidx.core.content.edit

class SharedPrefsManager(context:Context) {
    private val prefs = context.getSharedPreferences("AppCache",Context.MODE_PRIVATE)
    fun setString(key:String,value:String) {
        prefs.edit {
            putString(key,value)
        }
    }
    fun getString(key:String): String? {
       return  prefs.getString(key,null)
    }

}