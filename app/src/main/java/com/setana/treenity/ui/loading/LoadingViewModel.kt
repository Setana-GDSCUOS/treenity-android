package com.setana.treenity.ui.loading

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import com.setana.treenity.data.api.dto.LoginByFirebaseTokenResponseDTO
import com.setana.treenity.data.api.dto.RegisterCurrentFirebaseUserRequestDTO
import com.setana.treenity.data.repository.UserRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoadingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userInfoLiveData: MutableLiveData<Response<LoginByFirebaseTokenResponseDTO>> = MutableLiveData()
    val userInfoLiveData: LiveData<Response<LoginByFirebaseTokenResponseDTO>> = _userInfoLiveData

    private val _registerResponseLiveData: MutableLiveData<Response<Void>> = MutableLiveData()
    val registerResponseLiveData: LiveData<Response<Void>> = _registerResponseLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.value = Event(content)
    }

    fun loginByFirebaseToken() = viewModelScope.launch(Dispatchers.Main) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("SetupViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {
            val response = userRepository.loginByFirebaseToken()
            _userInfoLiveData.postValue(response)
        }
    }

    fun registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO: RegisterCurrentFirebaseUserRequestDTO) =
        viewModelScope.launch(Dispatchers.Main) {
            val handler = CoroutineExceptionHandler { _, throwable ->
                setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
                throwable.message?.let { Log.d("SignInViewModel.kt", it) }
            }

            withContext(Dispatchers.IO + handler) {
                val response =
                    userRepository.registerCurrentFirebaseUser(registerCurrentFirebaseUserRequestDTO)
                _registerResponseLiveData.postValue(response)
            }
        }
}