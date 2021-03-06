package com.setana.treenity.ui.purchase

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.store.StoreItem
import com.setana.treenity.data.repository.StoreRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val _storeItemLiveData: MutableLiveData<List<StoreItem>> = MutableLiveData()
    val storeItemLiveData: LiveData<List<StoreItem>> = _storeItemLiveData

    private val _buyItemResponseLiveData: MutableLiveData<Response<Void>> = MutableLiveData()
    val buyItemResponseLiveData: LiveData<Response<Void>> = _buyItemResponseLiveData

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
                setToastMessage("success bringing store items!!")
                _storeItemLiveData.postValue(response.body())
            } else {
                Log.d("tag", "getMyTrees: has an error receiving data")
            }
        }

    }

    fun buyItem(userId: Long, storeItem: StoreItem) =
        viewModelScope.launch(Dispatchers.Main) {

            val handler = CoroutineExceptionHandler { _, throwable ->
                setToastMessage("Error has been occurred buying store items")
                throwable.message?.let { Log.d("StoreViewModel.kt", it) }
            }

            withContext(Dispatchers.IO + handler) {
                val response =
                    storeRepository.buyTree(userId.toString(), storeItem)
                _buyItemResponseLiveData.postValue(response)
            }
        }
}