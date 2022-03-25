package com.setana.treenity.ui.ar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.data.repository.UserRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(
    private val treeRepository: TreeRepository,
    private val userRepository: UserRepository
): ViewModel() {
    private val _treeListLiveData: MutableLiveData<List<GetAroundTreeResponseDTO>> = MutableLiveData()
    val treeListLiveData: LiveData<List<GetAroundTreeResponseDTO>> = _treeListLiveData

    private val _treeInformationResponseLiveData: MutableLiveData<GetTreeInformationResponseDTO> = MutableLiveData()
    val treeInformationResponseLiveData: LiveData<GetTreeInformationResponseDTO> = _treeInformationResponseLiveData

    private val _postTreeResponseLiveData: MutableLiveData<GetTreeInformationResponseDTO> = MutableLiveData()
    val postTreeResponseLiveData: LiveData<GetTreeInformationResponseDTO> = _postTreeResponseLiveData

    private val _userItemListLiveData: MutableLiveData<List<GetUserItemResponseDTO>> = MutableLiveData()
    val userItemListLiveData: LiveData<List<GetUserItemResponseDTO>> = _userItemListLiveData

    //private val _treeIdLiveData: MutableLiveData<Long> = MutableLiveData()
    //val treeIdLiveData: LiveData<Long> = _treeIdLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }

    fun listAroundTrees(lat: Double, lng: Double, userId: Long) = viewModelScope.launch(Dispatchers.Main) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.getAroundTrees(lat, lng, userId)
            if (response.isSuccessful) {
                _treeListLiveData.postValue(response.body())
            } else {
                setToastMessage(response.message())
            }
        }
    }

    fun postHostedTree(userId:Long, requestDto: PostTreeRequestDTO) = viewModelScope.launch(Dispatchers.Main){
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.postTree(userId,requestDto)
            when(response.code()){
              200  -> {
                  _postTreeResponseLiveData.postValue(response.body())
              }
              500 -> {
                  setToastMessage("주변에 나무가 너무 많습니다. 다른 곳에 나무를 심어주세요")
              }
              else -> {
                  setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
              }
            }
        }
    }

    fun getTreeInformation(treeId: Long) = viewModelScope.launch ( Dispatchers.Main ){
        // 서버쪽 구성때문에 두 개로 나눠졌는데 둘 다 같은 treeId입니다
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.getTreeInformation(treeId)
            if (response.isSuccessful) {
                _treeInformationResponseLiveData.postValue(response.body())
            } else {
                setToastMessage("Error occured while loading data : ${response.code().toString()}")
            }
        }
    }

    fun waterTree(userId: Long, treeId:Long, waterTreeRequestDTO: WaterTreeRequestDTO)  = viewModelScope.launch ( Dispatchers.Main ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.waterTree(userId, treeId, waterTreeRequestDTO)
            when (response.code()) {
                200 -> {
                    setToastMessage("You have watered your tree!")
                }
                406 -> {
                    setToastMessage("You have not enough buckets")
                }
                else -> {
                    setToastMessage("Error occured while loading data : ${response.code().toString()}")

                }
            }
        }
    }

    fun putTreeInfo(userId:Long, treeId:Long, putTreeInfoRequestDTO: PutTreeInfoRequestDTO)= viewModelScope.launch ( Dispatchers.Main ){
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let {Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.putTreeInfo(userId,treeId,putTreeInfoRequestDTO)
            if (response.isSuccessful) {
                // Todo Not implemented yet
            } else {
                setToastMessage("Error occured while loading data : ${response.code().toString()}")
            }
        }
    }

    fun getUserItems(userId: Long) = viewModelScope.launch(Dispatchers.Main) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {
            val response = userRepository.getUserItems(userId)
            if (response.isSuccessful) {
                _userItemListLiveData.postValue(response.body())
            } else {
                setToastMessage("Error occured while loading data : ${response.code().toString()}")
            }
        }
    }

}