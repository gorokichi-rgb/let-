package com.example.countdown

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Ev(val id: Long, val title: String, val epochDay: Long)

object Store {
    private fun p(c: Context) = c.getSharedPreferences("countdown", Context.MODE_PRIVATE)

    fun load(c: Context): List<Ev> {
        val arr = JSONArray(p(c).getString("events", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            Ev(o.getLong("id"), o.getString("title"), o.getLong("day"))
        }.sortedBy { it.epochDay }
    }

    fun save(c: Context, list: List<Ev>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("title", it.title).put("day", it.epochDay))
        }
        p(c).edit().putString("events", arr.toString()).apply()
    }

    fun hour(c: Context) = p(c).getInt("hour", 7)
    fun minute(c: Context) = p(c).getInt("minute", 0)
    fun setTime(c: Context, h: Int, m: Int) {
        p(c).edit().putInt("hour", h).putInt("minute", m).apply()
    }

    fun daysLeft(e: Ev): Long = e.epochDay - LocalDate.now().toEpochDay()

    fun nearest(c: Context): Ev? =
        load(c).filter { daysLeft(it) >= 0 }.minByOrNull { it.epochDay }

    fun text(e: Ev): String {
        val n = daysLeft(e)
        return when {
            n == 0L -> "${e.title}は今日！"
            n > 0 -> "${e.title}まであと${n}日"
            else -> "${e.title}から${-n}日経過"
        }
    }
}
