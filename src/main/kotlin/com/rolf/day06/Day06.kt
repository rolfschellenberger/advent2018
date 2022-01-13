package com.rolf.day06

import com.rolf.Day
import com.rolf.util.MatrixInt
import com.rolf.util.Point

fun main() {
    Day06().run()
}

class Day06 : Day() {
    override fun solve1(lines: List<String>) {
        val locations = lines.map { parseLocation(it) }
        val grid = MatrixInt.buildDefault(500, 500, -1)

        // Calculate distance from every grid location to every location given.
        // Set the id when 1 is the closest. Set a -1 when multiple are.
        for (point in grid.allPoints()) {
            val closestLocation = locations
                .mapIndexed { index, location -> index to location.distance(point) }
                .groupBy { it.second }
                .minByOrNull { it.key }!!
            if (closestLocation.value.size == 1) {
                grid.set(point, closestLocation.value.first().first)
            }
        }

        // Find the numbers round the edges and remove them.
        val edges = grid.getTopEdge() + grid.getLeftEdge() + grid.getRightEdge() + grid.getBottomEdge()
        val toInclude = locations.indices - edges - listOf(-1)

        // Count the positions for each id
        val maxArea = grid.allElements()
            .filter { toInclude.contains(it) }
            .groupingBy { it }
            .eachCount()
            // Return the one with the highest count
            .maxByOrNull { it.value }!!
        println(maxArea.value)
    }

    override fun solve2(lines: List<String>) {
        val locations = lines.map { parseLocation(it) }
        val grid = MatrixInt.buildDefault(500, 500, 0)

        // Calculate distance from every grid location to every location given.
        // Set a 1 when it's less than 10000 for all locations.
        for (point in grid.allPoints()) {
            val sumLocationDistance = locations
                .map { it.distance(point) }
                .sum()
            if (sumLocationDistance < 10000) {
                grid.set(point, 1)
            }
        }

        // Count the ones
        println(grid.count(1))
        // 250000 too high
    }

    private fun parseLocation(line: String): Point {
        val (x, y) = line.split(", ").map { it.toInt() }
        return Point(x, y)
    }
}
