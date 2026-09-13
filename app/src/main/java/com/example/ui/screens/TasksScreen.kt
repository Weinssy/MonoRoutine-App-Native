package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoDark
import com.example.ui.theme.MonoGray
import com.example.ui.theme.MonoSurface
import com.example.ui.theme.UrgentAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    tasks: List<TaskItem>,
    allTasks: List<TaskItem>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    onToggleCompletion: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onAddTask: (title: String, deadlineMs: Long, priority: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showForm by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val h24 = now + 24 * 3600 * 1000L

    val pendingCount = allTasks.count { !it.isCompleted }
    val urgentCount = allTasks.count { !it.isCompleted && it.deadline in (now..h24) }
    val completedCount = allTasks.count { it.isCompleted }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
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
                        text = "TASK & DEADLINE MANAGER",
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Tenggat waktu terurut otomatis dengan peringatan H-2 & H-1",
                    color = MonoGray,
                    fontSize = 11.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MonoBlack)
                        .clickable { showForm = !showForm }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("toggle_task_form_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Buka Form",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showForm) "TUTUP FORM TUGAS" else "TAMBAH TUGAS BARU",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatBox(
                    count = pendingCount,
                    label = "BELUM SELESAI",
                    countColor = MonoBlack,
                    modifier = Modifier.weight(1f)
                )
                TaskStatBox(
                    count = urgentCount,
                    label = "< 24 JAM (H-1)",
                    countColor = BrandRed,
                    modifier = Modifier.weight(1f)
                )
                TaskStatBox(
                    count = completedCount,
                    label = "TELAH SELESAI",
                    countColor = MonoBlack,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Inline Form
        item {
            AnimatedVisibility(visible = showForm) {
                TaskInlineForm(
                    onCancel = { showForm = false },
                    onSave = { title, deadline, priority, notes ->
                        onAddTask(title, deadline, priority, notes)
                        showForm = false
                    }
                )
            }
        }

        // Filter Pills
        item {
            TaskFilterRow(
                selectedFilter = selectedFilter,
                onSelectFilter = onSelectFilter
            )
        }

        // Task Items
        if (tasks.isEmpty()) {
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
                            text = "TIDAK ADA TUGAS",
                            color = MonoDark,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tidak ada tugas yang sesuai kriteria filter saat ini.",
                            color = MonoGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskCardItem(
                    task = task,
                    onToggle = { onToggleCompletion(task) },
                    onDelete = { onDeleteTask(task) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskStatBox(
    count: Int,
    label: String,
    countColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                color = countColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                color = MonoGray,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun TaskFilterRow(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    val filters = listOf(
        "all" to "Semua",
        "pending" to "Belum Selesai",
        "approaching" to "< 24 Jam (H-1)",
        "h2" to "< 48 Jam (H-2)",
        "urgent" to "Mendesak",
        "completed" to "Selesai"
    )
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (key, label) ->
            val isSel = selectedFilter == key
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(
                        1.dp,
                        if (isSel) MonoBlack else MonoBorder,
                        RoundedCornerShape(3.dp)
                    )
                    .background(if (isSel) MonoBlack else Color.White)
                    .clickable { onSelectFilter(key) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.uppercase(Locale.getDefault()),
                    color = if (isSel) Color.White else MonoGray,
                    fontWeight = if (isSel) FontWeight.Black else FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun TaskCardItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val diffMs = task.deadline - now
    val isOverdue = diffMs < 0 && !task.isCompleted
    val isH1 = diffMs in 0..(24 * 3600 * 1000L) && !task.isCompleted
    val isH2 = diffMs in (24 * 3600 * 1000L)..(48 * 3600 * 1000L) && !task.isCompleted

    val urgencyTag = when {
        task.isCompleted -> "SELESAI"
        isOverdue -> "LEWAT TENGGAT"
        isH1 -> "H-1 / < 24 JAM"
        isH2 -> "H-2 / < 48 JAM"
        else -> "AMAN"
    }

    val (tagBg, tagText) = when {
        task.isCompleted -> Pair(Color(0xFFF0F0F0), MonoGray)
        isOverdue -> Pair(Color(0xFFFFF1F1), BrandRed)
        isH1 -> Pair(Color(0xFFFFFBEB), UrgentAmber)
        isH2 -> Pair(Color(0xFFF4F4F5), MonoDark)
        else -> Pair(Color(0xFFF7F7F7), MonoGray)
    }

    val deadlineFormatted = remember(task.deadline) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        sdf.format(Date(task.deadline))
    }

    val remainingText = when {
        task.isCompleted -> "Tugas selesai"
        isOverdue -> {
            val hoursOver = (-diffMs / (3600 * 1000)).toInt()
            if (hoursOver >= 24) "Terlambat ${hoursOver / 24} hari" else "Terlambat $hoursOver jam"
        }
        else -> {
            val hoursLeft = (diffMs / (3600 * 1000)).toInt()
            val minsLeft = ((diffMs % (3600 * 1000)) / (60 * 1000)).toInt()
            if (hoursLeft >= 24) "Sisa ${hoursLeft / 24} hr ${hoursLeft % 24} jam" else "Sisa $hoursLeft jam $minsLeft mnt"
        }
    }

    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverdue) BrandRed else MonoBorder
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Urgency Tag + Priority + Remaining Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, if (isOverdue) BrandRed else MonoBorder, RoundedCornerShape(2.dp))
                            .background(tagBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = urgencyTag,
                            color = tagText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .border(1.dp, MonoBorder, RoundedCornerShape(2.dp))
                            .background(Color(0xFFFAFAFA))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priority.uppercase(),
                            color = if (task.priority == "Mendesak") BrandRed else MonoDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = remainingText,
                    color = if (isOverdue) BrandRed else if (isH1) UrgentAmber else MonoGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Middle Row: Checkbox + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                1.5.dp,
                                if (task.isCompleted) MonoBlack else Color(0xFFAAAAAA),
                                RoundedCornerShape(3.dp)
                            )
                            .background(if (task.isCompleted) MonoBlack else Color.White)
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selesai",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) MonoGray else MonoBlack,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        if (task.notes.isNotBlank()) {
                            Text(
                                text = task.notes,
                                color = MonoGray,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Tugas",
                        tint = MonoGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Deadline date string
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = MonoGray,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Batas: $deadlineFormatted",
                    color = MonoGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TaskInlineForm(
    onCancel: () -> Unit,
    onSave: (title: String, deadlineMs: Long, priority: String, notes: String) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Mendesak") }
    var notes by remember { mutableStateOf("") }

    val cal = remember {
        Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 24)
        }
    }

    var selectedCalendar by remember { mutableStateOf(cal) }

    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    var deadlineText by remember { mutableStateOf(sdf.format(selectedCalendar.time)) }

    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBlack),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "INPUT TUGAS & BATAS WAKTU",
                    color = MonoBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "TERURUT OTOMATIS",
                    color = MonoGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Tugas / Pekerjaan", fontSize = 11.sp) },
                placeholder = { Text("Contoh: Laporan Keuangan, Submit Tugas...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Deadline Date & Time Picker trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MonoBorder, RoundedCornerShape(4.dp))
                    .clickable {
                        val currentCal = selectedCalendar
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                                        }
                                        selectedCalendar = newCal
                                        deadlineText = sdf.format(newCal.time)
                                    },
                                    currentCal.get(Calendar.HOUR_OF_DAY),
                                    currentCal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            currentCal.get(Calendar.YEAR),
                            currentCal.get(Calendar.MONTH),
                            currentCal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("TENGGAT WAKTU", fontSize = 9.sp, color = MonoGray, fontWeight = FontWeight.Bold)
                    Text(deadlineText, fontSize = 12.sp, color = MonoBlack, fontWeight = FontWeight.Black)
                }
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MonoDark, modifier = Modifier.size(16.dp))
            }

            // Priority Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("TINGKAT PRIORITAS", fontSize = 9.sp, color = MonoGray, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Mendesak", "Normal", "Rendah").forEach { p ->
                        val isSel = priority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    if (isSel) 1.5.dp else 1.dp,
                                    if (isSel) MonoBlack else MonoBorder,
                                    RoundedCornerShape(3.dp)
                                )
                                .background(if (isSel) MonoBlack else Color.White)
                                .clickable { priority = p }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.uppercase(),
                                color = if (isSel) Color.White else MonoDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Catatan Tambahan (Opsional)", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(title, selectedCalendar.timeInMillis, priority, notes)
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("TAMBAHKAN TUGAS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                    shape = RoundedCornerShape(3.dp)
                ) {
                    Text("BATAL", color = MonoBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
