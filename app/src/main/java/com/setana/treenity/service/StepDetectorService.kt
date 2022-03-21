package com.setana.treenity.service

import android.Manifest
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
import androidx.core.app.NotificationCompat
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.setana.treenity.ui.loading.LoadingActivity


class StepDetectorService : Service(), SensorEventListener {

    private var stepsBeforeDetection = 0

    companion object {
        const val CHANNEL_ID = "channel_id"
    }

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

        // 신체 활동 센서 켜져 있는 거 확인되면 notification 띄움 -> "Pedometer service is running" + 유저가 권한 끄면 그 즉시 notification 사라지는 거 확인!
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            showNotification()
        }

        return START_NOT_STICKY
    }

    private fun showNotification() {

        val intent = Intent(applicationContext, LoadingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0, intent, FLAG_IMMUTABLE
        )


        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentTitle("Notification")
            .setContentText("Pedometer service is running")
            .setSmallIcon(com.setana.treenity.R.drawable.mypage_settings_alarm_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.FLAG_NO_CLEAR) // swipe 해도 지워지지 않음
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 잠금화면에도 보여줌
        
        val channelName = "Channel Name"
        val channelDescription = "Channel Description"
        val channelImportance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
            description = channelDescription
        }

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.createNotificationChannel(channel)

        startForeground(10, notification.build()) // swipe 해도 사라지지 않음 -> 설정으로 끌 수 있음
        // TODO id가 뭘 의미하는 지 잘 모르겠음..
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

    override fun onDestroy() {
        super.onDestroy()
        
        // TODO: 발걸음 수 POST 요청
    }

}