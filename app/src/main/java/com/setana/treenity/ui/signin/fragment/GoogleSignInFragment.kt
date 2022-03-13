package com.setana.treenity.ui.signin.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.setana.treenity.BuildConfig
import com.setana.treenity.R
import com.setana.treenity.ui.signin.SignInActivity

class GoogleSignInFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInActivity: SignInActivity
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "SingInActivity - GoogleSignInFragment"
        private const val RC_SIGN_IN = 1000
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signInActivity = context as SignInActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.OAUTH_WEB_CLIENT_KEY)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(signInActivity, gso)

        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in_google, container, false)

        val signInButton = view.findViewById<SignInButton>(R.id.sign_in_button)

        signInButton.setOnClickListener {
            signIn()
        }

        return view
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(signInActivity, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    signInActivity.showRegistrationFragment()
                    Log.d(TAG, "signInWithCredential:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        signInActivity,
                        "signInWithCredential:failure",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}