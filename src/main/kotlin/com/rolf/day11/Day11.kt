package com.rolf.day11

import com.rolf.Day
import com.rolf.util.MatrixInt
import com.rolf.util.Point

fun main() {
    Day11().run()
}

class Day11 : Day() {
    override fun solve1(lines: List<String>) {
        val serialNumber = lines.first().toInt()
        val grid = MatrixInt.buildDefault(300, 300, 0)
        for (point in grid.allPoints()) {
            val powerLevel = calculatePowerLevel(point, serialNumber)
            grid.set(point, powerLevel)
        }

        // Find the largest power cell of a 3x3 size
        val size = 3
        val (position, _) = findMaxPower(grid, size)
        println("${position.x},${position.y}")
    }

    override fun solve2(lines: List<String>) {
        val serialNumber = lines.first().toInt()
        val grid = MatrixInt.buildDefault(300, 300, 0)
        for (point in grid.allPoints()) {
            val powerLevel = calculatePowerLevel(point, serialNumber)
            grid.set(point, powerLevel)
        }

        // Find the largest power cell of a NxN size
        var maxPower = Int.MIN_VALUE
        var bestPosition = Point(0, 0)
        var bestSize = 0
        for (size in 1..300) {
            val (position, power) = findMaxPower(grid, size)
            if (power > maxPower) {
                maxPower = power
                bestPosition = position
                bestSize = size
            }
        }
        println("${bestPosition.x},${bestPosition.y},$bestSize")
    }

    private fun findMaxPower(grid: MatrixInt, size: Int): Pair<Point, Int> {
        var maxPower = Int.MIN_VALUE
        var position = Point(0, 0)
        for (y in 0 until grid.height() - size) {
            for (x in 0 until grid.width() - size) {
                val totalPower = sumCells(grid, x, y, size)
                if (totalPower > maxPower) {
                    maxPower = totalPower
                    position = Point(x + 1, y + 1)
                }
            }
        }
        return position to maxPower
    }

    private fun sumCells(grid: MatrixInt, startX: Int, startY: Int, size: Int): Int {
        return grid.subMatrix(Point(startX, startY), Point(startX + size - 1, startY + size - 1))
            .allElements().sum()
    }

    private fun calculatePowerLevel(point: Point, serialNumber: Int): Int {
        // Remember to increase x and y with 1
        val x = point.x + 1
        val y = point.y + 1
        val rackId = x + 10
        var powerLevel = rackId * y
        powerLevel += serialNumber
        powerLevel *= rackId
        val chars = powerLevel.toString().toCharArray()
        if (chars.size >= 3) {
            return chars[chars.size - 3].toString().toInt() - 5
        }
        return 0
    }
}
