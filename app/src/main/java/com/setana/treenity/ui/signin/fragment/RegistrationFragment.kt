package com.setana.treenity.ui.signin.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.setana.treenity.R
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.ui.signin.SignInActivity
import com.setana.treenity.ui.signin.SignInViewModel

class RegistrationFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var signInActivity: SignInActivity
    private lateinit var registerButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var userProfileImage: ImageView
    private val signInViewModel: SignInViewModel by activityViewModels()

    companion object {
        private const val TAG = "SingInActivity - RegistrationFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        signInActivity = context as SignInActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        usernameEditText.setText(currentUser?.displayName)
        userProfileImage.load(currentUser?.photoUrl) {
            transformations(CircleCropTransformation())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register_setana, container, false)

        registerButton = view.findViewById(R.id.btn_request_register)
        usernameEditText = view.findViewById(R.id.et_username)
        userProfileImage = view.findViewById(R.id.iv_user_profile_image)

        registerButton.setOnClickListener {
            val usernameInput = usernameEditText.text.toString()
            val registerCurrentFirebaseUserRequestDTO =
                RegisterCurrentFirebaseUserRequestDTO(usernameInput)
            signInViewModel.registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO)
        }

        return view
    }
}