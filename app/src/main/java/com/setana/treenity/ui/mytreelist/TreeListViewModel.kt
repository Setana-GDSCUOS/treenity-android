package com.setana.treenity.ui.mytreelist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
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
class TreeListViewModel @Inject constructor(
    private val treeRepository: TreeRepository
): ViewModel() {

    private val _myTreeListLiveData: MutableLiveData<Response<List<MyTreeItem>>> = MutableLiveData()
    val myTreeListLiveData: LiveData<Response<List<MyTreeItem>>> = _myTreeListLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }


    fun getTreeList(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            val response = treeRepository.getTreeData(userId)

            if (response.isSuccessful) {
                setToastMessage("success bringing MyTree data!!")
                _myTreeListLiveData.postValue(response)
            } else {
                Log.d("tag", "getMyTrees: has an error receiving data")
            }
        }
    }
}