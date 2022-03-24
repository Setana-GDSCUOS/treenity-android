package com.setana.treenity

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.setana.treenity.util.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TreenityApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    companion object {
        lateinit var PREFS: PreferenceManager
        val DAILY_WALK_LOG = hashMapOf<String, String>()
        var idAndDate: MutableMap<Float, String> = mutableMapOf()
    }

    override fun onCreate() {
        PREFS = PreferenceManager(applicationContext)
        super.onCreate()
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}