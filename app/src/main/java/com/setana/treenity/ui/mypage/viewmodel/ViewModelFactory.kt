package com.setana.treenity.ui.mypage.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.setana.treenity.data.repository.mypage.WalkLogRepository

class ViewModelFactory constructor(private val repository: WalkLogRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(WalkLogViewModel::class.java)) {
            WalkLogViewModel(this.repository) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}