package com.setana.treenity.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.service.PushNotificationWorker
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener{

    private val settingsViewModel: SettingsViewModel by viewModels()
    val userId = PREFS.getLong(USER_ID_KEY, -1)
    private lateinit var newName : String

    // sensor permission
    private val activityPermission = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    private val constraints = androidx.work.Constraints.Builder()
        .setRequiresCharging(false)
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresBatteryNotLow(true)
        .build()

    private val alarmRequest = PeriodicWorkRequest.Builder(
        PushNotificationWorker::class.java,
        15, // 최소 시간이 15분 -> TODO 나중에 1시간으로 설정할 것
        TimeUnit.MINUTES
    ).setConstraints(constraints).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.mypage_settings_activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    // 이벤트 작성 nickname 변경될 때, push 알람 설정 true 되었을 때 서버로 POST 요청보낼 것
    @SuppressLint("InflateParams")
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {


        // 닉네임 변경되었을 때
        if(key == "user_name_key") {
            newName = sharedPreferences?.getString(key,"no name").toString()
            settingsViewModel.updateUserName(userId.toString(), newName)

            // post
            settingsViewModel.userLiveData.observe(this, { response ->

                response.let {
                    if (it != null && userId != -1L) {
                            Toast.makeText(this, "New Name has been successfully saved", Toast.LENGTH_SHORT).show()
                        } else {
                            throw IllegalArgumentException("Your Id is invalid or you haven't changed your name")
                        }
                    }

            })

            // test
            Log.d("tag", "onCreate: your new name is $newName")

        }

        // push 알람 설정되었을 때
        if(key == "switch") {

            if(sharedPreferences?.getBoolean(key, false) == false) {

                WorkManager.getInstance(this).cancelUniqueWork("alarm")

                Toast.makeText(this@SettingsActivity, "Push Alarm OFF", Toast.LENGTH_SHORT).show()
            } else {

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "alarm",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    alarmRequest
                )

                Toast.makeText(this@SettingsActivity, "Push Alarm ON", Toast.LENGTH_SHORT).show()
            }
        }

        // permission 체크박스 눌렀을 때
        if(key == "permission") {

            // 다이얼로그
            val builder = AlertDialog.Builder(this)

            builder.setIcon(R.drawable.mypage_setting_icon)    // 제목 아이콘
            builder.setTitle("AUTHORIZATION")    // 제목
            builder.setView(layoutInflater.inflate(R.layout.mypage_goto_application_settings, null)) // null 때문에 @SuppressLint("InflateParams") 붙임(IDE 추천)

            builder.setPositiveButton("GO To APP SETTINGS") { dialog, which ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }

            if(sharedPreferences?.getBoolean(key, false) == true) {

                // 권한이 거절된 상태 
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    ) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACTIVITY_RECOGNITION
                        )) {
                        // 1. 사용자가 승인 거절을 모두 한 적이 없는 경우, 권한 요청
                        ActivityCompat.requestPermissions(this, permission, activityPermission)

                    } else {
                        // 2. 사용자가 거절을 누른 경우, 요청을 다시 해봤자 다이얼로그가 안뜨기에, 설정 창으로 갈 수 있는 다이얼로그를 띄움
                        builder.show()
                    }
                } else {
                    // 3. 권한이 승인된 상태, 승인 되었다고 Toast 메시지 띄우기
                    Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // ACTIVITY_RECOGNITION 권한 수락/거부 시
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == activityPermission) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent(this, TreenityForegroundService::class.java)
                startService(intent)

                Toast.makeText(this, "Activity Permission Activated", Toast.LENGTH_SHORT).show()
            } else { // 거부하기를 눌렀다면
                Toast.makeText(this, "Activity Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}