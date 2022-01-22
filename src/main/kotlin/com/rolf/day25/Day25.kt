package com.rolf.day25

import com.rolf.Day
import com.rolf.util.splitLine
import kotlin.math.abs

fun main() {
    Day25().run()
}

class Day25 : Day() {
    override fun solve1(lines: List<String>) {
        val locations = lines.map { parseLocation(it) }.toMutableList()

        val constellations = mutableSetOf<Constellation>()
        while (locations.isNotEmpty()) {
            val location = locations.removeFirst()
            val constellation = buildConstellation(location, locations)
            constellations.add(constellation)
            locations.removeAll(constellation.locations)
        }
        println(constellations.size)
    }

    override fun solve2(lines: List<String>) {
    }

    private fun buildConstellation(startLocation: List<Int>, locations: List<List<Int>>): Constellation {
        val constellation = Constellation(mutableSetOf(startLocation.toList()))
        var added = true
        while (added) {
            added = false
            for (location in locations) {
                val distance = constellation.minDistance(location)
                if (distance in 1..3) {
                    if (constellation.locations.add(location)) {
                        added = true
                    }
                }
            }
        }
        return constellation
    }

    private fun parseLocation(line: String): List<Int> {
        return splitLine(line, ",")
            .map { it.toInt() }
    }
}

data class Constellation(val locations: MutableSet<List<Int>>) {
    fun minDistance(other: List<Int>): Int {
        return locations.map { it.distance(other) }.minOrNull()!!
    }
}

fun List<Int>.distance(other: List<Int>): Int {
    var distance = 0
    for (i in indices) {
        distance += abs(this[i] - other[i])
    }
    return distance
}
