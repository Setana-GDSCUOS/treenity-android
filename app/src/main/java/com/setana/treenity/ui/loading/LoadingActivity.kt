package com.setana.treenity.ui.loading

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieDrawable
import com.google.firebase.auth.FirebaseAuth
import com.setana.treenity.databinding.ActivityLoadingBinding
import com.setana.treenity.ui.ar.ArActivity
import com.setana.treenity.ui.signin.SignInActivity
import com.setana.treenity.util.EventObserver
import com.setana.treenity.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingActivity : AppCompatActivity() {
    private var permissionDenied = false
    private lateinit var activityLoadingBinding: ActivityLoadingBinding
    private val loadingViewModel: LoadingViewModel by viewModels()

    companion object {
        private const val TAG = "LoadingActivity"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    /**
     * 1. Activity Setup
     * 2. Auth
     * 3. Permission
     * 4. Send Walk Data
     * 5. Service Check?
     * 6. Start Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupUI()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        verifyUser()
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

                    // If login success, start AR activity
                    val userId = it.body()?.userId
                    userId?.let {
                        // TODO Intent
                    }
                    startArActivity()
                } else {
                    Log.d("SetupViewModel", response.message())

                    // If login fail, start SignInActivity
                    startSignInActivity()
                }
            }
        })
        loadingViewModel.showErrorToast.observe(this, EventObserver{
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Auth
     */
    private fun verifyUser() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        } else {
            // val userRequest = RegisterCurrentFirebaseUserRequestDTO(currentUser.displayName.toString())
            loadingViewModel.loginByFirebaseToken()
        }
    }

    private fun startSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
    }

    /**
     * Permission & Start ArActivity
     */
    private fun startArActivity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            //val intent = Intent(this, ArActivity::class.java)
            //startActivity(intent)
        } else {
            PermissionUtils.requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            startArActivity()
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
            .show(supportFragmentManager, "dialog")
    }
}