package com.setana.treenity.ui.mypage.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.setana.treenity.TreenityApplication.Companion.PREFS
import com.setana.treenity.data.api.dto.mypage.walklog.WalkLog
import com.setana.treenity.data.repository.mypage.WalkLogRepository
import com.setana.treenity.util.PreferenceManager.Companion.USER_ID_KEY
import kotlinx.coroutines.*

class WalkLogViewModel constructor(private val repository: WalkLogRepository): ViewModel() {

    var walkLogs = MutableLiveData<List<WalkLog>>()
    var walkLogList = ArrayList<WalkLog>()
    var job: Job? = null
    var item = WalkLog("",0,0)
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("tag","Exception handled: ${throwable.localizedMessage}")
    }


    fun getWalkLog() {
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val response = repository.getWalkLogs()
            withContext(Dispatchers.Main) {

                if (response.isSuccessful) {

                    // Walk Log 중 최근 것 7개만 가져옴(일주일 고려)
                    for(i in 7 downTo 1) {
                        item = response.body()!![response.body()!!.size - i]
                        walkLogList.add(item)

                        // test
                        Log.d("tag", "getWalkLog : $item")
                    }

                    walkLogs.postValue(walkLogList)

                } else {
                    Log.d("tag","Error : ${response.message()} ")
                }
            }
        }
    }
}