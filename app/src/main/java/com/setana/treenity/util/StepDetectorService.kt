package com.setana.treenity.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.setana.treenity.TreenityApplication

class StepDetectorService : Service(), SensorEventListener {

    private var stepsBeforeDetection = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val countSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) // 기존의 걸음 수도 가져오기에 초기값을 나중에 빼줘야 함

        if(countSensor != null){
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }else{
            Toast.makeText(this, "Sensor Not Detected", Toast.LENGTH_SHORT).show()
        }

        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onSensorChanged(sensor: SensorEvent?) {

        if (stepsBeforeDetection < 1) {
            // initial value
            if (sensor != null) {
                stepsBeforeDetection = sensor.values[0].toInt()
            }
        }
        // 리셋 안된 값 + 현재값 - 리셋 안된 값
        var mSteps = sensor!!.values[0].toInt() - stepsBeforeDetection

        // 100 넘었을 때 100단위로 잘라서 저장할 것 + sharedPreference
        // 안 넘었을 경우, 그대로 저장
        if(mSteps >= 100) {
            mSteps = (mSteps / 100) * 100
            TreenityApplication.steps = mSteps

            val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
            val editor: SharedPreferences.Editor = sharedPref.edit()

            editor.putInt("steps", mSteps) // steps 이라는 키워드로 저장
        }

        TreenityApplication.steps = mSteps

        // 로그 기록용
        Log.d("tag", "onSensorChanged: your step feature is ${TreenityApplication.steps}")
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.d("SERVICE", p0.toString())
    }

}