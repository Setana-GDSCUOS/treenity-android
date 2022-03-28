package com.setana.treenity.ui.store

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.data.repository.StoreRepository
import com.setana.treenity.data.repository.UserRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class StoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository, private val userRepository: UserRepository
) : ViewModel() {

    private val _storeItemLiveData: MutableLiveData<List<StoreItem>> = MutableLiveData()
    val storeItemLiveData: LiveData<List<StoreItem>> = _storeItemLiveData

    private val _userLiveData: MutableLiveData<User> = MutableLiveData()
    val userLiveData: LiveData<User> = _userLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }


    fun getStoreItems() = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("Error has been occurred bringing store items")
            throwable.message?.let { Log.d("StoreViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            val response = storeRepository.getStoreItems()

            if (response.isSuccessful) {
                setToastMessage("success bringing store items")
                _storeItemLiveData.postValue(response.body())
            } else {
                Log.d("tag", "getMyTrees: has an error receiving data")
            }
        }

    }


    fun getUserInfo(userId: Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("Error has been occurred bringing user info")
            throwable.message?.let { Log.d("StoreViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            userRepository.getUserData(userId.toString()).let { response ->
                if (response.isSuccessful) {
                    setToastMessage("success bringing user data")
                    _userLiveData.postValue(response.body())
                } else {
                    Log.d("tag", "getUserInfo: has an error receiving data")
                }
            }
        }
    }


}