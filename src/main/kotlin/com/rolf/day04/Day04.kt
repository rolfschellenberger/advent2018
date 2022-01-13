package com.rolf.day04

import com.rolf.Day
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    Day04().run()
}

class Day04 : Day() {
    override fun solve1(lines: List<String>) {
        val guardsAsleep = parseInput(lines)
        val guardWithMaxSleep =
            guardsAsleep

                .map { (key, value) -> key to value.map { it.value }.sum() }
                .maxByOrNull { (_, value) -> value }!!
        val mostAsleepMinute = guardsAsleep[guardWithMaxSleep.first]?.maxByOrNull { (_, value) -> value }!!
        println(guardWithMaxSleep.first * mostAsleepMinute.key)
    }

    override fun solve2(lines: List<String>) {
        val guardsAsleep = parseInput(lines)
        val mostSleepOnOneMinute =
            guardsAsleep.map { (key, value) -> key to value.maxByOrNull { (_, value) -> value }!! }
                .maxByOrNull { (_, value) -> value.value }!!
        println(mostSleepOnOneMinute.first * mostSleepOnOneMinute.second.key)
    }

    private fun parseInput(lines: List<String>): MutableMap<Int, MutableMap<Int, Int>> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val guardsAsleep: MutableMap<Int, MutableMap<Int, Int>> = mutableMapOf()
        var currentGuard = 0
        var startTime = LocalDateTime.now()
        for (line in lines.sorted()) {
            val parts = line.split(" ")
            val dateTime =
                LocalDateTime.parse(parts[0].removePrefix("[") + " " + parts[1].removeSuffix("]"), dateFormatter)
            when {
                line.contains("Guard") -> currentGuard = parts[3].removePrefix("#").toInt()
                line.contains("asleep") -> startTime = dateTime
                line.contains("wakes") -> {
                    val times = guardsAsleep.computeIfAbsent(currentGuard) { mutableMapOf() }
                    while (startTime.isBefore(dateTime)) {
                        times.computeIfAbsent(startTime.minute) { 0 }
                        times[startTime.minute] = times[startTime.minute]!! + 1
                        startTime = startTime.plusMinutes(1)
                    }
                }
            }
        }
        return guardsAsleep
    }
}
