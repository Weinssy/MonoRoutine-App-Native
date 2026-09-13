package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitCompletion
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.RingkasanHeroCard
import com.example.ui.components.StatusFilterRow
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoDark
import com.example.ui.theme.MonoGray
import com.example.ui.theme.MonoLightGray
import com.example.ui.theme.MonoSurface

@Composable
fun HabitsScreen(
    habits: List<Habit>,
    allHabits: List<Habit>,
    completions: List<HabitCompletion>,
    todayStr: String,
    selectedCategory: String,
    selectedStatus: String,
    onSelectCategory: (String) -> Unit,
    onSelectStatus: (String) -> Unit,
    onToggleCompletion: (Habit) -> Unit,
    onToggleAlarm: (Habit) -> Unit,
    onTriggerAlarm: (Habit) -> Unit,
    onAddHabit: (String, String, String, Boolean, String, String) -> Unit,
    onUpdateHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    val todayCompletedIds = remember(completions, todayStr) {
        completions.filter { it.date == todayStr }.map { it.habitId }.toSet()
    }

    val completedCount = todayCompletedIds.size
    val totalCount = allHabits.size
    val bestStreak = allHabits.maxOfOrNull { it.bestStreak } ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            RingkasanHeroCard(
                completedCount = completedCount,
                totalCount = totalCount,
                bestStreak = bestStreak,
                activeRoutines = totalCount
            )
        }

        // Section Title & Add Button below title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(BrandRed)
                    )
                    Text(
                        text = "DAFTAR KEBIASAAN",
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Kelola rutinitas harian untuk menjaga konsistensi",
                    color = MonoGray,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MonoBlack)
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("add_habit_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Kebiasaan",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TAMBAH DAFTAR KEBIASAAN",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Filters
        item {
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory
            )
        }

        item {
            StatusFilterRow(
                selectedStatus = selectedStatus,
                onSelectStatus = onSelectStatus
            )
        }

        // Habit Cards
        if (habits.isEmpty()) {
            item {
                Surface(
                    color = MonoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "TIDAK ADA RUTINITAS",
                            color = MonoDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tidak ada kebiasaan yang cocok dengan filter aktif saat ini.",
                            color = MonoGray,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                val isCompleted = todayCompletedIds.contains(habit.id)
                HabitCardItem(
                    habit = habit,
                    isCompleted = isCompleted,
                    onToggleCompletion = { onToggleCompletion(habit) },
                    onToggleAlarm = { onToggleAlarm(habit) },
                    onTriggerAlarm = { onTriggerAlarm(habit) },
                    onEdit = { habitToEdit = habit },
                    onDelete = { habitToDelete = habit }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Habit Dialog
    if (showAddDialog) {
        HabitFormDialog(
            title = "Tambah Rutinitas Baru",
            initialHabit = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, category, time, alarm, preset, notes ->
                onAddHabit(name, category, time, alarm, preset, notes)
                showAddDialog = false
            }
        )
    }

    // Edit Habit Dialog
    habitToEdit?.let { habit ->
        HabitFormDialog(
            title = "Edit Rutinitas",
            initialHabit = habit,
            onDismiss = { habitToEdit = null },
            onSave = { name, category, time, alarm, preset, notes ->
                onUpdateHabit(
                    habit.copy(
                        name = name,
                        category = category,
                        targetTime = time,
                        alarmEnabled = alarm,
                        soundPreset = preset,
                        notes = notes
                    )
                )
                habitToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = {
                Text("Hapus Rutinitas?", fontWeight = FontWeight.Black)
            },
            text = {
                Text("Apakah Anda yakin ingin menghapus '${habit.name}'? Seluruh riwayat streak kebiasaan ini akan ikut terhapus.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteHabit(habit)
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Hapus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { habitToDelete = null }) {
                    Text("Batal", color = MonoBlack)
                }
            }
        )
    }
}

@Composable
fun HabitCardItem(
    habit: Habit,
    isCompleted: Boolean,
    onToggleCompletion: () -> Unit,
    onToggleAlarm: () -> Unit,
    onTriggerAlarm: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCompleted) MonoLightGray else MonoBorder
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Top Row: Category + Time + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .border(1.dp, MonoBorder, RoundedCornerShape(2.dp))
                            .background(Color(0xFFF9F9F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = habit.category.uppercase(),
                            color = MonoDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Target Time Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = "Target Waktu",
                            tint = MonoGray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = habit.targetTime,
                            color = MonoGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sound Preset Badge
                    Text(
                        text = "• ${habit.soundPreset}",
                        color = MonoGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right action icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Quick Alarm test trigger
                    IconButton(
                        onClick = onTriggerAlarm,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Uji Alarm",
                            tint = MonoDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Toggle Alarm Enabled
                    IconButton(
                        onClick = onToggleAlarm,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (habit.alarmEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                            contentDescription = "Alarm Rutinitas",
                            tint = if (habit.alarmEnabled) BrandRed else Color(0xFFCCCCCC),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Context Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu Rutinitas",
                                tint = MonoDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (menuExpanded) {
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Rutinitas") },
                                    onClick = {
                                        menuExpanded = false
                                        onEdit()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Hapus", color = BrandRed) },
                                    onClick = {
                                        menuExpanded = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Row: Checkbox + Name + Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Custom square checkbox
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(
                                1.5.dp,
                                if (isCompleted) MonoBlack else Color(0xFFAAAAAA),
                                RoundedCornerShape(3.dp)
                            )
                            .background(if (isCompleted) MonoBlack else Color.White)
                            .clickable { onToggleCompletion() }
                            .testTag("habit_checkbox_${habit.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selesai",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = habit.name,
                            color = if (isCompleted) MonoGray else MonoBlack,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (habit.notes.isNotBlank()) {
                            Text(
                                text = habit.notes,
                                color = MonoGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Streak Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (habit.currentStreak > 0) Color(0xFFFFF1F1) else Color(0xFFF5F5F5))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = if (habit.currentStreak > 0) BrandRed else MonoGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${habit.currentStreak} hr",
                        color = if (habit.currentStreak > 0) BrandRed else MonoGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun HabitFormDialog(
    title: String,
    initialHabit: Habit?,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, time: String, alarm: Boolean, preset: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialHabit?.name ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "Kesehatan") }
    var time by remember { mutableStateOf(initialHabit?.targetTime ?: "07:00") }
    var alarm by remember { mutableStateOf(initialHabit?.alarmEnabled ?: true) }
    var preset by remember { mutableStateOf(initialHabit?.soundPreset ?: "Lembut") }
    var notes by remember { mutableStateOf(initialHabit?.notes ?: "") }

    val categories = listOf("Kesehatan", "Pekerjaan", "Pribadi", "Belajar", "Keuangan", "Lainnya")
    val presets = listOf("Lembut", "Bel Klasik", "Digital Beep", "Sirene")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Black, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Rutinitas", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector pills
                Column {
                    Text("Kategori", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MonoGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.take(3).forEach { cat ->
                            val isSel = cat == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSel) MonoBlack else MonoBorder, RoundedCornerShape(3.dp))
                                    .background(if (isSel) MonoBlack else Color.White)
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MonoBlack
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.drop(3).forEach { cat ->
                            val isSel = cat == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSel) MonoBlack else MonoBorder, RoundedCornerShape(3.dp))
                                    .background(if (isSel) MonoBlack else Color.White)
                                    .clickable { category = cat }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MonoBlack
                                )
                            }
                        }
                    }
                }

                // Time picker text input
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Waktu Target (HH:mm)", fontSize = 12.sp) },
                    placeholder = { Text("07:00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Sound preset selector
                Column {
                    Text("Pilihan Suara Alarm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MonoGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        presets.forEach { p ->
                            val isSel = p == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, if (isSel) MonoBlack else MonoBorder, RoundedCornerShape(3.dp))
                                    .background(if (isSel) MonoBlack else Color.White)
                                    .clickable { preset = p }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    p,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else MonoBlack
                                )
                            }
                        }
                    }
                }

                // Alarm Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Aktifkan Alarm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = alarm,
                        onCheckedChange = { alarm = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MonoBlack
                        )
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan (Opsional)", fontSize = 12.sp) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, category, time, alarm, preset, notes)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
            ) {
                Text("Simpan", color = Color.White, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = MonoGray)
            }
        }
    )
}
