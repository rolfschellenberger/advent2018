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
        val grid = MatrixString.build(splitLines(lines))
        val elves = getElves(grid).toMutableList()
        val goblins = getGoblins(grid).toMutableList()
        clearFromGrid(grid, elves + goblins)
        val score = takeTurns(grid, elves, goblins, 3, 5000)
        println(score)
    }

    override fun solve2(lines: List<String>) {
        val grid = MatrixString.build(splitLines(lines))
        val elves = getElves(grid)
        val goblins = getGoblins(grid)
        clearFromGrid(grid, elves + goblins)

        for (attackPower in 4 until 5000) {
            val newElves = elves.map { it.copy() }.toMutableList()
            val newGoblins = goblins.map { it.copy() }.toMutableList()
            val score = takeTurns(grid, newElves, newGoblins, attackPower, 5000)
            if (newElves.size == elves.size) {
                println(score)
                return
            }
        }
    }

    private fun getElves(grid: MatrixString): List<Unit> {
        val result = mutableListOf<Unit>()
        for (point in grid.allPoints()) {
            when (grid.get(point)) {
                "E" -> {
                    val e = ('a' + result.size).toString()
                    result.add(Unit(e, 200, point))
                }
            }
        }
        return result
    }

    private fun getGoblins(grid: MatrixString): List<Unit> {
        val result = mutableListOf<Unit>()
        for (point in grid.allPoints()) {
            when (grid.get(point)) {
                "G" -> {
                    val g = ('A' + result.size).toString()
                    result.add(Unit(g, 200, point))
                }
            }
        }
        return result
    }

    private fun clearFromGrid(grid: MatrixString, units: List<Unit>) {
        for (unit in units) {
            grid.set(unit.location, ".")
        }
    }

    private fun takeTurns(
        grid: MatrixString,
        elves: MutableList<Unit>,
        goblins: MutableList<Unit>,
        attackPower: Int,
        turns: Int
    ): Int {
        for (i in 0 until turns) {
            val units = (elves + goblins).sorted()
            for ((index, unit) in units.withIndex()) {
                takeTurn(unit, grid, elves, goblins, attackPower)
                if (elves.isEmpty() || goblins.isEmpty()) {
                    val elfHp = elves.map { it.hp }.sum()
                    val goblinHp = goblins.map { it.hp }.sum()
                    var n = i
                    if (index + 1 == units.size) {
                        // Exactly at the end of the roundm add 1 more iteration
                        n++
                    }
                    return n * (elfHp + goblinHp)
                }
            }
        }
        return 0
    }

    private fun takeTurn(
        unit: Unit,
        grid: MatrixString,
        elves: MutableList<Unit>,
        goblins: MutableList<Unit>,
        attackPower: Int
    ) {
        // Make sure this unit didn't die before its turn
        if (unit.isDead()) return

        // Each turn:
        // - Find targets
        val targets = getTargets(unit, elves, goblins)
        var targetsInRange = getInRange(unit, targets)

        // - No target closes?
        if (targetsInRange.isEmpty()) {
            //   - Find reachable targets
            //   - Pick closest (on tie: first in reading order)
            val path = findClosestTarget(unit, targets, grid, elves, goblins)

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
            attack(target, attackPower)
            //   - Remove if target dies
            if (target.isDead()) {
                elves.remove(target)
                goblins.remove(target)
            }
        }
    }

    private fun findClosestTarget(
        unit: Unit,
        targets: List<Unit>,
        grid: MatrixString,
        elves: MutableList<Unit>,
        goblins: MutableList<Unit>
    ): List<Point> {
//        val notAllowed =
//        grid.findPath(unit.location, targets.map { it.location }, )

        val seen: MutableSet<Point> = mutableSetOf(unit.location)
        val paths: ArrayDeque<List<Point>> = ArrayDeque()
        val copyGrid = grid.copy()
        for (u in elves + goblins - unit) {
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

    private fun getTargets(unit: Unit, elves: MutableList<Unit>, goblins: MutableList<Unit>): List<Unit> {
        return if (unit.isElf()) goblins else elves
    }

    private fun getInRange(unit: Unit, targets: List<Unit>): List<Unit> {
        return targets.filter { it.location.distance(unit.location) == 1 }
    }

    private fun pickWeakestTarget(targetsInRange: List<Unit>): Unit {
        var target: Unit? = null
        for (option in targetsInRange.sorted()) {
            if (target == null || option.hp < target.hp) {
                target = option
            }
        }
        return target!!
    }

    private fun attack(target: Unit, attackPower: Int) {
        // Goblins hit elves with 3
        if (target.isElf()) {
            target.hp -= 3
        } else {
            target.hp -= attackPower
        }
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Unit

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
