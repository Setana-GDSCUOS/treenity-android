package com.setana.treenity.ui.ar

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.GetAroundArTreeResponseDTO
import com.setana.treenity.data.api.dto.PostTreeDTO
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

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.value = Event(content)
    }

    fun listAroundTrees(lat: Double, lng: Double) = viewModelScope.launch(Dispatchers.Main) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("ArViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {
            val response = treeRepository.getAroundArTrees(lat, lng)
            if (response.isSuccessful) {
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
            if (response.isSuccessful) {
                // Todo 등록된 나무의 id를 받아서 View 에 전송 - 이건 live 가 필요할까? 아무래도 리로드가...
            } else {
                setToastMessage(response.message())
            }
        }
    }


    // 물주기는 어떤 식으로 작성해야
}