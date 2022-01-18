package com.rolf.day17

import com.rolf.Day
import com.rolf.util.Matrix
import com.rolf.util.MatrixString
import com.rolf.util.Point
import com.rolf.util.splitLine

fun main() {
    Day17().run()
}

class Day17 : Day() {
    private val open = "."
    private val wall = "#"
    private val water = "~"
    private val waterDown = "|"

    private val waterSpring = Point(500, 0)
    private var minX = waterSpring
    private var maxX = waterSpring
    private var minY = waterSpring
    private var maxY = waterSpring

    override fun solve1(lines: List<String>) {
        val result = solve(lines)
        println(result.count(setOf(water, waterDown)))
    }

    override fun solve2(lines: List<String>) {
        val result = solve(lines)
        println(result.count(water))
    }

    private fun solve(lines: List<String>): Matrix<String> {
        val sandLocations = lines.map { parseLocations(it) }.flatten()
        minX = sandLocations.minByOrNull { it.x }!!
        maxX = sandLocations.maxByOrNull { it.x }!!
        minY = sandLocations.minByOrNull { it.y }!!
        maxY = sandLocations.maxByOrNull { it.y }!!
        val grid = MatrixString.buildDefault(maxX.x + 2, maxY.y + 1, open)
        sandLocations.forEach { grid.set(it, wall) }

        walk(grid, waterSpring)
        return grid.subMatrix(Point(minX.x - 1, minY.y), Point(maxX.x + 1, maxY.y))
    }

    private fun parseLocations(line: String): List<Point> {
        val (a, bMin, bMax) = splitLine(line, pattern = "\\D+".toPattern())
            .filter { it.isNotEmpty() }
            .map { it.toInt() }
        return if (line.first() == 'x') {
            (bMin..bMax).map { Point(a, it) }
        } else {
            (bMin..bMax).map { Point(it, a) }
        }
    }

    private fun walk(grid: MatrixString, location: Point) {
        val down = grid.getDown(location) ?: return
        if (grid.get(down) == open) {
            grid.set(down, waterDown)
            walk(grid, down)
        }

        // Check down value AGAIN, since it is most likely changed
        val downVal = grid.get(down)
        val left = grid.getLeft(location)
        val leftVal = left?.let { grid.get(it) }
        if (leftVal == open && downVal in setOf(water, wall)) {
            grid.set(location, waterDown)
            grid.set(left, waterDown)
            walk(grid, left)
        }

        val right = grid.getRight(location)
        val rightVal = right?.let { grid.get(it) }
        if (rightVal == open && downVal in setOf(water, wall)) {
            grid.set(location, waterDown)
            grid.set(right, waterDown)
            walk(grid, right)
        }

        fillBetweenWalls(grid, location)
    }

    private fun fillBetweenWalls(grid: MatrixString, location: Point) {
        val left = findLeft(grid, location)
        val right = findRight(grid, location)
        if (left.isNotEmpty() && right.isNotEmpty()) {
            for (point in left + right) {
                grid.set(point, water)
            }
        }
    }

    private fun findLeft(grid: MatrixString, start: Point): List<Point> {
        val result = mutableListOf(start)
        var current = grid.getLeft(start)
        while (current != null) {
            val value = grid.get(current)
            if (value == wall) {
                return result
            }
            if (value != waterDown) {
                return emptyList()
            }
            result.add(current)
            current = grid.getLeft(current)
        }
        return emptyList()
    }

    private fun findRight(grid: MatrixString, start: Point): List<Point> {
        val result = mutableListOf(start)
        var current = grid.getRight(start)
        while (current != null) {
            val value = grid.get(current)
            if (value == wall) {
                return result
            }
            if (value != waterDown) {
                return emptyList()
            }
            result.add(current)
            current = grid.getRight(current)
        }
        return emptyList()
    }
}
