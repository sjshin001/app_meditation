package com.app.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MeditationViewModel(application: Application) : AndroidViewModel(application) {

    private val meditationDao = AppDatabase.getDatabase(application).meditationDao()
    private val meditationRecordDao = AppDatabase.getDatabase(application).meditationRecordDao()

    val meditations: LiveData<List<Meditation>> = meditationDao.getAll()

    private val _currentDisplayCalendar = MutableLiveData(Calendar.getInstance())
    val currentDisplayCalendar: LiveData<Calendar> = _currentDisplayCalendar

    private val _monthlyRecordSummaries = MediatorLiveData<List<MeditationRecordSummary>>()
    val monthlyRecordSummaries: LiveData<List<MeditationRecordSummary>> = _monthlyRecordSummaries

    private val _selectedMeditationRecords = MutableLiveData<List<MeditationRecord>?>()
    val selectedMeditationRecords: LiveData<List<MeditationRecord>?> = _selectedMeditationRecords

    val nextMeditationName: LiveData<String> = meditations.map {
        val prefix = "명상"
        val maxNum = it
            .filter { m -> m.name.startsWith(prefix) }
            .mapNotNull { m -> m.name.removePrefix(prefix).toIntOrNull() }
            .maxOrNull() ?: 0
        "$prefix${maxNum + 1}"
    }

    init {
        viewModelScope.launch {
            if (meditationDao.getAllList().isEmpty()) {
                addMeditation("10분 명상", 10)
                addMeditation("30분 명상", 30)
            }
        }
        changeMonth(0)
    }

    fun addMeditation(name: String, time: Int) {
        viewModelScope.launch {
            meditationDao.insert(Meditation(name = name, time = time))
        }
    }

    fun updateMeditation(meditation: Meditation) {
        viewModelScope.launch {
            meditationDao.update(meditation)
        }
    }

    fun deleteMeditation(meditation: Meditation) {
        viewModelScope.launch {
            meditationDao.deleteById(meditation.id)
        }
    }

    fun saveMeditationRecord(duration: Int) {
        if (duration > 0) {
            viewModelScope.launch {
                meditationRecordDao.insert(MeditationRecord(date = Date(), duration = duration))
            }
        }
    }

    fun changeMonth(amount: Int) {
        val calendar = _currentDisplayCalendar.value ?: Calendar.getInstance()
        calendar.add(Calendar.MONTH, amount)
        _currentDisplayCalendar.value = calendar

        val startOfMonth = getStartOfMonth(calendar)
        val endOfMonth = getEndOfMonth(calendar)

        _monthlyRecordSummaries.removeSource(meditationRecordDao.getMeditationRecordSummaries(startOfMonth, endOfMonth))
        _monthlyRecordSummaries.addSource(meditationRecordDao.getMeditationRecordSummaries(startOfMonth, endOfMonth)) {
            _monthlyRecordSummaries.value = it
        }
    }

    fun onDateClicked(summary: MeditationRecordSummary) {
        viewModelScope.launch {
            val ids = summary.ids.split(",").map { it.toLong() }
            _selectedMeditationRecords.postValue(meditationRecordDao.getRecordsByIds(ids))
        }
    }

    fun onDetailDialogDismissed() {
        _selectedMeditationRecords.value = null
    }

    private fun getStartOfMonth(calendar: Calendar): Date {
        val newCal = calendar.clone() as Calendar
        newCal.set(Calendar.DAY_OF_MONTH, 1)
        newCal.set(Calendar.HOUR_OF_DAY, 0)
        newCal.set(Calendar.MINUTE, 0)
        newCal.set(Calendar.SECOND, 0)
        newCal.set(Calendar.MILLISECOND, 0)
        return newCal.time
    }

    private fun getEndOfMonth(calendar: Calendar): Date {
        val newCal = calendar.clone() as Calendar
        newCal.set(Calendar.DAY_OF_MONTH, newCal.getActualMaximum(Calendar.DAY_OF_MONTH))
        newCal.set(Calendar.HOUR_OF_DAY, 23)
        newCal.set(Calendar.MINUTE, 59)
        newCal.set(Calendar.SECOND, 59)
        newCal.set(Calendar.MILLISECOND, 999)
        return newCal.time
    }
}