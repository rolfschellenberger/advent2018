package com.rolf.day15

import com.rolf.Day
import com.rolf.util.MatrixString
import com.rolf.util.Point
import com.rolf.util.splitLines

fun main() {
    Day15().run()
}

class Day15 : Day() {
    override fun solve1(lines: List<String>) {
        // Load grid
        val grid = MatrixString.build(splitLines(lines))

        // Give every elf lower case letters and goblins upper case
        val elfs = mutableMapOf<String, Unit>()
        val goblins = mutableMapOf<String, Unit>()
        for (point in grid.allPoints()) {
            val value = grid.get(point)
            val newValue = when (value) {
                "E" -> {
                    val e = ('a' + elfs.size).toString()
                    elfs[e] = Unit(e, 200, point)
                    "."
                }
                "G" -> {
                    val g = ('A' + goblins.size).toString()
                    goblins[g] = Unit(g, 200, point)
                    "."
                }
                else -> value
            }
            grid.set(point, newValue)
        }
        println(combine(grid, elfs.values + goblins.values))

        // Take turns in order
        for (i in 0 until 5000) {
            val units = (elfs.values + goblins.values).sorted()
            for ((index, unit) in units.withIndex()) {
                takeTurn(unit, grid, elfs, goblins)
                if (elfs.isEmpty() || goblins.isEmpty()) {
                    val elfHp = elfs.values.map { it.hp }.sum()
                    val goblinHp = goblins.values.map { it.hp }.sum()
                    println()
                    println(combine(grid, elfs.values + goblins.values))
                    var n = i
                    if (index + 1 == units.size) {
                        println("End of round")
                        n++
                    }
                    println(n)
                    println("${elfs.size}: $elfHp")
                    println("${goblins.size}: $goblinHp")
                    println(n * (elfHp + goblinHp))
                    return
                }
            }
            println()
            println(combine(grid, elfs.values + goblins.values))
            println(i)
        }
    }

    private fun combine(grid: MatrixString, list: List<Unit>): MatrixString {
        val copy = grid.copy()
        for (unit in list) {
            copy.set(unit.location, unit.id)
        }
        return copy
    }

    private fun takeTurn(
        unit: Unit,
        grid: MatrixString,
        elfs: MutableMap<String, Unit>,
        goblins: MutableMap<String, Unit>
    ) {
        // Make sure this unit didn't die before its turn
        if (unit.isDead()) return

        // Each turn:
        // - Find targets
        val targets = getTargets(unit, elfs, goblins)
        var targetsInRange = getInRange(unit, targets)

        // - No target closes?
        if (targetsInRange.isEmpty()) {
            //   - Find reachable targets
            //   - Pick closest (on tie: first in reading order)
            val path = findClosestTarget(unit, targets, grid, elfs, goblins)

            //   - Take step (on tie: first in reading order)
            if (path.isNotEmpty()) {
                unit.location = path.first()
            }

            // Check if there are now any targets in range
            targetsInRange = getInRange(unit, targets)
        }

        // - When target in reach
        if (targetsInRange.isNotEmpty()) {
            //   - Pick one with least health (on tie: first in reading order)
            val target = pickWeakestTarget(targetsInRange)
            //   - Attack
            attack(target)
            //   - Remove if target dies
            if (target.isDead()) {
                elfs.remove(target.id)
                goblins.remove(target.id)
            }
        }
    }

    private fun findClosestTarget(
        unit: Unit,
        targets: List<Unit>,
        grid: MatrixString,
        elfs: MutableMap<String, Unit>,
        goblins: MutableMap<String, Unit>
    ): List<Point> {
        val seen: MutableSet<Point> = mutableSetOf(unit.location)
        val paths: ArrayDeque<List<Point>> = ArrayDeque()
        val copyGrid = grid.copy()
        for (u in elfs.values + goblins.values - unit) {
            copyGrid.set(u.location, "#")
            seen.add(u.location)
        }

        // Look for 1 values surrounding, going from top, left, right, bottom. First is winner.
        val startNeighbours = listOfNotNull(
            grid.getUp(unit.location),
            grid.getLeft(unit.location),
            grid.getRight(unit.location),
            grid.getDown(unit.location)
        )
        for (neighbour in startNeighbours) {
            if (grid.get(neighbour) == ".") {
                paths.add(listOf(neighbour))
            }
        }

        while (paths.isNotEmpty()) {
            val path = paths.removeFirst()
            val pathEnd: Point = path.last()

            // If this is one of our destinations, return it
            if (pathEnd in targets.map { it.location }) {
                return path
            }

            if (pathEnd !in seen) {
                seen.add(pathEnd)

                val neighbours = listOfNotNull(
                    grid.getUp(pathEnd),
                    grid.getLeft(pathEnd),
                    grid.getRight(pathEnd),
                    grid.getDown(pathEnd)
                )
                for (neighbour in neighbours) {
                    if (grid.get(neighbour) == ".") {
                        paths.add(path + neighbour)
                    }
                }
            }
        }
        return emptyList()
    }

    private fun getTargets(
        unit: Unit,
        elfs: MutableMap<String, Unit>,
        goblins: MutableMap<String, Unit>
    ): List<Unit> {
        return if (unit.isElf()) goblins.values.toList() else elfs.values.toList()
    }

    private fun getInRange(unit: Unit, targets: List<Unit>): List<Unit> {
        return targets.filter { it.location.distance(unit.location) == 1 }
    }

//    private fun pickClosestTarget(
//        unit: Unit,
//        targetsInRange: List<Unit>,
//        grid: MatrixString,
//        elfs: MutableMap<String, Unit>,
//        goblins: MutableMap<String, Unit>
//    ): Pair<Unit, List<Point>> {
//        val options = mutableMapOf<Int, MutableList<Pair<Unit, List<Point>>>>()
//        for (target in targetsInRange.sortedBy { it.location.distance(unit.location) }) {
//            val distanceMatrix = MatrixInt.buildForShortestPath(grid, "#")
//            // Add all units, except the unit and the target
//            for (u in elfs.values + goblins.values - unit - target) {
//                distanceMatrix.set(u.location, Int.MIN_VALUE)
//            }
//            val minDistance = options.keys.minOrNull() ?: Int.MAX_VALUE
//            val distance = distanceMatrix.shortestPath(unit.location, target.location, maxDistance = minDistance)
//            val list = options.computeIfAbsent(distance.size) { mutableListOf() }
//            list.add(target to distance)
//        }
//
//        // Get closest targets
//        val closest = options.minByOrNull { it.key }!!.value
//        // On tie: first in reading order
//        return closest.minByOrNull { it.first }!!
//    }

//    private fun takeStep(
//        unit: Unit,
//        target: Pair<Unit, List<Point>>,
//        grid: MatrixString,
//        elfs: MutableMap<String, Unit>,
//        goblins: MutableMap<String, Unit>
//    ): Unit {
//        val distanceMatrix = MatrixInt.buildForShortestPath(grid, "#")
//        // Add all units, except the unit and the target
//        for (u in elfs.values + goblins.values - unit - target) {
//            distanceMatrix.set(u.location, Int.MIN_VALUE)
//        }
//        distanceMatrix.shortestPath(target.location, unit.location)
//
//        // Look for 1 values surrounding, going from top, left, right, bottom. First is winner.
//        val neighbours = listOf(
//            grid.getUp(unit.location),
//            grid.getLeft(unit.location),
//            grid.getRight(unit.location),
//            grid.getDown(unit.location)
//        )
//
//        var bestValue = Int.MAX_VALUE
//        var bestNeighbour: Point? = null
//        for (neighbour in neighbours.filterNotNull()) {
//            val value = distanceMatrix.get(neighbour)
//            if (value in 0 until bestValue) {
//                bestValue = value
//                bestNeighbour = neighbour
//            }
//        }
//        if (bestNeighbour != null) {
//            unit.location = bestNeighbour
//        }
//    }

    private fun pickWeakestTarget(targetsInRange: List<Unit>): Unit {
        var target: Unit? = null
        for (option in targetsInRange.sorted()) {
            if (target == null || option.hp < target.hp) {
                target = option
            }
        }
        return target!!
    }

    private fun attack(target: Unit) {
        target.hp -= 3
    }

    override fun solve2(lines: List<String>) {
    }
}

data class Unit(val id: String, var hp: Int, var location: Point) : Comparable<Unit> {
    fun isElf(): Boolean {
        return id.lowercase() == id
    }

    fun isDead(): Boolean {
        return hp < 0
    }

    override fun compareTo(other: Unit): Int {
        val yCompare = location.y.compareTo(other.location.y)
        if (yCompare != 0) return yCompare
        return location.x.compareTo(other.location.x)
    }
}
