package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.data.TaskItem
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
fun BackupScreen(
    habits: List<Habit>,
    tasks: List<TaskItem>,
    onExportJson: () -> String,
    onImportJson: (String, (Boolean) -> Unit) -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    var actionNotice by remember { mutableStateOf<String?>(null) }

    fun shareText(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun copyToClipboard(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        actionNotice = "$label telah disalin ke papan klip!"
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
                        text = "CADANGAN & EKSPOR DATA",
                        color = MonoBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "Ekspor rutinitas, streak, dan tugas ke berbagai format",
                    color = MonoGray,
                    fontSize = 11.sp
                )
            }
        }

        // Action Feedback Notice
        if (actionNotice != null) {
            item {
                Surface(
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = actionNotice ?: "",
                            color = Color(0xFF166534),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Export Options Card
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
                    Text(
                        text = "FORMAT EKSPOR",
                        color = MonoBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    // JSON Backup
                    ExportOptionRow(
                        icon = Icons.Default.DataObject,
                        title = "CADANGAN JSON LENGKAP",
                        subtitle = "Menyimpan seluruh data rutinitas, streak, dan tugas",
                        onShare = {
                            val json = onExportJson()
                            shareText(json, "MonoRoutine-Backup.json")
                        },
                        onCopy = {
                            val json = onExportJson()
                            copyToClipboard(json, "JSON Backup")
                        }
                    )

                    // CSV Table
                    ExportOptionRow(
                        icon = Icons.Default.TableChart,
                        title = "TABEL CSV",
                        subtitle = "Dapat dibuka di Microsoft Excel atau Google Sheets",
                        onShare = {
                            val csv = buildCsv(habits, tasks)
                            shareText(csv, "MonoRoutine-Report.csv")
                        },
                        onCopy = {
                            val csv = buildCsv(habits, tasks)
                            copyToClipboard(csv, "Tabel CSV")
                        }
                    )

                    // TXT Summary
                    ExportOptionRow(
                        icon = Icons.Default.Description,
                        title = "RINGKASAN TEKS",
                        subtitle = "Rangkuman daftar rutinitas & deadline yang rapi dibaca",
                        onShare = {
                            val txt = buildSummaryText(habits, tasks)
                            shareText(txt, "Ringkasan MonoRoutine.txt")
                        },
                        onCopy = {
                            val txt = buildSummaryText(habits, tasks)
                            copyToClipboard(txt, "Ringkasan Teks")
                        }
                    )
                }
            }
        }

        // Import / Restore Card
        item {
            Surface(
                color = MonoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "PULIHKAN DATA (RESTORE)",
                        color = MonoBlack,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Tempel teks cadangan JSON yang telah diekspor untuk memulihkan data.",
                        color = MonoGray,
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = { showImportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MonoBlack),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "TEMPEL & PULIHKAN DATA JSON",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Reset Data Card
        item {
            Surface(
                color = MonoSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandRed),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ATUR ULANG DATA (RESET)",
                        color = BrandRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Menghapus seluruh entri kustom dan mengembalikan database ke paket rutinitas awal.",
                        color = MonoGray,
                        fontSize = 11.sp
                    )

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = BrandRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "RESET KE CONTOH AWAL",
                            color = BrandRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Native Engine Info Badge
        item {
            Surface(
                color = Color(0xFFF9F9F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = MonoBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "100% ANDROID NATIVE ENGINE",
                            color = MonoBlack,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Ditenagai Room SQLite Database, Jetpack Compose, & AudioTrack Synthesizer mandiri.",
                            color = MonoGray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text("Konfirmasi Reset Data", fontWeight = FontWeight.Black)
            },
            text = {
                Text("Semua kebiasaan, catatan tugas, dan riwayat streak saat ini akan diganti dengan data contoh default. Lanjutkan?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Ya, Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Batal", color = MonoBlack)
                }
            }
        )
    }

    // Import JSON Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text("Pulihkan dari JSON", fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tempel teks JSON hasil ekspor cadangan ke kolom di bawah ini:", fontSize = 11.sp, color = MonoGray)
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        placeholder = { Text("{\"version\": 1, ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onImportJson(importInputText) { success ->
                            if (success) {
                                showImportDialog = false
                                importInputText = ""
                            }
                        }
                    },
                    enabled = importInputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MonoBlack)
                ) {
                    Text("Impor Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Batal", color = MonoGray)
                }
            }
        )
    }
}

@Composable
private fun ExportOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = Color(0xFFFAFAFA),
        border = androidx.compose.foundation.BorderStroke(1.dp, MonoBorder),
        shape = RoundedCornerShape(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MonoBlack)
                        .clip(RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MonoBlack)
                    Text(text = subtitle, fontSize = 9.sp, color = MonoGray)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .border(1.dp, MonoBorder)
                        .background(Color.White)
                        .clickable { onCopy() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text("SALIN", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MonoBlack)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(MonoBlack)
                        .clickable { onShare() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                        Text("BAGIKAN", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun buildCsv(habits: List<Habit>, tasks: List<TaskItem>): String {
    val sb = StringBuilder()
    sb.append("Tipe,Nama/Judul,Kategori/Prioritas,Waktu/Batas,Streak/Status,Catatan\n")
    for (h in habits) {
        sb.append("\"Rutinitas\",\"${h.name}\",\"${h.category}\",\"${h.targetTime}\",\"${h.currentStreak} hr\",\"${h.notes}\"\n")
    }
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    for (t in tasks) {
        val status = if (t.isCompleted) "Selesai" else "Belum Selesai"
        val dStr = sdf.format(Date(t.deadline))
        sb.append("\"Tugas\",\"${t.title}\",\"${t.priority}\",\"$dStr\",\"$status\",\"${t.notes}\"\n")
    }
    return sb.toString()
}

private fun buildSummaryText(habits: List<Habit>, tasks: List<TaskItem>): String {
    val sb = StringBuilder()
    sb.append("=== LAPORAN MONOROUTINE ===\n")
    sb.append("Tanggal: ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())}\n\n")

    sb.append("--- DAFTAR RUTINITAS ---\n")
    for ((index, h) in habits.withIndex()) {
        sb.append("${index + 1}. ${h.name} (${h.category}) - Target: ${h.targetTime} WIB | Streak: ${h.currentStreak} hari\n")
    }

    sb.append("\n--- DAFTAR TUGAS & DEADLINE ---\n")
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    for ((index, t) in tasks.withIndex()) {
        val status = if (t.isCompleted) "[SELESAI]" else "[PENDING]"
        sb.append("${index + 1}. $status ${t.title} (Prioritas: ${t.priority}) - Batas: ${sdf.format(Date(t.deadline))}\n")
    }
    return sb.toString()
}
