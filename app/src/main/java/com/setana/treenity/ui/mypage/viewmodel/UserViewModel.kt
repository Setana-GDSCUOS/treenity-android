package com.setana.treenity.ui.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.repository.mypage.UserRepository
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel
@Inject
constructor(private val repository: UserRepository): ViewModel(){

    private val _resp = MutableLiveData<User>()
    val userResp: LiveData<User>
        get() = _resp

    init {
        getUserData()
    }

    private fun getUserData()= viewModelScope.launch {
        repository.getUserData().let { response->

            if(response.isSuccessful) {
                _resp.postValue(response.body())
            }else {
                Log.d("tag", "getUserDataError: ${response.message()}")
            }
        }
    }


}