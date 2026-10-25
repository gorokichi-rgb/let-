package com.example.countdown

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Scheduler.scheduleAll(this)
        setContent { MaterialTheme { App() } }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf(Store.load(ctx)) }
    var hour by remember { mutableIntStateOf(Store.hour(ctx)) }
    var minute by remember { mutableIntStateOf(Store.minute(ctx)) }
    var showAdd by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun commit(list: List<Ev>) {
        Store.save(ctx, list)
        events = Store.load(ctx)
        scope.launch { CountdownWidget().updateAll(ctx) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("＋") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("朝の通知: %02d:%02d".format(hour, minute), Modifier.weight(1f))
                TextButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m ->
                        Store.setTime(ctx, h, m)
                        hour = h; minute = m
                        Scheduler.scheduleAll(ctx)
                    }, hour, minute, true).show()
                }) { Text("変更") }
            }
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(events, key = { it.id }) { ev ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ev.title, style = MaterialTheme.typography.titleMedium)
                            Text(LocalDate.ofEpochDay(ev.epochDay).toString(), style = MaterialTheme.typography.bodySmall)
                        }
                        val n = Store.daysLeft(ev)
                        Text(if (n == 0L) "今日！" else if (n > 0) "あと${n}日" else "${-n}日前")
                        TextButton(onClick = { commit(events.filter { it.id != ev.id }) }) { Text("削除") }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (showAdd) {
        AddDialog(
            onDismiss = { showAdd = false },
            onAdd = { title, date ->
                commit(events + Ev(System.currentTimeMillis(), title, date.toEpochDay()))
                showAdd = false
            }
        )
    }
}

@Composable
fun AddDialog(onDismiss: () -> Unit, onAdd: (String, LocalDate) -> Unit) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().plusDays(30)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("予定を追加") },
        text = {
            Column {
                OutlinedTextField(title, { title = it }, label = { Text("何まで？（例：試験）") }, singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = {
                    DatePickerDialog(ctx, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) },
                        date.year, date.monthValue - 1, date.dayOfMonth).show()
                }) { Text("日付: $date") }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onAdd(title.trim(), date) }) { Text("追加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
