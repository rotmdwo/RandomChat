package com.sungjae.randomchat

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class PreferenceManager {
    companion object {
        lateinit var instance: PreferenceManager
        lateinit var mSharedPreferences: SharedPreferences
        private const val SHARED_PREFERENCES = "SHARED_PREFERENCES"

        fun create(context: Context) {
            instance = PreferenceManager()
            mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, AppCompatActivity.MODE_PRIVATE)
        }
    }

    fun getString(key: String): String? {
        return mSharedPreferences.getString(key, "")
    }

    fun putString(key: String, value: String) {
        // apply() is asynchronous, while commit() is synchronous which shouldn't be used in UI Thread
        mSharedPreferences.edit {
            putString(key, value).apply()
        }
    }
}