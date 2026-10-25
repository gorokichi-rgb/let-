package com.example.countdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (i.action == Scheduler.ACTION_NOTIFY) Notifier.show(c)
                Scheduler.scheduleAll(c)
                CountdownWidget().updateAll(c)
            } finally {
                pending.finish()
            }
        }
    }
}
