package com.setana.treenity.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.util.PreferenceManager.Companion.DAILY_WALK_LOG_KEY
import java.text.SimpleDateFormat
import java.util.*

class StepDetectorService : Service(), SensorEventListener {

    private var stepsBeforeDetection = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val countSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) // 기존의 걸음 수도 가져오기에 초기값을 나중에 빼줘야 함

        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL)
            /*
            1.SENSOR_DELAY_FASTEST  0 ms 최대한 빠르게
            2.SENSOR_DELAY_GAME     20,000ms 게임에 적합한 속도
            3.SENSOR_DELAY_UI       60,000ms UI 수정에 적합한 속도
            4.SENSOR_DELAY_NORMAL   200,000ms 화면 방향 변화를 모니터링하기에 적합한 속도
             */
        }else{
            Toast.makeText(this, "Sensor Not Detected", Toast.LENGTH_SHORT).show()
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private var mSteps = 0
    private var mStepBuffer = 0
    override fun onSensorChanged(sensor: SensorEvent?) {
        sensor?.let {
            val currentDetectedSteps =
                if (stepsBeforeDetection == 0) stepsBeforeDetection else it.values[0].toInt() - stepsBeforeDetection
            mSteps += currentDetectedSteps
            mStepBuffer += currentDetectedSteps
            storeStepToGlobalHashMap(mSteps)
            if (mStepBuffer >= 10) {
                storeStepToSharedPreference()
                mStepBuffer = 0
            }
            stepsBeforeDetection = it.values[0].toInt()
        }
        // 로그 기록용
        // Log.d("tag", "onSensorChanged: your step feature is ${DAILY_WALK_LOG[getDateString()]}")

        val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
            ?: hashMapOf(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date()) to "0"
            )
        Log.d("tag", "onSensorChanged: your step feature is $hashMap")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("SERVICE", p0.toString())
    }

    private fun getDateString() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun storeStepToGlobalHashMap(step: Int) {
        DAILY_WALK_LOG[getDateString()] = step.toString()
    }

    private fun storeStepToSharedPreference() {
        val hashMapString = Gson().toJson(DAILY_WALK_LOG)
        PREFS.setString(DAILY_WALK_LOG_KEY, hashMapString)
    }

}