package com.rolf.day01

import com.rolf.Day

fun main() {
    Day01().run()
}

class Day01 : Day() {
    override fun solve1(lines: List<String>) {
        val numbers = lines.map { it.toInt() }
        println(numbers.sum())
    }

    override fun solve2(lines: List<String>) {
        var frequency = 0
        val frequencies = mutableSetOf(frequency)
        val numbers = lines.map { it.toInt() }
        frequencies.add(frequency)
        while (true) {
            for (number in numbers) {
                frequency += number
                if (!frequencies.add(frequency)) {
                    println(frequency)
                    return
                }
            }
        }
    }
}
