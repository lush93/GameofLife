package com.nogotech.gameoflife.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {
    private val _stateCount = MutableLiveData<Long>().apply {
        value = 0
    }
    val stateCount get() = _stateCount

    private val _isRunning = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isRunning get() = _isRunning

    private val _frameRate = MutableLiveData<Int>().apply {
        value = 10
    }
    val frameRate get() = _frameRate

    private fun nextState(){
        viewModelScope.launch {
            nextStateSuspend()
        }
    }

    private suspend fun nextStateSuspend(){
        withContext(Dispatchers.Main) {
            while(_isRunning.value!!){
                delay(1000/_frameRate.value!!.toLong())
                _stateCount.postValue(_stateCount.value!! + 1L)
            }
        }
    }

    fun start(){
        _isRunning.value = true
        nextState()
    }

    fun pause(){
        _isRunning.value = false
    }
}