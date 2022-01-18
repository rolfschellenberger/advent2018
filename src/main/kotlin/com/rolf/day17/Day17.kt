package com.rolf.day17

import com.rolf.Day
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
    private val allowedFieldsToTravel = setOf(open, waterDown)

    private val waterSpring = Point(500, 0)
    private var minX = waterSpring
    private var maxX = waterSpring
    private var minY = waterSpring
    private var maxY = waterSpring

    override fun solve1(lines: List<String>) {
        val sandLocations = lines.map { parseLocations(it) }.flatten() + waterSpring
        minX = sandLocations.minByOrNull { it.x }!!
        maxX = sandLocations.maxByOrNull { it.x }!!
        minY = sandLocations.minByOrNull { it.y }!!
        maxY = sandLocations.maxByOrNull { it.y }!!
        val grid = MatrixString.buildDefault(maxX.x + 2, maxY.y + 1, open)
        sandLocations.forEach { grid.set(it, wall) }

        solve(grid, waterSpring)
        printGrid(grid)
        println(grid.count(setOf(water, waterDown)) - 1)
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

    private fun solve(grid: MatrixString, waterSpring: Point) {
        val waterDrops = mutableListOf(waterSpring)
        var lowestPoint = waterSpring.y
        while (waterDrops.isNotEmpty()) {
            // Drop down to a bottom.
            val waterDrop = waterDrops.removeFirst()
            val down = findDown(grid, waterDrop, "#").toMutableList() // TODO: ~ or #?
            down.forEach { grid.set(it, waterDown) }
            // FIXME: Take the lowest position and keep moving up until it overflows. This might mean you go higher than before
            // FIXME: Remember previous water drop location(s)
            lowestPoint = maxOf(lowestPoint, down.last().y)

            if (down.isNotEmpty()) {
                // Find the min and max x value to detect any 'water overflow' during the next step
                val (minX, maxX) = findWidth(grid, down.last())
                val xRange = minX.x..maxX.x

                // Move up and scan every line left/right from the drop until a wall is hit or when they overflow
                var overflow = false
                while (!overflow) {
                    val line = down.removeLast()
                    val left = findLeft(grid, line, "#")
                    val right = findRight(grid, line, "#")

                    // Let water move down from all these points until it hits a wall
                    val flowDown = flowDown(grid, (left + right).filter { it.x in xRange })

                    // Draw the water
                    for (point in (left + right + flowDown + line).filter { it.x in xRange }) {
                        grid.set(point, water)
                    }

                    // The overflow (can be one or two sides) is the start of a next iteration, but only if the
                    // drop down is below the previous drop down
                    val overFlowLeft = left.filter { it.x < xRange.first }
                    val overFlowRight = right.filter { it.x > xRange.last }
                    for (ol in overFlowLeft) {
                        grid.set(ol, "|")
                        val od = findDown(grid, ol, "#")
                        if (od.size > 1 && od.last().y > lowestPoint) {
                            overflow = true
                            waterDrops.add(ol)
                            break
                        }
                    }
                    for (or in overFlowRight) {
                        grid.set(or, "|")
                        val od = findDown(grid, or, "#")
                        if (od.size > 1 && od.last().y > lowestPoint) {
                            overflow = true
                            waterDrops.add(or)
                            break
                        }
                    }
                }

                // When a line is found with an overflow, drop down water on every position of the last line within the x range

            }
        }


//        // Start by traveling downward until we hit a surface.
//        val aboveSurface = moveDown(grid, waterDrop)
////            printGrid(grid)
//        if (aboveSurface != null) {
//            // When this location is the lowest y measured, keep the water drop, otherwise another will take us lower
////                if (waterDrops.isEmpty() || aboveSurface.y >= lowestY) {
////                    lowestY = aboveSurface.y
////                    waterDrops.add(waterDrop)
////                }
//            var foundNewDrop = false
//
//            // Move left and right and check if we can continue moving down
//            val leftDrop = moveLeft(grid, aboveSurface)
//            if (leftDrop != null) {
//                foundNewDrop = true
//                waterDrops.add(leftDrop)
//            }
////                printGrid(grid)
//            val rightDrop = moveRight(grid, aboveSurface)
//            if (rightDrop != null) {
//                foundNewDrop = true
//                waterDrops.add(rightDrop)
//            }
////                printGrid(grid)
//
//            // When new water drops are found, start over
////                if (!foundNewDrop) {
////                    waterDrops.add(waterDrop)
////                }
//
//            // Add water drop when there is no other water drop
//            if (waterDrops.isEmpty()) {
//                waterDrops.add(waterDrop)
//            }
//        }
    }

    private fun flowDown(grid: MatrixString, points: List<Point>): List<Point> {
        return points.map { findDown(grid, it, "#") }.flatten()
    }

    private fun findWidth(grid: MatrixString, point: Point): Pair<Point, Point> {
        val left = findLeft(grid, point, "#")
        val right = findRight(grid, point, "#")
        return left.last() to right.last()
    }

    private fun findDown(grid: MatrixString, start: Point, stop: String): List<Point> {
        val result = mutableListOf(start)
        var down = grid.getDown(start)
        while (down != null && grid.get(down) != stop) {
            result.add(down)
            down = grid.getDown(down)
        }
        return result
    }

    private fun findLeft(grid: MatrixString, start: Point, stop: String): List<Point> {
        val result = mutableListOf(start)
        var left = grid.getLeft(start)
        while (left != null && grid.get(left) != stop) {
            result.add(left)
            left = grid.getLeft(left)
        }
        return result
    }

    private fun findRight(grid: MatrixString, start: Point, stop: String): List<Point> {
        val result = mutableListOf(start)
        var right = grid.getRight(start)
        while (right != null && grid.get(right) != stop) {
            result.add(right)
            right = grid.getRight(right)
        }
        return result
    }

    private fun moveDown(grid: MatrixString, start: Point): Point? {
        grid.set(start, waterDown)

        var previous = start
        var down = grid.getDown(start)
        while (down != null && grid.get(down) in allowedFieldsToTravel) {
            grid.set(down, waterDown)
            previous = down
            down = grid.getDown(down)
        }

        // We hit the boundary of the grid OR a non-allowed field
        return if (down != null) previous else down
    }

    private fun moveLeft(grid: MatrixString, start: Point): Point? {
        grid.set(start, water)

        // While moving left, look for the option to move down again
        var left = grid.getLeft(start)
        while (left != null && grid.get(left) in allowedFieldsToTravel) {
            grid.set(left, water)
            // Can we move down
            val down = grid.getDown(left)
            if (down != null && grid.get(down) in allowedFieldsToTravel) {
                return left
            }
            left = grid.getLeft(left)
        }

        // We hit the boundary of the grid OR a non-allowed field, stop moving left
        return null
    }

    private fun moveRight(grid: MatrixString, start: Point): Point? {
        grid.set(start, water)

        // While moving right, look for the option to move down again
        var right = grid.getRight(start)
        while (right != null && grid.get(right) in allowedFieldsToTravel) {
            grid.set(right, water)
            // Can we move down
            val down = grid.getDown(right)
            if (down != null && grid.get(down) in allowedFieldsToTravel) {
                return right
            }
            right = grid.getRight(right)
        }

        // We hit the boundary of the grid OR a non-allowed field, stop moving left
        return null
    }

    private fun printGrid(grid: MatrixString) {
        println()
        println(grid.subMatrix(Point(minX.x - 1, minY.y), Point(maxX.x + 1, maxY.y)))
    }

    override fun solve2(lines: List<String>) {
    }
}
