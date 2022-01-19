package com.rolf.day18

import com.rolf.Day
import com.rolf.util.MatrixString
import com.rolf.util.splitLines

fun main() {
    Day18().run()
}

class Day18 : Day() {
    override fun solve1(lines: List<String>) {
        val grid = MatrixString.build(splitLines(lines))
        for (i in 0 until 10) {
            updateGrid(grid)
        }
        println(grid.count("|") * grid.count("#"))
    }

    override fun solve2(lines: List<String>) {
        val grid = MatrixString.build(splitLines(lines))

        // Find some repetition.
        val gridCaches = mutableMapOf<MatrixString, Int>()
        var nextStart = 0
        var iterationSize = 0
        for (i in 0 until 1000) {
            updateGrid(grid)
            val copy = grid.copy()
            if (gridCaches.contains(copy)) {
                nextStart = i
                iterationSize = i - gridCaches[copy]!!
                break
            }
            gridCaches[copy] = i
        }

        // Only do the last few iterations
        for (i in 0 until ((1000000000 - nextStart) % iterationSize) - 1) {
            updateGrid(grid)
        }
        println(grid.count("|") * grid.count("#"))
    }

    private fun updateGrid(grid: MatrixString) {
        val copy = grid.copy()
        for (point in copy.allPoints()) {
            when (copy.get(point)) {
                "." -> {
                    // An open acre will become filled with trees if three or more adjacent acres contained trees. Otherwise, nothing happens.
                    if (copy.getNeighbours(point).map { copy.get(it) }.count { it == "|" } >= 3) {
                        grid.set(point, "|")
                    }
                }
                "|" -> {
                    // An acre filled with trees will become a lumberyard if three or more adjacent acres were lumberyards. Otherwise, nothing happens.
                    if (copy.getNeighbours(point).map { copy.get(it) }.count { it == "#" } >= 3) {
                        grid.set(point, "#")
                    }
                }
                "#" -> {
                    // An acre containing a lumberyard will remain a lumberyard if it was adjacent to at least one other lumberyard and at least one acre containing trees. Otherwise, it becomes open.
                    val neighbours = copy.getNeighbours(point).map { copy.get(it) }
                    if (neighbours.count { it == "#" } == 0 || neighbours.count { it == "|" } == 0) {
                        grid.set(point, ".")
                    }
                }
            }
        }
    }
}
