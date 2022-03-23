package com.setana.treenity

import android.app.Application
import com.setana.treenity.util.PreferenceManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TreenityApplication : Application() {
    companion object {
        lateinit var PREFS: PreferenceManager
        val DAILY_WALK_LOG = hashMapOf<String, String>()
        var idAndDate: MutableMap<Float, String> = mutableMapOf()
        var daily_Step : Int = 0
    }

    override fun onCreate() {
        PREFS = PreferenceManager(applicationContext)
        super.onCreate()
    }
}