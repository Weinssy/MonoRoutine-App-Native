package com.example

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Habit
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.EmergencyAlarmBanner
import com.example.ui.components.MonoTopAppBar
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.BackupScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoGray
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    companion object {
        const val CHANNEL_ID = "monoroutine_alarm_channel"
        const val CHANNEL_NAME = "MonoRoutine Pengingat & Alarm"
    }

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()
        requestNotificationPermission()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel, onSendNotification = { title, msg, isAlarm ->
                    sendNativeNotification(title, msg, isAlarm)
                    if (isAlarm) vibrateDevice(800)
                })
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat alarm rutinitas harian dan tenggat waktu tugas"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    private fun sendNativeNotification(title: String, message: String, isAlarm: Boolean) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(if (isAlarm) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(if (isAlarm) NotificationCompat.CATEGORY_ALARM else NotificationCompat.CATEGORY_REMINDER)
            .build()

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notification)
    }

    private fun vibrateDevice(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onSendNotification: (title: String, msg: String, isAlarm: Boolean) -> Unit
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val habits by viewModel.filteredHabits.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val completions by viewModel.allCompletions.collectAsStateWithLifecycle()
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()

    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedTaskFilter by viewModel.selectedTaskFilter.collectAsStateWithLifecycle()

    val selectedSoundPreset by viewModel.selectedSoundPreset.collectAsStateWithLifecycle()
    val alarmVolume by viewModel.alarmVolume.collectAsStateWithLifecycle()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsStateWithLifecycle()
    val activeAlarmHabit by viewModel.activeAlarmHabit.collectAsStateWithLifecycle()
    val calendarMonthOffset by viewModel.calendarMonthOffset.collectAsStateWithLifecycle()
    val isWakeLockEnabled by viewModel.isWakeLockEnabled.collectAsStateWithLifecycle()
    val bannerMessage by viewModel.bannerMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show banner messages
    LaunchedEffect(bannerMessage) {
        bannerMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    // Periodic Alarm Checker (Runs every 15 seconds)
    LaunchedEffect(allHabits) {
        var lastTriggeredMinute = ""
        while (isActive) {
            val nowStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            if (nowStr != lastTriggeredMinute) {
                val matchingHabits = allHabits.filter { it.alarmEnabled && it.targetTime == nowStr }
                if (matchingHabits.isNotEmpty()) {
                    val habitToRing = matchingHabits.first()
                    viewModel.triggerAlarm(habitToRing)
                    onSendNotification(
                        "Waktunya Rutinitas!",
                        "${habitToRing.name} (${habitToRing.targetTime} WIB)",
                        true
                    )
                    lastTriggeredMinute = nowStr
                }
            }
            delay(15000)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                MonoTopAppBar(
                    isWakeLockActive = isWakeLockEnabled,
                    onToggleWakeLock = { viewModel.toggleWakeLock() },
                    isAlarmActive = activeAlarmHabit != null || isAudioPlaying,
                    onEmergencyStopAlarm = { viewModel.dismissEmergencyAlarm() }
                )
                EmergencyAlarmBanner(
                    activeHabit = activeAlarmHabit,
                    onComplete = { viewModel.completeAlarmHabit() },
                    onStop = { viewModel.dismissEmergencyAlarm() }
                )
            }
        },
        bottomBar = {
            MonoBottomNav(
                currentTab = currentTab,
                onSelectTab = { viewModel.setTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HABITS -> HabitsScreen(
                    habits = habits,
                    allHabits = allHabits,
                    completions = completions,
                    todayStr = viewModel.todayStr,
                    selectedCategory = selectedCategory,
                    selectedStatus = selectedStatus,
                    onSelectCategory = { viewModel.setCategoryFilter(it) },
                    onSelectStatus = { viewModel.setStatusFilter(it) },
                    onToggleCompletion = { viewModel.toggleHabitCompletion(it) },
                    onToggleAlarm = { viewModel.toggleHabitAlarm(it) },
                    onTriggerAlarm = {
                        viewModel.triggerAlarm(it)
                        onSendNotification("Uji Alarm", "${it.name} - ${it.targetTime}", true)
                    },
                    onAddHabit = { name, cat, time, alarm, preset, notes ->
                        viewModel.addHabit(name, cat, time, alarm, preset, notes)
                    },
                    onUpdateHabit = { viewModel.updateHabit(it) },
                    onDeleteHabit = { viewModel.deleteHabit(it) }
                )

                AppTab.ALARM -> AlarmScreen(
                    habits = allHabits,
                    selectedSoundPreset = selectedSoundPreset,
                    alarmVolume = alarmVolume,
                    isAudioPlaying = isAudioPlaying,
                    isWakeLockEnabled = isWakeLockEnabled,
                    onSelectPreset = { viewModel.setSoundPreset(it) },
                    onVolumeChange = { viewModel.setAlarmVolume(it) },
                    onTestSound = { viewModel.testSound(it) },
                    onStopAudio = { viewModel.stopAudio() },
                    onTriggerAlarm = {
                        viewModel.triggerAlarm(it)
                        onSendNotification("Alarm Berdering", "${it.name} - ${it.targetTime}", true)
                    },
                    onToggleHabitAlarm = { viewModel.toggleHabitAlarm(it) },
                    onToggleWakeLock = { viewModel.toggleWakeLock() }
                )

                AppTab.CALENDAR -> CalendarScreen(
                    habits = allHabits,
                    completions = completions,
                    monthOffset = calendarMonthOffset,
                    onChangeMonth = { viewModel.changeMonthOffset(it) }
                )

                AppTab.TASKS -> TasksScreen(
                    tasks = tasks,
                    allTasks = allTasks,
                    selectedFilter = selectedTaskFilter,
                    onSelectFilter = { viewModel.setTaskFilter(it) },
                    onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onAddTask = { title, deadline, priority, notes ->
                        viewModel.addTask(title, deadline, priority, notes)
                    }
                )

                AppTab.BACKUP -> BackupScreen(
                    habits = allHabits,
                    tasks = allTasks,
                    onExportJson = { viewModel.exportDataJson() },
                    onImportJson = { json, callback -> viewModel.importDataJson(json, callback) },
                    onResetData = { viewModel.resetData() }
                )
            }
        }
    }
}

@Composable
fun MonoBottomNav(
    currentTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Triple(AppTab.HABITS, "Rutinitas", Icons.Outlined.CheckCircle to Icons.Default.CheckCircle),
        Triple(AppTab.ALARM, "Alarm", Icons.Outlined.Alarm to Icons.Default.Alarm),
        Triple(AppTab.CALENDAR, "Kalender", Icons.Outlined.CalendarMonth to Icons.Default.CalendarMonth),
        Triple(AppTab.TASKS, "Tugas", Icons.Outlined.Checklist to Icons.Default.Checklist),
        Triple(AppTab.BACKUP, "Backup", Icons.Outlined.Settings to Icons.Default.Settings)
    )

    Surface(
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (tab, label, iconPair) ->
                val isSelected = currentTab == tab
                val icon = if (isSelected) iconPair.second else iconPair.first

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clickable { onSelectTab(tab) }
                        .testTag("nav_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Top active indicator bar
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 2.5.dp)
                                .background(if (isSelected) MonoBlack else Color.Transparent)
                        )

                        Box(modifier = Modifier.height(4.dp))

                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) MonoBlack else MonoGray,
                            modifier = Modifier.size(20.dp)
                        )

                        Box(modifier = Modifier.height(2.dp))

                        Text(
                            text = label.uppercase(Locale.getDefault()),
                            color = if (isSelected) MonoBlack else MonoGray,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
