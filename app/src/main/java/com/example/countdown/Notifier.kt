package com.example.countdown

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object Notifier {
    fun show(c: Context) {
        val upcoming = Store.load(c).filter { Store.daysLeft(it) >= 0 }
        if (upcoming.isEmpty()) return
        if (Build.VERSION.SDK_INT >= 33 &&
            c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("daily", "毎朝のカウントダウン", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val lines = upcoming.map { Store.text(it) }
        val open = PendingIntent.getActivity(
            c, 0, Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(c, "daily")
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle(lines.first())
            .setContentText(lines.drop(1).joinToString(" / "))
            .setStyle(Notification.BigTextStyle().bigText(lines.joinToString("\n")))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        nm.notify(1, n)
    }
}
