package com.setana.treenity.ui.mypage

import android.util.Log
import androidx.lifecycle.*
import com.setana.treenity.TreenityApplication.Companion.DAILY_WALK_LOG
import com.setana.treenity.data.api.dto.mypage.tree.MyTreeItem
import com.setana.treenity.data.api.dto.mypage.user.User
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import com.setana.treenity.data.repository.TreeRepository
import com.setana.treenity.data.repository.UserRepository
import com.setana.treenity.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val userRepository: UserRepository, private val treeRepository: TreeRepository
) : ViewModel() {

    private val _userLiveData: MutableLiveData<User> = MutableLiveData()
    val userLiveData: LiveData<User> = _userLiveData

    private val _myTreesLiveData: MutableLiveData<List<MyTreeItem>> = MutableLiveData()
    val myTreesLiveData: LiveData<List<MyTreeItem>> = _myTreesLiveData

    private val _myWalkLogsLiveData: MutableLiveData<List<WalkLog>> = MutableLiveData()
    val myWalkLogsLiveData: LiveData<List<WalkLog>> = _myWalkLogsLiveData

    private val _showErrorToast = MutableLiveData<Event<String>>()
    val showErrorToast: LiveData<Event<String>> = _showErrorToast

    var steps = MutableLiveData<Int>()


    init {
        steps.value = DAILY_WALK_LOG[SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())]?.toInt()
    }

    fun increase(){
        steps.value =
            DAILY_WALK_LOG[SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())]?.toInt()
                ?.plus(1)
    }

    private fun setToastMessage(content: String) {
        _showErrorToast.postValue(Event(content))
    }

//    var walkLogList = ArrayList<WalkLog>()
//    var job: Job? = null
//    var item = WalkLog("",0,0)
//    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
//        Log.d("tag","Exception handled: ${throwable.localizedMessage}")
//    }


    fun getUserInfo(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }
        
        withContext(Dispatchers.IO + handler) {

            userRepository.getUserData(userId.toString()).let { response ->
                if (response.isSuccessful) {
                    setToastMessage("success bringing user data!!")
                    _userLiveData.postValue(response.body())
                } else {
                    Log.d("tag", "getUserInfo: has an error receiving data")
                }
            }
        }
    }


    fun getTreeData(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
          setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
          throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }

        withContext(Dispatchers.IO + handler) {

            treeRepository.getUserTrees(userId.toString()).let { response ->
                if (response.isSuccessful) {
                    setToastMessage("success bringing My Tree data!!")
                    _myTreesLiveData.postValue(response.body())
                } else {
                    Log.d("tag", "getUserInfo: has an error receiving data")
                }
            }
        }
    }


    fun getMyWalkLogs(userId:Long) = viewModelScope.launch(Dispatchers.Main) {

        val handler = CoroutineExceptionHandler { _, throwable ->
            setToastMessage("데이터를 불러오는 중 오류가 발생하였습니다.")
            throwable.message?.let { Log.d("MyPageViewModel.kt", it) }
        }

        val response = userRepository.getUserWalkLogs(userId.toString())

        withContext(Dispatchers.IO + handler) {

            if (response.isSuccessful) {
                setToastMessage("success bringing WalkLog data!!")
                _myWalkLogsLiveData.postValue(response.body())
            } else {
                Log.d("tag", "getMyWalkLogs: has an error receiving data")
            }
        }
    }

//    fun getMyWalkLogs(userId:Long) {
//        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
//            val response = userRepository.getUserWalkLogs(userId.toString())
//            withContext(Dispatchers.Main) {
//
//                if (response.isSuccessful) {
//
//                    // Walk Log 중 최근 것 7개만 가져옴(일주일 고려)
//                    for(i in response.body()?.size?.downTo(1)!!) {
//                        item = response.body()!![response.body()!!.size - i]
//                        walkLogList.add(item)
//
//                        // test
//                        Log.d("tag", "getWalkLog : $item")
//                    }
//
//                    _myWalkLogsLiveData.postValue(walkLogList)
//
////                    _myWalkLogsLiveData.postValue(response.body())
//
//                } else {
//                    Log.d("tag","Error : ${response.message()} ")
//                }
//            }
//        }
//    }
}