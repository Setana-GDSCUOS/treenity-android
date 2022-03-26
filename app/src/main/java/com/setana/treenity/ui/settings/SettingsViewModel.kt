package com.setana.treenity.ui.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.mypage.user.User
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
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    private val _userLiveData: MutableLiveData<Response<Void>> = MutableLiveData()
    val userLiveData: LiveData<Response<Void>> = _userLiveData

    private val _userNameLiveData: MutableLiveData<User> = MutableLiveData()
    val userNameLiveData: LiveData<User> = _userNameLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }


    fun updateUserName(userId: String, username: String) =
        viewModelScope.launch(Dispatchers.Main) {

            val handler = CoroutineExceptionHandler { _, throwable ->
                setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
                throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
            }

            withContext(Dispatchers.IO + handler) {
                val response =
                    userRepository.changeUserName(userId, username)
                _userLiveData.postValue(response)
            }
        }

    fun getUserInfo(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            userRepository.getUserData(userId.toString()).let { response ->
                if (response.isSuccessful) {
                    setToastMessage("success bringing user data!!")
                    _userNameLiveData.postValue(response.body())
                } else {
                    Log.d("tag", "getUserInfo: has an error receiving data")
                }
            }
        }
    }
}