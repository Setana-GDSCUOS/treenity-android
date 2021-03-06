package com.setana.treenity.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.GetAroundTreeResponseDTO
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val treeRepository: TreeRepository
) : ViewModel() {
    private val _treeListLiveData: MutableLiveData<List<GetAroundTreeResponseDTO>> =
        MutableLiveData()
    val treeListLiveData: LiveData<List<GetAroundTreeResponseDTO>> = _treeListLiveData

    private val _treeBookmarkResponseLiveData: MutableLiveData<Response<Void>> = MutableLiveData()
    val treeBookmarkResponseLiveData: LiveData<Response<Void>> = _treeBookmarkResponseLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }

    fun listAroundTrees(lat: Double, lng: Double, userId: Long) =
        viewModelScope.launch(Dispatchers.Main) {
            val handler = CoroutineExceptionHandler { _, throwable ->
                setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
                throwable.message?.let { Log.d("MapViewModel.kt", it) }
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

    fun updateTreeBookmarkState(userId: Long, treeId: Long, bookmark: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            val handler = CoroutineExceptionHandler { _, throwable ->
                setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
                throwable.message?.let { Log.d("MapViewModel.kt", it) }
            }

            withContext(Dispatchers.IO + handler) {
                val response = if (bookmark) {
                    treeRepository.postTreeBookmark(userId, treeId)
                } else {
                    treeRepository.deleteTreeBookmark(userId, treeId)
                }
                _treeBookmarkResponseLiveData.postValue(response)
            }
        }
}