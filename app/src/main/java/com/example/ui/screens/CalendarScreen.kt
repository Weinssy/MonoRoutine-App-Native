package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.HabitCompletion
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoDark
import com.example.ui.theme.MonoGray
import com.example.ui.theme.MonoSurface
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class DayCompletionType {
    EMPTY,
    PARTIAL,
    ALL,
    TODAY_EMPTY
}

data class CalendarGridCell(
    val dayNumber: Int?,
    val completionType: DayCompletionType,
    val isToday: Boolean
)

@Composable
fun CalendarScreen(
    habits: List<Habit>,
    completions: List<HabitCompletion>,
    monthOffset: Int,
    onChangeMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthYearTitle = remember(cal) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        sdf.format(cal.time).uppercase()
    }

    val daysInMonth = remember(cal) { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val firstDayOfWeek = remember(cal) { cal.get(Calendar.DAY_OF_WEEK) } // 1=Sunday, 2=Monday, etc.
    val startOffset = firstDayOfWeek - 1

    val (todayYear, todayMonth, todayDay) = remember {
        val todayCalendar = Calendar.getInstance()
        Triple(
            todayCalendar.get(Calendar.YEAR),
            todayCalendar.get(Calendar.MONTH),
            todayCalendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)
    val isCurrentViewingMonth = (todayYear == currentYear && todayMonth == currentMonth)

    // Completion map: "YYYY-MM-DD" -> count
    val completionsByDate = remember(completions) {
        val map = mutableMapOf<String, Int>()
        for (c in completions) {
            map[c.date] = (map[c.date] ?: 0) + 1
        }
        map
    }

    val totalHabitsCount = habits.size

    // Precompute all grid cells to eliminate string formatting and map lookups during scroll
    val calendarWeeks = remember(
        daysInMonth,
        startOffset,
        currentYear,
        currentMonth,
        isCurrentViewingMonth,
        todayDay,
        completionsByDate,
        totalHabitsCount
    ) {
        val totalSlots = startOffset + daysInMonth
        val rows = (totalSlots + 6) / 7
        (0 until rows).map { r ->
            (0 until 7).map { c ->
                val slotIndex = r * 7 + c
                val dayNumber = slotIndex - startOffset + 1
                if (dayNumber in 1..daysInMonth) {
                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", currentYear, currentMonth + 1, dayNumber)
                    val completed = completionsByDate[dateStr] ?: 0
                    val isToday = isCurrentViewingMonth && (dayNumber == todayDay)
                    val completionType = when {
                        completed > 0 && totalHabitsCount > 0 && completed >= totalHabitsCount -> DayCompletionType.ALL
                        completed > 0 -> DayCompletionType.PARTIAL
                        isToday -> DayCompletionType.TODAY_EMPTY
                        else -> DayCompletionType.EMPTY
                    }
                    CalendarGridCell(dayNumber = dayNumber, completionType = completionType, isToday = isToday)
                } else {
                    CalendarGridCell(dayNumber = null, completionType = DayCompletionType.EMPTY, isToday = false)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Column {
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
                        text = "KALENDER & STREAK",
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Matriks konsistensi harian & pencapaian streak",
                    color = MonoGray,
                    fontSize = 11.sp
                )
            }
        }

        // Calendar Card
        item {
            Surface(
                color = MonoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onChangeMonth(-1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Sebelumnya")
                        }

                        Text(
                            text = monthYearTitle,
                            color = MonoBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        IconButton(
                            onClick = { onChangeMonth(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Berikutnya")
                        }
                    }

                    // Days of week row
                    val dayNames = listOf("MIN", "SEN", "SEL", "RAB", "KAM", "JUM", "SAB")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayNames.forEachIndexed { index, name ->
                            Text(
                                text = name,
                                modifier = Modifier.weight(1f),
                                color = if (index == 0) BrandRed else MonoGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    // Precomputed Calendar Grid
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        calendarWeeks.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                week.forEach { cell ->
                                    CalendarDayCellItem(
                                        cell = cell,
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(MonoBlack))
                            Text("Semua Selesai", fontSize = 9.sp, color = MonoGray, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF737373)))
                            Text("Sebagian", fontSize = 9.sp, color = MonoGray, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).border(1.dp, MonoBorder).background(Color(0xFFF9F9F9)))
                            Text("Kosong", fontSize = 9.sp, color = MonoGray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Streak Breakdown Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = BrandRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "PENCAPAIAN STREAK KEBIASAAN",
                    color = MonoBlack,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        if (habits.isEmpty()) {
            item {
                Text(
                    text = "Belum ada kebiasaan untuk ditampilkan.",
                    color = MonoGray,
                    fontSize = 12.sp
                )
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                StreakCardItem(habit = habit)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarDayCellItem(
    cell: CalendarGridCell,
    modifier: Modifier = Modifier
) {
    if (cell.dayNumber == null) {
        Box(modifier = modifier)
        return
    }

    val (bgColor, textColor, borderColor) = when (cell.completionType) {
        DayCompletionType.ALL -> Triple(MonoBlack, Color.White, MonoBlack)
        DayCompletionType.PARTIAL -> Triple(Color(0xFF737373), Color.White, Color(0xFF737373))
        DayCompletionType.TODAY_EMPTY -> Triple(Color.White, BrandRed, BrandRed)
        DayCompletionType.EMPTY -> Triple(Color(0xFFF9F9F9), MonoDark, Color(0xFFEBEBEB))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .border(
                if (cell.isToday) 1.5.dp else 0.5.dp,
                borderColor,
                RoundedCornerShape(3.dp)
            )
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${cell.dayNumber}",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = if (cell.isToday || cell.completionType != DayCompletionType.EMPTY) FontWeight.Black else FontWeight.Bold
            )
            if (cell.completionType == DayCompletionType.ALL || cell.completionType == DayCompletionType.PARTIAL) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (bgColor == MonoBlack) BrandRed else Color.White)
                )
            }
        }
    }
}

@Composable
private fun StreakCardItem(
    habit: Habit,
    modifier: Modifier = Modifier
) {
    val progress = remember(habit.currentStreak, habit.bestStreak) {
        if (habit.bestStreak > 0) {
            (habit.currentStreak.toFloat() / habit.bestStreak).coerceIn(0f, 1f)
        } else 0f
    }

    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = habit.name,
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = habit.category,
                        color = MonoGray,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${habit.currentStreak} hr",
                            color = BrandRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "(Rekor: ${habit.bestStreak} hr)",
                        color = MonoGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (habit.currentStreak >= habit.bestStreak && habit.bestStreak > 0) BrandRed else MonoBlack,
                trackColor = Color(0xFFE5E5E5)
            )
        }
    }
}
