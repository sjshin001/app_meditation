package com.app.meditation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class BowViewModel(application: Application) : AndroidViewModel(application) {

    private val recordDao = AppDatabase.getDatabase(application).recordDao()
    private val settingsDao = AppDatabase.getDatabase(application).bowSettingsDao()

    // State of the workout
    private val _workoutState = MutableLiveData(WorkoutState.INITIAL)
    val workoutState: LiveData<WorkoutState> = _workoutState

    private val _count = MutableLiveData(0)
    val count: LiveData<Int> = _count

    private val _timeWhenStopped = MutableLiveData(0L)
    val timeWhenStopped: LiveData<Long> = _timeWhenStopped

    private val _goalReached = MutableLiveData(false)
    val goalReached: LiveData<Boolean> = _goalReached

    // Settings from DB
    private val dbSettings: LiveData<BowSettings?> = settingsDao.getSettings()

    // In-memory settings for UI
    private val _uiSettings = MediatorLiveData<BowSettings>()
    val uiSettings: LiveData<BowSettings> = _uiSettings

    // Today's total record from DB
    val todayRecord: LiveData<Int>

    val monthlyRecordSummaries: LiveData<List<DailyRecordSummary>>
    private val _currentDisplayCalendar = MutableLiveData(Calendar.getInstance())
    val currentDisplayCalendar: LiveData<Calendar> = _currentDisplayCalendar

    private val _selectedDailyRecords = MutableLiveData<List<DailyRecord>?>()
    val selectedDailyRecords: LiveData<List<DailyRecord>?> = _selectedDailyRecords

    init {
        _workoutState.value = WorkoutState.INITIAL
        _uiSettings.addSource(dbSettings) {
            if (it != null && _uiSettings.value == null) {
                _uiSettings.value = it
            } else if (it == null && _uiSettings.value == null) {
                 _uiSettings.value = BowSettings()
            }
        }

        val dailyRecords = recordDao.getRecordsForDay(getStartOfDay(Date()), getEndOfDay(Date()))
        todayRecord = MediatorLiveData<Int>().apply {
            addSource(dailyRecords) {
                value = it.sumOf { record -> record.count }
            }
        }

        val startOfMonth = getStartOfMonth(_currentDisplayCalendar.value!!)
        val endOfMonth = getEndOfMonth(_currentDisplayCalendar.value!!)
        monthlyRecordSummaries = recordDao.getDailyRecordSummaries(startOfMonth, endOfMonth)
    }

    // --- Control Functions ---
    fun onStartClicked() {
        _workoutState.value = WorkoutState.PREPARING
    }

    fun onPauseClicked() {
        _workoutState.value = WorkoutState.PAUSED
    }

    fun onResumeClicked() {
        _workoutState.value = WorkoutState.RUNNING
    }
    
    fun onResetClicked() {
        _count.value = 0
        _timeWhenStopped.value = 0L
        _workoutState.value = WorkoutState.INITIAL
        _goalReached.value = false
    }

    fun workoutStarted() {
        _workoutState.value = WorkoutState.RUNNING
    }

    fun endWorkout(duration: Long, saveRecord: Boolean = true) {
        if (saveRecord) {
            val newCount = _count.value ?: 0
            if (newCount > 0) {
                viewModelScope.launch {
                    recordDao.insert(DailyRecord(date = Date(), count = newCount, duration = duration))
                }
            }
        }
        _count.value = 0
        _timeWhenStopped.value = 0L
        _workoutState.value = WorkoutState.INITIAL
        _goalReached.value = false
    }

    fun incrementCount(): Boolean {
        val currentCount = (_count.value ?: 0) + 1
        _count.value = currentCount

        val planCount = _uiSettings.value?.planCount ?: 111
        val isGoalReached = currentCount >= planCount
        if (isGoalReached) {
            _goalReached.value = true
        }
        return isGoalReached
    }

    fun onGoalReachedHandled() {
        _goalReached.value = false
    }

    fun setTimeWhenStopped(time: Long) {
        _timeWhenStopped.value = time
    }

    fun updateIntervalInMemory(delta: Double) {
        val current = _uiSettings.value ?: BowSettings()
        val newInterval = current.interval + delta
        if (newInterval > 0) {
             // Format to one decimal place
            val formattedInterval = String.format("%.1f", newInterval).toDouble()
            _uiSettings.value = current.copy(interval = formattedInterval)
        }
    }

    fun saveSettings(planCount: Int, keepScreenOn: Boolean) {
        val current = _uiSettings.value ?: BowSettings()
        _uiSettings.value = current.copy(planCount = planCount, keepScreenOn = keepScreenOn)
        saveSettingsToDatabase()
    }

    fun saveSettingsToDatabase() {
        _uiSettings.value?.let { settings ->
            viewModelScope.launch {
                settingsDao.saveSettings(settings)
            }
        }
    }
    
    fun changeMonth(amount: Int) {
        // This function is for the monthly record screen, no change needed here.
    }

    fun onDateClicked(summary: DailyRecordSummary) {
        viewModelScope.launch {
            val ids = summary.ids.split(",").map { it.toLong() }
            _selectedDailyRecords.postValue(recordDao.getRecordsByIds(ids))
        }
    }

    fun onDetailDialogDismissed() {
        _selectedDailyRecords.value = null
    }

    // --- Helper Functions (omitted for brevity) ---
    private fun getStartOfDay(date: Date): Date { val c = Calendar.getInstance(); c.time = date; c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0); return c.time }
    private fun getEndOfDay(date: Date): Date { val c = Calendar.getInstance(); c.time = date; c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999); return c.time }
    private fun getStartOfMonth(calendar: Calendar): Date { val newCal = calendar.clone() as Calendar; newCal.set(Calendar.DAY_OF_MONTH, 1); return getStartOfDay(newCal.time) }
    private fun getEndOfMonth(calendar: Calendar): Date { val newCal = calendar.clone() as Calendar; newCal.set(Calendar.DAY_OF_MONTH, newCal.getActualMaximum(Calendar.DAY_OF_MONTH)); return getEndOfDay(newCal.time) }
}
