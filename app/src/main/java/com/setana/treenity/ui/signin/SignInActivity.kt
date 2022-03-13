package com.setana.treenity.ui.signin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.setana.treenity.R
import com.setana.treenity.databinding.ActivitySignInBinding
import com.setana.treenity.ui.signin.fragment.GoogleSignInFragment
import com.setana.treenity.ui.signin.fragment.RegistrationFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    private val signInViewModel: SignInViewModel by viewModels()
    private lateinit var activitySignInBinding: ActivitySignInBinding

    companion object {
        private const val TAG = "SingInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupViewModel()
    }

    private fun setupUI() {
        activitySignInBinding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(activitySignInBinding.root)
        showGoogleSignInFragment()
    }

    private fun showGoogleSignInFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_frame, GoogleSignInFragment()).commit()
    }

    fun showRegistrationFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_frame, RegistrationFragment()).commit()
    }

    private fun setupViewModel() {
        signInViewModel.registerResponseLiveData.observe(this, { response ->
            response?.let {
                if (response.isSuccessful) {
                    Toast.makeText(this, "Registration Success", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registration Fail", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        })
    }
}