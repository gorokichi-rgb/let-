package com.example.countdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object Scheduler {
    const val ACTION_NOTIFY = "com.example.countdown.NOTIFY"
    const val ACTION_MIDNIGHT = "com.example.countdown.MIDNIGHT"

    fun scheduleAll(c: Context) {
        val am = c.getSystemService(AlarmManager::class.java)
        set(c, am, ACTION_NOTIFY, 1, next(Store.hour(c), Store.minute(c)))
        set(c, am, ACTION_MIDNIGHT, 2, next(0, 1))
    }

    private fun set(c: Context, am: AlarmManager, action: String, code: Int, at: Long) {
        val i = Intent(c, AlarmReceiver::class.java).setAction(action)
        val pi = PendingIntent.getBroadcast(
            c, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
    }

    private fun next(h: Int, m: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        return cal.timeInMillis
    }
}
