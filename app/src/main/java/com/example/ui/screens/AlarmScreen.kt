package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun AlarmScreen(
    habits: List<Habit>,
    selectedSoundPreset: String,
    alarmVolume: Float,
    isAudioPlaying: Boolean,
    isWakeLockEnabled: Boolean,
    onSelectPreset: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onTestSound: (String) -> Unit,
    onStopAudio: () -> Unit,
    onTriggerAlarm: (Habit) -> Unit,
    onToggleHabitAlarm: (Habit) -> Unit,
    onToggleWakeLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        SoundPresetInfo("Lembut", "Chime relaksasi bertingkat", Icons.Default.MusicNote),
        SoundPresetInfo("Bel Klasik", "Dentang resonansi harmonik", Icons.Default.Notifications),
        SoundPresetInfo("Digital Beep", "Pola weker digital modern", Icons.Default.Alarm),
        SoundPresetInfo("Sirene", "Modulasi frekuensi tajam", Icons.Default.Warning)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header
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
                        text = "AUDIO SYNTHESIZER & ALARM",
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Synthesizer Audio Native murni (100% offline & mandiri)",
                    color = MonoGray,
                    fontSize = 11.sp
                )
            }
        }

        // Sound Studio Card
        item {
            Surface(
                color = MonoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PILIHAN NADA SUARA",
                            color = MonoBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .border(1.dp, BrandRed, RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NATIVE SYNTH",
                                color = BrandRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // 2x2 Preset Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetBox(
                                preset = presets[0],
                                isSelected = selectedSoundPreset == presets[0].name,
                                onClick = { onSelectPreset(presets[0].name) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetBox(
                                preset = presets[1],
                                isSelected = selectedSoundPreset == presets[1].name,
                                onClick = { onSelectPreset(presets[1].name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetBox(
                                preset = presets[2],
                                isSelected = selectedSoundPreset == presets[2].name,
                                onClick = { onSelectPreset(presets[2].name) },
                                modifier = Modifier.weight(1f)
                            )
                            PresetBox(
                                preset = presets[3],
                                isSelected = selectedSoundPreset == presets[3].name,
                                onClick = { onSelectPreset(presets[3].name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Volume slider
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VOLUME ALARM",
                                color = MonoGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${(alarmVolume * 100).toInt()}%",
                                color = MonoBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Slider(
                            value = alarmVolume,
                            onValueChange = onVolumeChange,
                            colors = SliderDefaults.colors(
                                thumbColor = MonoBlack,
                                activeTrackColor = MonoBlack,
                                inactiveTrackColor = Color(0xFFE5E5E5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Play / Stop buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTestSound(selectedSoundPreset) },
                            colors = ButtonDefaults.buttonColors(containerColor = MonoBlack),
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_sound_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = "UJI NADA SUARA",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onStopAudio,
                            shape = RoundedCornerShape(3.dp),
                            modifier = Modifier.testTag("stop_sound_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("STOP", color = MonoBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Screen Wake Lock & Keep Screen On Card
        item {
            Surface(
                color = MonoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = MonoBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "SCREEN WAKE LOCK",
                                color = MonoBlack,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isWakeLockEnabled)
                                "Layar akan tetap aktif dan tidak redup/terkunci."
                            else
                                "Mencegah layar meredup saat menunggu alarm tiba.",
                            color = MonoGray,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isWakeLockEnabled,
                        onCheckedChange = { onToggleWakeLock() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MonoBlack
                        )
                    )
                }
            }
        }

        // Active Alarm Schedules List
        item {
            Text(
                text = "DAFTAR ALARM RUTINITAS",
                color = MonoBlack,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        if (habits.isEmpty()) {
            item {
                Text(
                    text = "Belum ada rutinitas yang terdaftar.",
                    color = MonoGray,
                    fontSize = 12.sp
                )
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                Surface(
                    color = MonoSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                text = "${habit.targetTime} • Nada: ${habit.soundPreset}",
                                color = MonoGray,
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { onTriggerAlarm(habit) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Test Alarm",
                                    tint = BrandRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Switch(
                                checked = habit.alarmEnabled,
                                onCheckedChange = { onToggleHabitAlarm(habit) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MonoBlack
                                )
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class SoundPresetInfo(
    val name: String,
    val desc: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun PresetBox(
    preset: SoundPresetInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) MonoBlack else MonoBorder,
                RoundedCornerShape(3.dp)
            )
            .background(if (isSelected) Color(0xFFF7F7F7) else Color.White)
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name.uppercase(),
                    color = MonoBlack,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = preset.icon,
                    contentDescription = null,
                    tint = if (isSelected) BrandRed else MonoGray,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = preset.desc,
                color = MonoGray,
                fontSize = 9.sp,
                lineHeight = 12.sp
            )
        }
    }
}
