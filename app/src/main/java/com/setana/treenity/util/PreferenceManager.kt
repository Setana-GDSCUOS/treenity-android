package com.setana.treenity.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    companion object {
        const val PREFS_FILENAME = "treenity_preference"
        const val DAILY_WALK_LOG_KEY = "DAILY_WALK_LOG_KEY"
        const val USER_ID_KEY = "USER_ID_KEY"
        const val USER_EMAIL_KEY = "USER_EMAIL_KEY"
    }
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getLong(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun setLong(key: String, longVal: Long) {
        prefs.edit().putLong(key, longVal).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, booleanVal: Boolean) {
        prefs.edit().putBoolean(key, booleanVal).apply()
    }

    fun removeValue(key: String) {
        prefs.edit().remove(key).apply()
    }
}