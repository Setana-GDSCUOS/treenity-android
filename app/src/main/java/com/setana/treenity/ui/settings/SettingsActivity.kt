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
import android.widget.CompoundButton
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
import com.setana.treenity.databinding.MypageSettingsActivityMainBinding
import com.setana.treenity.service.PushNotificationWorker
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.util.AuthUtils
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener{


    private lateinit var mypageSettingsActivityMainBinding: MypageSettingsActivityMainBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var localUserId: Long = -1
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

    companion object {
        private const val TAG = "MyPageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
    }

    override fun onStart() {
        super.onStart()
        checkUser()
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        // nothing to write
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

    private fun setupUI() {
        setupViewBinding()

        // TODO: 이름 update 는 좀 더 고민해볼 것


        mypageSettingsActivityMainBinding.physicalActivityPermissionCheck.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { // 신체 활동 권한이 ON 일때
                Log.d(TAG, "setupUI: physicalActivityPermissionCheck is checked")

            } else { // 신체 활동 권한이 OFF 일때
                Log.d(TAG, "setupUI: physicalActivityPermissionCheck is unchecked")
            }
        }

        mypageSettingsActivityMainBinding.alarmNotificationCheck.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) { // 알람 설정이 ON 일때
                Log.d(TAG, "setupUI: alarmNotificationCheck is checked")

            } else { // 알람 설정이 OFF 일때
                Log.d(TAG, "setupUI: alarmNotificationCheck is unchecked")
            }
        }
    }

    private fun checkUser() {
        if (AuthUtils.userId <= 0) {
            Toast.makeText(this, "Invalid user credentials!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoadingActivity::class.java)
            startService(intent)
            finish()
        } else {
            localUserId = AuthUtils.userId
        }
    }

    private fun setupViewBinding() {
        mypageSettingsActivityMainBinding =
            MypageSettingsActivityMainBinding.inflate(layoutInflater)
        setContentView(mypageSettingsActivityMainBinding.root)
    }
}