package com.setana.treenity.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.util.PreferenceManager.Companion.DAILY_WALK_LOG_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Coroutine 사용법
 * lifecycleScope.launch{ ... }
 */

@AndroidEntryPoint
class TreenityForegroundService : Service(), SensorEventListener {
    @Inject
    lateinit var fServiceNotification: Notification
    private var stepCountSensor: Sensor? = null
    private var mSteps = 0
    private var mStepBuffer = 0
    private var stepsBeforeDetection = 0

    companion object {
        const val CHANNEL_ID = "treenity_foreground"
    }

    /**
     * startService 첫 수행 시 onCreate -> onStartCommand 순으로 호출됨
     * 서비스 이미 실행 중에 다시 startService 수행된다면 onCreate 생략하고 onStartCommand 부터 수행
     * 센서 중복해서 가져올 필요 없으므로 onCreate 에서 수행하도록 구현
     */

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Restore mSteps
        mSteps = loadStepFromSharedPreference()

        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) // 기존의 걸음 수도 가져오기에 초기값을 나중에 빼줘야 함
        if (stepCountSensor != null) {
            /*
            1.SENSOR_DELAY_FASTEST  0 ms 최대한 빠르게
            2.SENSOR_DELAY_GAME     20,000ms 게임에 적합한 속도
            3.SENSOR_DELAY_UI       60,000ms UI 수정에 적합한 속도
            4.SENSOR_DELAY_NORMAL   200,000ms 화면 방향 변화를 모니터링하기에 적합한 속도
             */
            sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(this, "Sensor Not Detected", Toast.LENGTH_SHORT).show()
        }

        // 신체 활동 센서 켜져 있는 거 확인되면 notification 띄움 -> "Pedometer service is running" + 유저가 권한 끄면 그 즉시 notification 사라지는 거 확인!
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startForegroundServiceWithNotification()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    private fun startForegroundServiceWithNotification() {
        startForeground(10, fServiceNotification) // swipe 해도 사라지지 않음 -> 설정으로 끌 수 있음
        // TODO id가 뭘 의미하는 지 잘 모르겠음..
    }

    override fun onSensorChanged(sensor: SensorEvent?) {
        sensor?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                mSteps = loadDailyStepFromGlobalHashMap()
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

                addDailyWalkBy1(currentDetectedSteps) // sensor 인식할 때마다 1씩 더해야 함

            }
        }

//        DEBUG
        Log.d("####################", "onSensorChanged: mSteps $mSteps")
//
//        val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
//        val type = object : TypeToken<HashMap<String, String>>() {}.type
//        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
//            ?: hashMapOf(
//                SimpleDateFormat(
//                    "yyyy-MM-dd",
//                    Locale.US
//                ).format(Date()) to "0"
//            )
//        Log.d("####################", "onSensorChanged: hm $hashMap")

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun getDateString() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun loadDailyStepFromGlobalHashMap(): Int = DAILY_WALK_LOG[getDateString()]?.toInt() ?: 0

    private fun storeStepToGlobalHashMap(step: Int) {
        DAILY_WALK_LOG[getDateString()] = step.toString()
    }

    private fun loadStepFromSharedPreference(): Int {
        val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
            ?: hashMapOf(
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(Date()) to "0"
            )
        return hashMap[getDateString()]?.toInt() ?: 0
    }

    private fun storeStepToSharedPreference() {
        val hashMapString = Gson().toJson(DAILY_WALK_LOG)
        PREFS.setString(DAILY_WALK_LOG_KEY, hashMapString)
    }

    private fun addDailyWalkBy1(mSteps: Int) {
        val intent = Intent("GOTO_MYPAGE_AND_ADD_DETECTED_STEP")
        intent.putExtra("NEWLY_DETECTED_STEP", mSteps)
        sendBroadcast(intent)
    }
}