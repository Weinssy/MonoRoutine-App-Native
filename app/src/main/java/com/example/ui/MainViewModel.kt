package com.example.ui

import android.app.Application
import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Habit
import com.example.data.HabitCompletion
import com.example.data.RoutineRepository
import com.example.data.TaskItem
import com.example.sound.ToneSynthesizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppTab {
    HABITS, ALARM, CALENDAR, TASKS, BACKUP
}

data class UiNotificationEvent(
    val title: String,
    val message: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RoutineRepository
    private var wakeLock: PowerManager.WakeLock? = null

    val todayStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _currentTab = MutableStateFlow(AppTab.HABITS)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("all") // all, today, pending, completed
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    private val _selectedTaskFilter = MutableStateFlow("all") // all, pending, approaching (<24h), h2 (<48h), urgent, completed
    val selectedTaskFilter: StateFlow<String> = _selectedTaskFilter.asStateFlow()

    private val _selectedSoundPreset = MutableStateFlow("Lembut")
    val selectedSoundPreset: StateFlow<String> = _selectedSoundPreset.asStateFlow()

    private val _alarmVolume = MutableStateFlow(0.8f)
    val alarmVolume: StateFlow<Float> = _alarmVolume.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _activeAlarmHabit = MutableStateFlow<Habit?>(null)
    val activeAlarmHabit: StateFlow<Habit?> = _activeAlarmHabit.asStateFlow()

    private val _calendarMonthOffset = MutableStateFlow(0)
    val calendarMonthOffset: StateFlow<Int> = _calendarMonthOffset.asStateFlow()

    private val _isWakeLockEnabled = MutableStateFlow(false)
    val isWakeLockEnabled: StateFlow<Boolean> = _isWakeLockEnabled.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = RoutineRepository(database.habitDao(), database.taskDao())
    }

    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletions: StateFlow<List<HabitCompletion>> = repository.allCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Habits Flow
    val filteredHabits: StateFlow<List<Habit>> = combine(
        allHabits,
        allCompletions,
        _selectedCategory,
        _selectedStatusFilter
    ) { habits, completions, category, statusFilter ->
        val todayCompletedIds = completions.filter { it.date == todayStr }.map { it.habitId }.toSet()

        habits.filter { habit ->
            val matchCategory = category == "Semua" || habit.category.equals(category, ignoreCase = true)
            val isCompletedToday = todayCompletedIds.contains(habit.id)

            val matchStatus = when (statusFilter) {
                "today" -> true
                "pending" -> !isCompletedToday
                "completed" -> isCompletedToday
                else -> true
            }

            matchCategory && matchStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Tasks Flow
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        allTasks,
        _selectedTaskFilter
    ) { tasks, filter ->
        val now = System.currentTimeMillis()
        val h24 = now + 24 * 3600 * 1000L
        val h48 = now + 48 * 3600 * 1000L

        tasks.filter { task ->
            when (filter) {
                "pending" -> !task.isCompleted
                "approaching" -> !task.isCompleted && task.deadline in (now..h24)
                "h2" -> !task.isCompleted && task.deadline in (h24..h48)
                "urgent" -> !task.isCompleted && task.priority.equals("Mendesak", ignoreCase = true)
                "completed" -> task.isCompleted
                else -> true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setStatusFilter(status: String) {
        _selectedStatusFilter.value = status
    }

    fun setTaskFilter(filter: String) {
        _selectedTaskFilter.value = filter
    }

    fun setSoundPreset(preset: String) {
        _selectedSoundPreset.value = preset
    }

    fun setAlarmVolume(volume: Float) {
        _alarmVolume.value = volume.coerceIn(0f, 1f)
    }

    fun changeMonthOffset(delta: Int) {
        _calendarMonthOffset.value += delta
    }

    fun toggleHabitCompletion(habit: Habit) {
        viewModelScope.launch {
            val isCurrentlyCompleted = allCompletions.value.any { it.habitId == habit.id && it.date == todayStr }
            repository.toggleHabitCompletion(habit, todayStr, isCurrentlyCompleted)
        }
    }

    fun addHabit(
        name: String,
        category: String,
        targetTime: String,
        alarmEnabled: Boolean,
        soundPreset: String,
        notes: String
    ) {
        viewModelScope.launch {
            val newHabit = Habit(
                name = name.trim(),
                category = category,
                targetTime = targetTime,
                alarmEnabled = alarmEnabled,
                soundPreset = soundPreset,
                notes = notes.trim()
            )
            repository.insertHabit(newHabit)
            showToast("Rutinitas ditambahkan")
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            showToast("Rutinitas diperbarui")
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            showToast("Rutinitas dihapus")
        }
    }

    fun toggleHabitAlarm(habit: Habit) {
        viewModelScope.launch {
            val updated = habit.copy(alarmEnabled = !habit.alarmEnabled)
            repository.updateHabit(updated)
        }
    }

    fun addTask(title: String, deadlineMs: Long, priority: String, notes: String) {
        viewModelScope.launch {
            val newTask = TaskItem(
                title = title.trim(),
                deadline = deadlineMs,
                priority = priority,
                notes = notes.trim(),
                isCompleted = false
            )
            repository.insertTask(newTask)
            showToast("Tugas baru ditambahkan")
        }
    }

    fun toggleTaskCompletion(task: TaskItem) {
        viewModelScope.launch {
            repository.setTaskCompleted(task.id, !task.isCompleted)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Tugas dihapus")
        }
    }

    // Audio Synthesizer controls
    fun testSound(preset: String = _selectedSoundPreset.value) {
        _isAudioPlaying.value = true
        ToneSynthesizer.playPreset(
            scope = viewModelScope,
            preset = preset,
            volume = _alarmVolume.value,
            loop = false,
            onStopped = { _isAudioPlaying.value = false }
        )
    }

    fun triggerAlarm(habit: Habit) {
        _activeAlarmHabit.value = habit
        _isAudioPlaying.value = true
        ToneSynthesizer.playPreset(
            scope = viewModelScope,
            preset = habit.soundPreset,
            volume = _alarmVolume.value,
            loop = true,
            onStopped = { _isAudioPlaying.value = false }
        )
    }

    fun stopAudio() {
        ToneSynthesizer.stop()
        _isAudioPlaying.value = false
    }

    fun dismissEmergencyAlarm() {
        stopAudio()
        _activeAlarmHabit.value = null
    }

    fun completeAlarmHabit() {
        val habit = _activeAlarmHabit.value
        if (habit != null) {
            toggleHabitCompletion(habit)
        }
        dismissEmergencyAlarm()
    }

    fun toggleWakeLock() {
        val context = getApplication<Application>().applicationContext
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return

        if (_isWakeLockEnabled.value) {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
            wakeLock = null
            _isWakeLockEnabled.value = false
            showToast("Layar aktif: Nonaktif")
        } else {
            val wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, "MonoRoutine:WakeLock")
            wl.acquire(60 * 60 * 1000L) // 60 mins
            wakeLock = wl
            _isWakeLockEnabled.value = true
            showToast("Layar aktif: Aktif")
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            showToast("Data berhasil direset ke pengaturan awal")
        }
    }

    fun exportDataJson(): String {
        return repository.exportToJson(allHabits.value, allCompletions.value, allTasks.value)
    }

    fun importDataJson(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importFromJson(json)
            if (success) {
                showToast("Data berhasil dipulihkan")
            } else {
                showToast("Gagal membaca format JSON")
            }
            onComplete(success)
        }
    }

    private fun showToast(message: String) {
        _bannerMessage.value = message
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }
}
