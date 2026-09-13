package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.ui.theme.BrandRed
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoDark
import com.example.ui.theme.MonoGray
import com.example.ui.theme.MonoSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonoTopAppBar(
    isWakeLockActive: Boolean,
    onToggleWakeLock: () -> Unit,
    isAlarmActive: Boolean,
    onEmergencyStopAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left branding: MonoRoutine Brand Logo + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, MonoBorder, RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(4.dp)
                        .testTag("app_logo_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_monoroutine_logo),
                        contentDescription = "Logo MonoRoutine",
                        tint = MonoBlack,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column {
                    Text(
                        text = "MONOROUTINE",
                        color = MonoBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ROUTINE & DEADLINE HUB",
                        color = MonoGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Right actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Emergency Stop button if alarm ringing
                if (isAlarmActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandRed)
                            .clickable { onEmergencyStopAlarm() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("emergency_stop_alarm_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Matikan Alarm",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "MATIKAN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Wake Lock Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            1.dp,
                            if (isWakeLockActive) MonoBlack else MonoBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .background(if (isWakeLockActive) MonoBlack else Color.White)
                        .clickable { onToggleWakeLock() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("wake_lock_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Layar Aktif",
                            tint = if (isWakeLockActive) Color.White else MonoGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isWakeLockActive) "Layar: ON" else "Layar: OFF",
                            color = if (isWakeLockActive) Color.White else MonoBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyAlarmBanner(
    activeHabit: Habit?,
    onComplete: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = activeHabit != null,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        if (activeHabit != null) {
            Surface(
                color = MonoBlack,
                modifier = modifier
                    .fillMaxWidth()
                    .border(2.dp, BrandRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandRed)
                        )
                        Column {
                            Text(
                                text = "PERINGATAN ALARM AKTIF",
                                color = BrandRed,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${activeHabit.name} • ${activeHabit.targetTime}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White)
                                .clickable { onComplete() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("alarm_banner_complete_btn")
                        ) {
                            Text(
                                text = "SELESAI",
                                color = MonoBlack,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(BrandRed)
                                .clickable { onStop() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("alarm_banner_stop_btn")
                        ) {
                            Text(
                                text = "STOP",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RingkasanHeroCard(
    completedCount: Int,
    totalCount: Int,
    bestStreak: Int,
    activeRoutines: Int,
    modifier: Modifier = Modifier
) {
    val progressPct = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    Surface(
        color = MonoSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "RINGKASAN HARI INI",
                        color = BrandRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = todayFormatted,
                        color = MonoBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.2).sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$progressPct%",
                        color = MonoBlack,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "KEPATUHAN",
                        color = MonoGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar (High contrast black bar on light gray)
            LinearProgressIndicator(
                progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MonoBlack,
                trackColor = Color(0xFFEEEEEE)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Column Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        color = MonoBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "SELESAI",
                        color = MonoGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MonoBorder)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = BrandRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$bestStreak",
                            color = BrandRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "STREAK REKOR",
                        color = MonoGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MonoBorder)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "$activeRoutines",
                        color = MonoBlack,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "RUTINITAS",
                        color = MonoGray,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Semua", "Kesehatan", "Pekerjaan", "Pribadi", "Belajar", "Keuangan", "Lainnya")
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILTER KATEGORI",
                color = MonoGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = selectedCategory,
                color = MonoBlack,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .border(
                            1.dp,
                            if (isSelected) MonoBlack else MonoBorder,
                            RoundedCornerShape(3.dp)
                        )
                        .background(if (isSelected) MonoBlack else Color.White)
                        .clickable { onSelectCategory(cat) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cat.uppercase(Locale.getDefault()),
                        color = if (isSelected) Color.White else MonoDark,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatusFilterRow(
    selectedStatus: String,
    onSelectStatus: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = listOf(
        "all" to "Semua Status",
        "today" to "Jadwal Hari Ini",
        "pending" to "Belum Selesai",
        "completed" to "Selesai"
    )
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { (key, label) ->
            val isSelected = selectedStatus == key
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .border(
                        1.dp,
                        if (isSelected) MonoBlack else MonoBorder,
                        RoundedCornerShape(3.dp)
                    )
                    .background(if (isSelected) MonoBlack else Color.White)
                    .clickable { onSelectStatus(key) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label.uppercase(Locale.getDefault()),
                    color = if (isSelected) Color.White else MonoGray,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
