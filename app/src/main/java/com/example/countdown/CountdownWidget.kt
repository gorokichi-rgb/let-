package com.example.countdown

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val ev = Store.nearest(context)
        provideContent { Content(ev) }
    }

    @Composable
    private fun Content(ev: Ev?) {
        val white = ColorProvider(Color.White)
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF1E88E5)))
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ev == null) {
                Text("予定を追加", style = TextStyle(color = white, fontSize = 16.sp))
            } else {
                val n = Store.daysLeft(ev)
                Text(
                    ev.title,
                    style = TextStyle(color = white, fontSize = 14.sp, textAlign = TextAlign.Center),
                    maxLines = 2
                )
                Text(
                    if (n == 0L) "今日！" else "あと${n}日",
                    style = TextStyle(color = white, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}
