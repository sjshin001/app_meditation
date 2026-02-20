package com.app.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val progressDao = AppDatabase.getDatabase(application).progressDao()
    private val recordDao = AppDatabase.getDatabase(application).recordDao()
    private val meditationRecordDao = AppDatabase.getDatabase(application).meditationRecordDao()

    val allRounds: LiveData<List<ProgressRound>> = progressDao.getAllRounds()

    private val _selectedRoundId = MutableLiveData<Long>()
    val selectedRoundId: LiveData<Long> = _selectedRoundId

    val recordsForSelectedRound: LiveData<List<ProgressRecord>> = _selectedRoundId.switchMap { roundId ->
        progressDao.getRecordsForRound(roundId)
    }

    init {
        viewModelScope.launch {
            if (progressDao.getAllRoundsList().isEmpty()) {
                progressDao.insertRound(ProgressRound(round = 1))
                progressDao.insertRound(ProgressRound(round = 2))
                val newRoundId = progressDao.insertRound(ProgressRound(round = 3))
                _selectedRoundId.postValue(newRoundId)
            }
        }
    }

    fun addRound() {
        viewModelScope.launch {
            val lastRound = allRounds.value?.firstOrNull()?.round ?: 0
            val newRoundId = progressDao.insertRound(ProgressRound(round = lastRound + 1))
            _selectedRoundId.postValue(newRoundId)
        }
    }

    fun deleteCurrentRound() {
        selectedRoundId.value?.let {
            viewModelScope.launch {
                progressDao.deleteRoundById(it)
                val latestRound = progressDao.getAllRoundsList().firstOrNull()
                _selectedRoundId.postValue(latestRound?.id)
            }
        }
    }

    fun setSelectedRound(roundId: Long) {
        _selectedRoundId.value = roundId
    }

    fun getRecordForDay(day: Int): LiveData<ProgressRecord?> {
        val result = MutableLiveData<ProgressRecord?>()
        viewModelScope.launch {
            result.postValue(progressDao.getRecordForDay(selectedRoundId.value ?: 0, day))
        }
        return result
    }

    fun getTodaysProgressContent(): LiveData<String> {
        val result = MutableLiveData<String>()
        viewModelScope.launch {
            val today = Date()
            val startOfDay = getStartOfDay(today)
            val endOfDay = getEndOfDay(today)

            val bowCount = recordDao.getRecordsForDayNonLive(startOfDay, endOfDay).sumOf { it.count }
            val medDuration = meditationRecordDao.getRecordsForDayNonLive(startOfDay, endOfDay).sumOf { it.duration }

            result.postValue("절 : $bowCount\n명상 : ${medDuration}분")
        }
        return result
    }

    fun saveRecord(day: Int, content: String, date: Date) {
        viewModelScope.launch {
            val roundId = selectedRoundId.value ?: return@launch
            val existingRecord = progressDao.getRecordForDay(roundId, day)
            val recordToSave = existingRecord?.copy(content = content, date = date) 
                                ?: ProgressRecord(roundId = roundId, day = day, date = date, content = content)
            progressDao.insertOrUpdateRecord(recordToSave)
        }
    }

    fun clearContentForDay(day: Int) {
        viewModelScope.launch {
            val roundId = selectedRoundId.value ?: return@launch
            val existingRecord = progressDao.getRecordForDay(roundId, day)
            if (existingRecord != null) {
                val clearedRecord = existingRecord.copy(content = "")
                progressDao.insertOrUpdateRecord(clearedRecord)
            }
        }
    }

    private fun getStartOfDay(date: Date): Date { val c = Calendar.getInstance(); c.time = date; c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.time }
    private fun getEndOfDay(date: Date): Date { val c = Calendar.getInstance(); c.time = date; c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.time }
}