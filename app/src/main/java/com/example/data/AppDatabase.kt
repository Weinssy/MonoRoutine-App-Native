package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [Habit::class, HabitCompletion::class, TaskItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monoroutine_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.habitDao(), database.taskDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(habitDao: HabitDao, taskDao: TaskDao) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val sampleHabits = listOf(
                Habit(
                    name = "Minum Air Putih 500ml",
                    category = "Kesehatan",
                    targetTime = "06:30",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    currentStreak = 4,
                    bestStreak = 12,
                    lastCompletedDate = todayStr,
                    alarmEnabled = true,
                    soundPreset = "Lembut",
                    notes = "Mulai metabolisme pagi dengan hidrasi cukup"
                ),
                Habit(
                    name = "Membaca Buku / Jurnal 20 Mnt",
                    category = "Belajar",
                    targetTime = "07:15",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    currentStreak = 7,
                    bestStreak = 14,
                    lastCompletedDate = todayStr,
                    alarmEnabled = false,
                    soundPreset = "Bel Klasik",
                    notes = "Membaca minimal 1 bab atau materi baru"
                ),
                Habit(
                    name = "Review Prioritas Kerja Harian",
                    category = "Pekerjaan",
                    targetTime = "08:30",
                    daysOfWeek = "1,2,3,4,5",
                    currentStreak = 5,
                    bestStreak = 9,
                    lastCompletedDate = "",
                    alarmEnabled = true,
                    soundPreset = "Digital Beep",
                    notes = "Tentukan 3 target pekerjaan paling penting"
                ),
                Habit(
                    name = "Pencatatan Pengeluaran Harian",
                    category = "Keuangan",
                    targetTime = "20:00",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    currentStreak = 3,
                    bestStreak = 8,
                    lastCompletedDate = "",
                    alarmEnabled = true,
                    soundPreset = "Lembut",
                    notes = "Catat semua pemasukan dan pengeluaran hari ini"
                ),
                Habit(
                    name = "Stretching & Relaksasi Malam",
                    category = "Kesehatan",
                    targetTime = "21:45",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    currentStreak = 6,
                    bestStreak = 10,
                    lastCompletedDate = "",
                    alarmEnabled = true,
                    soundPreset = "Lembut",
                    notes = "Peregangan otot ringan sebelum tidur"
                )
            )

            for (habit in sampleHabits) {
                val habitId = habitDao.insertHabit(habit)
                if (habit.lastCompletedDate == todayStr) {
                    habitDao.insertCompletion(
                        HabitCompletion(habitId = habitId, date = todayStr)
                    )
                }
            }

            val now = Calendar.getInstance()

            val cal1 = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 18) // < 24 jam (H-1)
            }
            val cal2 = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, 38) // < 48 jam (H-2)
            }
            val cal3 = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 4) // Aman
            }

            val sampleTasks = listOf(
                TaskItem(
                    title = "Submit Laporan Evaluasi Mingguan",
                    deadline = cal1.timeInMillis,
                    priority = "Mendesak",
                    notes = "Pastikan data lampiran CSV sudah lengkap",
                    isCompleted = false
                ),
                TaskItem(
                    title = "Presentasi Rancangan Desain UI",
                    deadline = cal2.timeInMillis,
                    priority = "Normal",
                    notes = "Diskusi bersama tim internal jam 10:00",
                    isCompleted = false
                ),
                TaskItem(
                    title = "Review Kode & Deploy Sprint 4",
                    deadline = cal3.timeInMillis,
                    priority = "Normal",
                    notes = "Cek coverage pengujian dan performa",
                    isCompleted = false
                )
            )

            for (task in sampleTasks) {
                taskDao.insertTask(task)
            }
        }
    }
}
