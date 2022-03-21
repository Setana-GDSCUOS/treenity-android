package com.setana.treenity.ui.ar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.*
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ArViewModel @Inject constructor(
    private val treeRepository: TreeRepository
): ViewModel() {
    private val _treeListLiveData: MutableLiveData<List<GetAroundArTreeResponseDTO>> = MutableLiveData()
    val treeListLiveData: LiveData<List<GetAroundArTreeResponseDTO>> = _treeListLiveData

    private val _treeInformationLiveData: MutableLiveData<GetTreeInformationDTO> = MutableLiveData()
    val treeInformationLiveData: LiveData<GetTreeInformationDTO> = _treeInformationLiveData

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
            val response = treeRepository.getAroundArTrees(lat, lng, userId)
            if (response.isSuccessful) {
                setToastMessage("loading trees around, please look around")
                _treeListLiveData.postValue(response.body())
            } else {
                setToastMessage(response.message())
            }
        }
    }

    fun postHostedTree(dto: PostTreeDTO) = viewModelScope.launch(Dispatchers.Main){
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.postTree(dto)
            /*if (response.code() == 200) {
                //_treeIdLiveData.postValue(response.body)
            } else if(response.code()==500){

            }
            else{
                setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            }*/
            when(response.code()){
              200  -> {

              }
              500 -> {
                  setToastMessage("주변에 나무가 너무 많습니다. 다른 곳에 나무를 심어주세요")
              }
              else -> {

              }
            }
        }
    }

    fun getTreeInformation(id:Long, treeId: Long, userId: Long) = viewModelScope.launch ( Dispatchers.Main ){
        // 서버쪽 구성때문에 두 개로 나눠졌는데 둘 다 같은 treeId입니다
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.getTreeInformation(id, treeId, userId)
            if (response.isSuccessful) {
                _treeInformationLiveData.postValue(response.body())
            } else {
                setToastMessage(response.message())
            }
        }
    }

    fun waterTree(treeId:Long, waterTreeDTO: WaterTreeDTO)  = viewModelScope.launch ( Dispatchers.Main ){
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let {Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.waterTree(treeId,waterTreeDTO)
            if (response.isSuccessful) {
                // Todo Not implemented yet
                // 물 잘 주면 뭐 해야되냐? 그냥 성공했다고 알려주고 성공했으면 모델 리로드해야지뭐
            } else {
                setToastMessage(response.message())
            }
        }
    }

    fun putTreeInfo(userId:Long, treeId:Long,putTreeInfoDTO: PutTreeInfoDTO)= viewModelScope.launch ( Dispatchers.Main ){
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let {Log.d("ArViewModel.kt", it) }
        }
        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.putTreeInfo(userId,treeId,putTreeInfoDTO)
            if (response.isSuccessful) {
                // Todo Not implemented yet
            } else {
                setToastMessage(response.message())
            }
        }
    }
}