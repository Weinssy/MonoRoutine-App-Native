package com.example.data

import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoutineRepository(
    private val habitDao: HabitDao,
    private val taskDao: TaskDao
) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()
    val allCompletions: Flow<List<HabitCompletion>> = habitDao.getAllCompletions()

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> {
        return habitDao.getCompletionsForDate(date)
    }

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteCompletionsByHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habit: Habit, date: String, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            // Uncheck
            habitDao.deleteCompletion(habit.id, date)
            val newStreak = (habit.currentStreak - 1).coerceAtLeast(0)
            val updated = habit.copy(
                currentStreak = newStreak,
                lastCompletedDate = if (habit.lastCompletedDate == date) "" else habit.lastCompletedDate
            )
            habitDao.updateHabit(updated)
        } else {
            // Mark complete
            habitDao.insertCompletion(HabitCompletion(habitId = habit.id, date = date))

            // Check if consecutive
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(date) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)

            val newCurrentStreak = if (habit.lastCompletedDate == yesterdayStr) {
                habit.currentStreak + 1
            } else if (habit.lastCompletedDate == date) {
                habit.currentStreak
            } else {
                1
            }

            val newBestStreak = maxOf(newCurrentStreak, habit.bestStreak)
            val updated = habit.copy(
                currentStreak = newCurrentStreak,
                bestStreak = newBestStreak,
                lastCompletedDate = date
            )
            habitDao.updateHabit(updated)
        }
    }

    // Task operations
    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskDao.setTaskCompleted(id, completed)

    // Data reset
    suspend fun resetAllData() {
        habitDao.clearAllHabits()
        habitDao.clearAllCompletions()
        taskDao.clearAllTasks()
        AppDatabase.populateInitialData(habitDao, taskDao)
    }

    // Export & Import
    fun exportToJson(habits: List<Habit>, completions: List<HabitCompletion>, tasks: List<TaskItem>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "MonoRoutine")
        root.put("exportedAt", System.currentTimeMillis())

        val habitsArray = JSONArray()
        for (h in habits) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("name", h.name)
            obj.put("category", h.category)
            obj.put("targetTime", h.targetTime)
            obj.put("daysOfWeek", h.daysOfWeek)
            obj.put("currentStreak", h.currentStreak)
            obj.put("bestStreak", h.bestStreak)
            obj.put("lastCompletedDate", h.lastCompletedDate)
            obj.put("alarmEnabled", h.alarmEnabled)
            obj.put("soundPreset", h.soundPreset)
            obj.put("notes", h.notes)
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        val completionsArray = JSONArray()
        for (c in completions) {
            val obj = JSONObject()
            obj.put("habitId", c.habitId)
            obj.put("date", c.date)
            obj.put("completedAt", c.completedAt)
            completionsArray.put(obj)
        }
        root.put("completions", completionsArray)

        val tasksArray = JSONArray()
        for (t in tasks) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("deadline", t.deadline)
            obj.put("priority", t.priority)
            obj.put("notes", t.notes)
            obj.put("isCompleted", t.isCompleted)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("habits")) {
                val habitsArray = root.getJSONArray("habits")
                for (i in 0 until habitsArray.length()) {
                    val obj = habitsArray.getJSONObject(i)
                    val habit = Habit(
                        name = obj.optString("name", "Rutinitas"),
                        category = obj.optString("category", "Lainnya"),
                        targetTime = obj.optString("targetTime", "08:00"),
                        daysOfWeek = obj.optString("daysOfWeek", "1,2,3,4,5,6,7"),
                        currentStreak = obj.optInt("currentStreak", 0),
                        bestStreak = obj.optInt("bestStreak", 0),
                        lastCompletedDate = obj.optString("lastCompletedDate", ""),
                        alarmEnabled = obj.optBoolean("alarmEnabled", true),
                        soundPreset = obj.optString("soundPreset", "Lembut"),
                        notes = obj.optString("notes", "")
                    )
                    habitDao.insertHabit(habit)
                }
            }

            if (root.has("tasks")) {
                val tasksArray = root.getJSONArray("tasks")
                for (i in 0 until tasksArray.length()) {
                    val obj = tasksArray.getJSONObject(i)
                    val task = TaskItem(
                        title = obj.optString("title", "Tugas"),
                        deadline = obj.optLong("deadline", System.currentTimeMillis() + 86400000L),
                        priority = obj.optString("priority", "Normal"),
                        notes = obj.optString("notes", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                    taskDao.insertTask(task)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
