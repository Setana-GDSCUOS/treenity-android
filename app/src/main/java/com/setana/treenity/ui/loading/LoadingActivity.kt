package com.setana.treenity.ui.loading

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.setana.treenity.R
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.api.dto.UpdateUserWalkLogsRequestDTO
import com.setana.treenity.databinding.ActivityLoadingBinding
import com.setana.treenity.service.StepDetectorService
import com.setana.treenity.ui.ar.ArActivity
import com.setana.treenity.ui.signin.SignInActivity
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PermissionUtils
import com.setana.treenity.util.PreferenceManager.Companion.DAILY_WALK_LOG_KEY
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class LoadingActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var permissionDenied = false
    private lateinit var activityLoadingBinding: ActivityLoadingBinding
    private val loadingViewModel: LoadingViewModel by viewModels()

    companion object {
        private const val TAG = "LoadingActivity"
        private val permissions = arrayOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        private const val PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupViewModel()
        initializeAuth()
    }

    override fun onStart() {
        super.onStart()
        verifyUser()
    }

    /**
     * Start Activity or Service Methods
     */
    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    private fun startArActivity() {
        val intent = Intent(this, ArActivity::class.java)
        startActivity(intent)
    }

    private fun startStepDetectorService() {
        val intent = Intent(this, StepDetectorService::class.java)
        startService(intent)
    }

    /**
     * Activity Setup
     */
    private fun setupUI() {
        activityLoadingBinding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(activityLoadingBinding.root)

        val lottieAnimationView = activityLoadingBinding.lottieLoading
        lottieAnimationView.setAnimation("loading.json")
        lottieAnimationView.repeatCount = LottieDrawable.INFINITE
        lottieAnimationView.playAnimation()
    }

    private fun setupViewModel() {
        loadingViewModel.userInfoLiveData.observe(this, { response ->
            response?.let {
                if (it.isSuccessful) {
                    Log.d("SetupViewModel", "로그인 성공")

                    // If login success, check permission and start AR activity
                    val userId = it.body()?.userId
                    userId?.let { uid ->
                        // User ID 저장
                        // 꺼내쓸 때는 PREFS.getLong(USER_ID_KEY, -1) 후 -1인지 아닌지 확인
                        // 로그아웃 시 setLong -1 필요
                        PREFS.setLong(USER_ID_KEY, uid)

                        // 저장된 걸음 수 불러오기 (from SharedPreference)
                        val hashMapString = PREFS.getString(DAILY_WALK_LOG_KEY, "")
                        val type = object : TypeToken<HashMap<String, String>>() {}.type
                        val hashMap = Gson().fromJson<HashMap<String, String>>(hashMapString, type)
                            ?: hashMapOf(
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.US
                                ).format(Date()) to "0"
                            )

                        val updateUserWalkLogsRequestDTO = UpdateUserWalkLogsRequestDTO(hashMap)

                        loadingViewModel.updateUserWalkLogs(
                            uid.toString(),
                            updateUserWalkLogsRequestDTO
                        )
                    }
                } else {
                    Log.d("SetupViewModel", response.message())
                    // If login fail
                    showRegisterDialog()
                }
            }
        })

        loadingViewModel.updateWalkLogsResponseLiveData.observe(this, { response ->
            response?.let {
                if (it.isSuccessful) {
                    Log.d(TAG, "걸음 수 전송 성공")
                    if (checkAndRequestPermissions()) {
                        startStepDetectorService()
                        // startArActivity()
                    }
                } else {
                    Log.d(TAG, "걸음 수 전송 실패")
                }
            }
        })

        loadingViewModel.registerResponseLiveData.observe(this, { response ->
            response?.let {
                if (it.isSuccessful) {
                    Log.d(TAG, "회원가입 성공")
                    verifyUser()
                } else {
                    Log.d(TAG, "회원가입 실패")
                    showRegisterDialog()
                }
            }
        })

        loadingViewModel.showErrorToast.observe(this, EventObserver {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    @SuppressLint("InflateParams")
    private fun showRegisterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registration, null)
        val usernameEditText = dialogView.findViewById<EditText>(R.id.et_username)
        val userProfileImageView = dialogView.findViewById<ImageView>(R.id.iv_user_profile_image)
        val currentUser = auth.currentUser

        currentUser?.let { user ->
            usernameEditText.setText(user.displayName)
            userProfileImageView.load(user.photoUrl) {
                transformations(CircleCropTransformation())
            }

            AlertDialog.Builder(this).apply {
                setTitle("Registration")
                setView(dialogView)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    val registerCurrentFirebaseUserRequestDTO =
                        RegisterCurrentFirebaseUserRequestDTO(usernameEditText.text.toString())
                    loadingViewModel.registerCurrentFirebaseUser(
                        registerCurrentFirebaseUserRequestDTO
                    )
                }
                setNegativeButton(
                    android.R.string.cancel
                ) { dialog, _ -> dialog.cancel() }
                show()
            }
        }
    }

    /**
     * Auth
     */
    private fun initializeAuth() {
        // Firebase 오브젝트 초기화
        auth = Firebase.auth
        // 앱 데이터 삭제 후, 혹은 외부에서 로그아웃 이후 다른 계정으로 로그인 시 이전 uid로 요청하는 현상을 방지하기 위해 초기화
        PREFS.setLong(USER_ID_KEY, -1)
    }

    private fun verifyUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startSignInActivity()
        } else {
            loadingViewModel.loginByFirebaseToken()
        }
    }

    /**
     * Permission Check
     */
    private fun checkAndRequestPermissions(): Boolean {
        Log.d(TAG, "check permissions")
        val listPermissionsNeeded = arrayListOf<String>()
        permissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(it)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            Log.d(TAG, "listPermissionsNeeded isNotEmpty")
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            Toast.makeText(this, "All Permission Granted", Toast.LENGTH_SHORT).show()
            startStepDetectorService()
            // startArActivity()
        } else {
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    private fun showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true)
            .show(supportFragmentManager, "Missing Permission")
    }
}