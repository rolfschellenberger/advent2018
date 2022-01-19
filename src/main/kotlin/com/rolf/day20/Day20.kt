package com.rolf.day20

import com.rolf.Day
import com.rolf.util.Point
import java.util.*

fun main() {
    Day20().run()
}

class Day20 : Day() {
    override fun solve1(lines: List<String>) {
        val distances = mapRoutes(lines.first())
        println(distances.maxByOrNull { it.value }?.value)
    }

    override fun solve2(lines: List<String>) {
        val distances = mapRoutes(lines.first())
        println(distances.count { it.value >= 1000 })
    }

    private fun mapRoutes(line: String): MutableMap<Point, Int> {
        var current = Point(0, 0)
        val distance = mutableMapOf(current to 0)
        val moves = Stack<Point>()
        val movement = setOf('N', 'E', 'S', 'W')

        // Read until (
        // Read until matching closing )
        // Remember all | seen on the closing level to split this part into N parts and pass all to the next step recursively
        // Every matching | go back to the previous location before the (
        for (char in line) {
            when (char) {
                '(' -> moves.push(current)
                // Close cycle and rewind
                ')' -> current = moves.pop()
                // Rewind back to the previous state
                '|' -> current = moves.peek()
                in movement -> {
                    val nextDistance = distance.getValue(current) + 1
                    current = move(current, char)
                    val knownDistance = distance.getOrDefault(current, Int.MAX_VALUE)
                    distance[current] = minOf(knownDistance, nextDistance)
                }
            }
        }
        return distance
    }

    private fun move(current: Point, direction: Char): Point {
        return when (direction) {
            'N' -> Point(current.x, current.y - 1)
            'E' -> Point(current.x + 1, current.y)
            'S' -> Point(current.x, current.y + 1)
            'W' -> Point(current.x - 1, current.y)
            else -> throw Exception("Unknown direction $direction")
        }
    }
}

data class Node(val instructions: MutableList<Char> = mutableListOf(), val split: MutableList<Node> = mutableListOf())