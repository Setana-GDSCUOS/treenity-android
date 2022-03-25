package com.setana.treenity.ui.mytreelist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.util.Event
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class TreeListViewModel @Inject constructor(
    private val treeRepository: TreeRepository
): ViewModel() {

    private val _myTreeListLiveData: MutableLiveData<List<MyTreeItem>> = MutableLiveData()
    val myTreeListLiveData: LiveData<List<MyTreeItem>> = _myTreeListLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    val userId = PREFS.getLong(USER_ID_KEY, -1)

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }


    fun getTreeList(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            treeRepository.getTreeData(userId.toString()).let { response ->
                if (response.isSuccessful) {
                    setToastMessage("success bringing MyTreeList data!!")
                    _myTreeListLiveData.postValue(response.body())
                } else {
                    Log.d("tag", "getUserInfo: has an error receiving data")
                }
            }
        }
    }

}