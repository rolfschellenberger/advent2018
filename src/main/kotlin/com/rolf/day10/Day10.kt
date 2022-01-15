package com.rolf.day10

import com.rolf.Day
import com.rolf.util.MatrixString
import com.rolf.util.Point
import com.rolf.util.splitLine

fun main() {
    Day10().run()
}

class Day10 : Day() {
    override fun solve1(lines: List<String>) {
        println(buildGrid(lines).second)
        // HJBJXRAZ
    }

    override fun solve2(lines: List<String>) {
        println(buildGrid(lines).first)
    }

    private fun buildGrid(lines: List<String>): Pair<Int, MatrixString> {
        var locations = lines.map { parseLocation(it) }

        // Sum the different x and y locations to find the minimum set
        // Also keep track of the min and max for x and y in that location
        var minDiff = Int.MAX_VALUE
        var minX = Int.MAX_VALUE
        var maxX = Int.MIN_VALUE
        var minY = Int.MAX_VALUE
        var maxY = Int.MIN_VALUE
        var step = 0

        for (i in 0 until 20000) {
            val x = locations.map { it.position.x }.toSet()
            val y = locations.map { it.position.y }.toSet()
            val diff = x.size + y.size
            if (diff < minDiff) {
                minDiff = diff
                step = i
                minX = x.minOrNull()!!
                maxX = x.maxOrNull()!!
                minY = y.minOrNull()!!
                maxY = y.maxOrNull()!!
            }
            locations.forEach { it.move() }
        }

        // Now we found the step where the locations have the smallest different x and y values
        // We re-iterate until this step and print the letters in a grid grid range
        locations = lines.map { parseLocation(it) }
        locations.forEach { for (i in 0 until step) it.move() }
        val grid = MatrixString.buildDefault(maxX + 1, maxY + 1, " ")
        for (location in locations) {
            grid.set(location.position.x, location.position.y, "#")
        }
        grid.cutOut(Point(minX, minY), Point(maxX, maxY))
        return step to grid
    }

    private fun parseLocation(line: String): Location {
        val parts = splitLine(line, pattern = "\\w+=".toPattern())
        val position = parsePoint(parts[1])
        val velocity = parsePoint(parts[2])
        return Location(position, velocity)
    }

    private fun parsePoint(coords: String): Point {
        val (x, y) = coords.removePrefix("<").removeSuffix(" ").removeSuffix(">").split(", ").map { it.trim().toInt() }
        return Point(x, y)
    }
}

data class Location(var position: Point, val velocity: Point) {
    fun move() {
        position = Point(position.x + velocity.x, position.y + velocity.y)
    }
}
