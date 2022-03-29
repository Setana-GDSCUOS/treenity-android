package com.setana.treenity.ui.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.SeekBar
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
import com.airbnb.lottie.LottieDrawable
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.databinding.MypageSettingsActivityMainBinding
import com.setana.treenity.databinding.MypageSettingsNameDialogBinding
import com.setana.treenity.service.PushNotificationWorker
import com.setana.treenity.service.TreenityForegroundService
import com.setana.treenity.ui.loading.LoadingActivity
import com.setana.treenity.util.AuthUtils
import com.setana.treenity.util.PreferenceManager.Companion.ENABLE_PUSH_KEY
import com.setana.treenity.util.PreferenceManager.Companion.RENDER_TREE_NO_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener, View.OnClickListener{

    private lateinit var mypageSettingsActivityMainBinding: MypageSettingsActivityMainBinding
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var localUserId: Long = -1

    // animation
    private lateinit var loadingAnimationFrameLayout: FrameLayout

    // sensor permission
    private val PERMISSION_PHYSICAL_ACTIVITY = 100
    val permission = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION)

    companion object {
        private const val TAG = "MyPageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        checkUser()

        settingsViewModel.getUserInfo(localUserId)
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        // nothing to write
    }

    override fun onClick(p0: View?) {
        // nothing to write
    }

    // ACTIVITY_RECOGNITION 권한 수락/거부 시
    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_PHYSICAL_ACTIVITY) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {

                val intent = Intent(this, TreenityForegroundService::class.java)
                startService(intent)

                Toast.makeText(this, "Activity Permission Activated", Toast.LENGTH_SHORT).show()
            } else { // 거부하기를 눌렀다면
                Toast.makeText(this, "Activity Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViewBinding() {
        mypageSettingsActivityMainBinding = MypageSettingsActivityMainBinding.inflate(layoutInflater)
        setContentView(mypageSettingsActivityMainBinding.root)
    }

    private fun setupViewModel() {

        // get name
        settingsViewModel.userNameLiveData.observe(this) { user ->
            mypageSettingsActivityMainBinding.apply {
                newName.text = user.username

            }
            hideLoadingAnimation()
        }

        // put name
        settingsViewModel.userLiveData.observe(this@SettingsActivity) { response ->
            response.let {
                if (it.isSuccessful) { // put 요청 잘 되었으면, 유저 정보 가져옴
                    Toast.makeText(
                        this@SettingsActivity,
                        "New Name has been successfully saved",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    throw IllegalArgumentException("Error has been occurred or you haven't changed your name")
                }
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

    private fun setupLoadingAnimationFrameLayout() {
        loadingAnimationFrameLayout = mypageSettingsActivityMainBinding.frameLottieHolder

    }

    private fun showLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.VISIBLE
        playLottieAnimation()
    }

    private fun hideLoadingAnimation() {
        loadingAnimationFrameLayout.visibility = View.INVISIBLE
        loadingAnimationFrameLayout.bringToFront() // 왜 이걸 앞으로 가져와야 다른 뷰들이 잘 보이는 건지 잘 모르겠음
    }

    private fun playLottieAnimation() {
        val lottieAnimationView = mypageSettingsActivityMainBinding.lottieLoading
        lottieAnimationView.setAnimation("loading.json")
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    @SuppressLint("InflateParams")
    private fun setupUI() {
        setupViewBinding()
        setupLoadingAnimationFrameLayout()
        showLoadingAnimation()

        val builder = AlertDialog.Builder(this)
        // 이름 변경 탭을 누르면 editText 가 있는 다이얼로그가 나오며 적은게 있고 OK 를 누르면 적은 이름이 textView 에 보이며 PUT 요청이 이루어짐
        mypageSettingsActivityMainBinding.nameDialog.setOnClickListener {
            val builderItem = MypageSettingsNameDialogBinding.inflate(layoutInflater)
            val nameField = builderItem.nameField
            with(builder){
                setTitle("Update Name")
                setIcon(R.drawable.ic_baseline_edit_24)
                setView(builderItem.root)
                setPositiveButton("OK"){ _: DialogInterface, _: Int ->
                    if(nameField.text.isNotEmpty()) {

                        mypageSettingsActivityMainBinding.newName.text = nameField.text

                        settingsViewModel.updateUserName(localUserId.toString(), nameField.text.toString())
                    }
                }
                show()
            }
        }

        mypageSettingsActivityMainBinding.physicalActivityPermissionCheck.setOnClickListener {

            builder.setIcon(R.drawable.ic_baseline_settings_24)    // 제목 아이콘
            builder.setTitle("AUTHORIZATION")    // 제목
            builder.setView(layoutInflater.inflate(R.layout.mypage_goto_application_settings, null))

            builder.setPositiveButton("GO To APP SETTINGS") { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACTIVITY_RECOGNITION
                    )) {
                    // 1. 사용자가 승인 거절을 모두 한 적이 없는 경우, 권한 요청
                    ActivityCompat.requestPermissions(this, permission, PERMISSION_PHYSICAL_ACTIVITY)

                } else {
                    // 2. 사용자가 거절을 누른 경우, 요청을 다시 해봤자 다이얼로그가 안뜨기에, 설정 창으로 갈 수 있는 다이얼로그를 띄움
                    builder.show()
                }
            } else {
                // 3. 권한이 승인된 상태, 승인 되었다고 Toast 메시지 띄우기
                Toast.makeText(this, "Activity Sensor is Activated", Toast.LENGTH_SHORT).show()
            }
        }

        mypageSettingsActivityMainBinding.alarmNotificationCheck.setOnCheckedChangeListener { _, isChecked ->
            PREFS.setBoolean(ENABLE_PUSH_KEY, isChecked)
            updatePushNotificationWorker()
        }

        mypageSettingsActivityMainBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) { // 조작하고 있는 중에 발생

                mypageSettingsActivityMainBinding.renderTreeNo.text = progress.toString()
                PREFS.setString(RENDER_TREE_NO_KEY, progress.toString())

                // test
                Log.d(TAG, "onProgressChanged: your number of rendering tree is ${PREFS.getString(RENDER_TREE_NO_KEY, "")}")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { // 처음 터치했을 때 발생
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) { // 터치가 끝났을 때 발생
            }
        })
    }

    private fun updatePushNotificationWorker() {
        if (PREFS.getBoolean(ENABLE_PUSH_KEY, true)) {
            Log.d("bimoon","worker set")
            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                PushNotificationWorker::class.java,
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            ).setInitialDelay(1,TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                PushNotificationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
        } else {
            Log.d("bimoon","worker cancel")
            WorkManager.getInstance(applicationContext).cancelAllWork()
        }
    }

}