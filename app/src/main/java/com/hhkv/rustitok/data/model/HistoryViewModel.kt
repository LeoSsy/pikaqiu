package com.hhkv.rustitok.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhkv.rustitok.database.DataBaseHistoryHelper
import com.hhkv.rustitok.database.dao.HistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryViewModel : ViewModel() {

    private val _historys = MutableStateFlow<List<PoiData>>(emptyList())
    val historys: StateFlow<List<PoiData>> = _historys.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error : StateFlow<String?> = _error.asStateFlow()

    fun loadHistory(dao: HistoryDao){
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val list = withContext(Dispatchers.IO){
                    dao.getAll()
                }
                _historys.value = list.reversed()
            }catch (e: Exception){
                _error.value = e.message?: "加载数据失败！"
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHistory(selItems: List<PoiData>,dao: HistoryDao){
        try {
             for (item in selItems){
                 dao.delete(item.id!!)
             }
            _historys.value = dao.getAll()
        }catch (e: Exception){
            _error.value = e.message?: "删除数据失败！"
        }finally {
            _isLoading.value = false
        }
    }
}