package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // "Kesehatan", "Pekerjaan", "Pribadi", "Belajar", "Keuangan", "Lainnya"
    val targetTime: String, // "HH:mm", e.g., "07:00"
    val daysOfWeek: String = "1,2,3,4,5,6,7", // comma-separated 1=Monday to 7=Sunday
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = "", // "YYYY-MM-DD"
    val alarmEnabled: Boolean = true,
    val soundPreset: String = "Lembut", // "Lembut", "Bel Klasik", "Digital Beep", "Sirene"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis()
)
