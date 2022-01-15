package com.rolf.day13

import com.rolf.Day
import com.rolf.util.Direction
import com.rolf.util.MatrixString
import com.rolf.util.Point
import com.rolf.util.splitLines

fun main() {
    Day13().run()
}

class Day13 : Day() {
    override fun solve1(lines: List<String>) {
        val grid = buildGrid(lines)
        val carts = findCarts(grid)
        val crashes = mutableListOf<Point>()
        while (crashes.isEmpty()) {
            crashes.addAll(tick(grid, carts))
        }
        println("${crashes.first().x},${crashes.first().y}")
    }

    override fun solve2(lines: List<String>) {
        val grid = buildGrid(lines)
        val carts = findCarts(grid).toMutableList()
        while (carts.size > 1) {
            val crashes = tick(grid, carts).toSet()
            val crashedCarts = carts.filter { crashes.contains(it.location) }
            carts.removeAll(crashedCarts)
        }
        println("${carts.first().location.x},${carts.first().location.y}")
    }

    private fun buildGrid(lines: List<String>): MatrixString {
        val maxWidth = lines.map { it.length }.maxOrNull()!!
        val input = lines.map { it.padEnd(maxWidth, ' ') }
        return MatrixString.build(splitLines(input))
    }

    private fun findCarts(grid: MatrixString): List<Cart> {
        val carts: MutableList<Cart> = mutableListOf()
        for (point in grid.allPoints()) {
            when (grid.get(point)) {
                ">" -> {
                    carts.add(Cart(point, Direction.EAST, Turn.LEFT))
                }
                "<" -> {
                    carts.add(Cart(point, Direction.WEST, Turn.LEFT))
                }
                "^" -> {
                    carts.add(Cart(point, Direction.NORTH, Turn.LEFT))
                }
                "v" -> {
                    carts.add(Cart(point, Direction.SOUTH, Turn.LEFT))
                }
            }
        }
        return carts
    }

    private fun tick(grid: MatrixString, carts: List<Cart>): List<Point> {
        val crashes = mutableListOf<Point>()
        for (cart in carts.sorted()) {
            if (!crashes.contains(cart.location)) {
                cart.move(grid)
                val crash = detectCrash(carts, cart)
                if (crash != null) crashes.add(crash)
            }
        }
        return crashes
    }

    private fun detectCrash(carts: List<Cart>, cart: Cart): Point? {
        for (c1 in carts) {
            if (c1 != cart && c1.location == cart.location) {
                return c1.location
            }
        }
        return null
    }
}

data class Cart(var location: Point, var direction: Direction, var turn: Turn) : Comparable<Cart> {
    override fun compareTo(other: Cart): Int {
        val yCompare = location.y.compareTo(other.location.y)
        if (yCompare != 0) return yCompare
        return location.x.compareTo(other.location.x)
    }

    fun move(grid: MatrixString) {
        location = when (direction) {
            Direction.NORTH -> grid.getUp(location)
            Direction.EAST -> grid.getRight(location)
            Direction.SOUTH -> grid.getDown(location)
            Direction.WEST -> grid.getLeft(location)
        }!!

        // Should we turn on this new location?
        when (grid.get(location)) {
            "\\" -> {
                direction = when (direction) {
                    Direction.NORTH -> Direction.WEST
                    Direction.EAST -> Direction.SOUTH
                    Direction.SOUTH -> Direction.EAST
                    Direction.WEST -> Direction.NORTH
                }
            }
            "/" -> {
                direction = when (direction) {
                    Direction.NORTH -> Direction.EAST
                    Direction.EAST -> Direction.NORTH
                    Direction.SOUTH -> Direction.WEST
                    Direction.WEST -> Direction.SOUTH
                }
            }
            "+" -> {
                direction = when (turn) {
                    Turn.LEFT -> direction.left()
                    Turn.STRAIGHT -> direction
                    Turn.RIGHT -> direction.right()
                }
                // Go to the next turn state
                turn = turn.next()
            }
        }
    }
}

enum class Turn {
    LEFT,
    STRAIGHT,
    RIGHT;

    fun next(): Turn {
        return when (this) {
            LEFT -> STRAIGHT
            STRAIGHT -> RIGHT
            RIGHT -> LEFT
        }
    }
}
