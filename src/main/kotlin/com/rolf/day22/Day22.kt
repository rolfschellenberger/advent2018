package com.rolf.day22

import com.rolf.Day
import com.rolf.util.MatrixInt
import com.rolf.util.Point
import java.util.*

fun main() {
    Day22().run()
}

class Day22 : Day() {
    override fun solve1(lines: List<String>) {
        val depth = lines[0].split(" ")[1].toInt()
        val (x, y) = lines[1].split(" ")[1].split(",").map { it.toInt() }
        val target = Point(x, y)

        val grid = buildErosionGrid(target, depth)
        val type = buildTypeGrid(grid)
        println(type.allElements().sum())
    }

    override fun solve2(lines: List<String>) {
        val depth = lines[0].split(" ")[1].toInt()
        val (x, y) = lines[1].split(" ")[1].split(",").map { it.toInt() }
        val target = Point(x, y)

        val grid = buildErosionGrid(target, depth, 10)
        val type = buildTypeGrid(grid)

//        val queue = mutableListOf<LocationState>()
        val queue = PriorityQueue<LocationState>()
        queue.add(LocationState(Point(0, 0), 0, Tool.TORCH))
//        val distances = mutableMapOf<Point, Int>()
        val distances = mutableMapOf<LocationState, Int>()
        while (queue.isNotEmpty()) {
//            println(queue.size)
            val element = queue.remove()
//            val key = element.point
            if (element.point == Point(0,1) && element.distance == 1) {
                println(element)
            }
            if (element.point == Point(1,1) && element.distance == 2) {
                println(element)
            }
            if (element.point == Point(2,1) && element.distance == 10) {
                println(element)
            }
            if (element.point == Point(3,1) && element.distance == 11) {
                println(element)
            }
            if (element.point == Point(4,1) && element.distance == 12) {
                println(element)
            }
            if (element.point == Point(4,2) && element.distance == 20) {
                println(element)
            }
            if (element.point == Point(4,3) && element.distance == 21) {
                println(element)
            }
            if (element.point == Point(4,4) && element.distance == 22) {
                println(element)
            }
            if (element.point == Point(4,5) && element.distance == 23) {
                println(element)
            }
            if (element.point == Point(5,9) && element.distance == 28 && element.tool == Tool.CLIMB) {
                println(element)
            }
            val key = element.copy(distance = 0)
            val latestDistance = distances.getOrDefault(key, Int.MAX_VALUE)
            if (element.distance < latestDistance) {
                distances[key] = element.distance

                for (neighbour in type.getNeighbours(element.point, diagonal = false)) {
                    val newState = getStates(type, element, neighbour, target)
                    queue.add(newState)
                }
            }
        }
//        val di = MatrixInt.buildDefault(type.width(), type.height(), 0)
//        distances.forEach { di.set(it.key, it.value) }
//        println(di.toString(" ", "\n"))
        println(distances[LocationState(target, 0, Tool.TORCH)])
//        println(distances[target])
        // 1104 too high
        // 1041 too high
        // 1034 too high
        // 1023
    }

    private fun getStates(
        grid: MatrixInt,
        element: LocationState,
        nextLocation: Point,
        target: Point
    ): LocationState {
        // In rocky regions, you can use the climbing gear or the torch. You cannot use neither
        // (you'll likely slip and fall).
        // In wet regions, you can use the climbing gear or neither tool. You cannot use the torch
        // (if it gets wet, you won't have a light source).
        // In narrow regions, you can use the torch or neither tool. You cannot use the climbing gear
        // (it's too bulky to fit).
        // tool      from             to            new tool
        // climb     rock             rock          climb
        // climb     rock             wet           climb
        // climb     rock             narrow        torch
        // torch     rock             rock          torch
        // torch     rock             wet           climb
        // torch     rock             narrow        torch
        // ...
        val from = grid.get(element.point)
        val to = grid.get(nextLocation)
        val fromTools = getTools(from)
        val toTools = getTools(to)
        val toolOptions = fromTools.intersect(toTools)
        val newTool = if (toolOptions.contains(element.tool)) element.tool else toolOptions.first()
        val switchTime = if (newTool == element.tool) 0 else 7

        var targetTime = 0
        if (nextLocation == target) {
            // Finally, once you reach the target, you need the torch equipped before you can find him in the dark.
            // The target is always in a rocky region, so if you arrive there with climbing gear equipped, you will
            // need to spend seven minutes switching to your torch.
            if (element.tool != Tool.TORCH) {
                targetTime = 7
            }
        }
        return LocationState(nextLocation, element.distance + 1 + switchTime + targetTime, newTool)
    }

    private fun getTools(type: Int): Set<Tool> {
        // 0 = rock, 1 = wet, 2 = narrow
        return when (type) {
            0 -> Tool.ROCK_TOOLS
            1 -> Tool.WET_TOOLS
            2 -> Tool.NARROW_TOOLS
            else -> throw Exception("Unknown type: $type")
        }
    }

    private fun buildErosionGrid(target: Point, depth: Int, extend: Int = 0): MatrixInt {
        val grid = MatrixInt.buildDefault(target.x + 1 + extend, target.y + 1 + extend, 0)
        for (point in grid.allPoints()) {
            val erosionLevel = erosion(grid, point, depth, target)
            grid.set(point, erosionLevel)
        }
        return grid
    }

    private fun erosion(grid: MatrixInt, point: Point, depth: Int, target: Point): Int {
        val geologicIndex = geologicIndex(grid, point, target)
        return (geologicIndex + depth) % 20183
    }

    private fun geologicIndex(grid: MatrixInt, point: Point, target: Point): Int {
        if (point.x == 0 && point.y == 0) return 0
        if (point == target) return 0
        if (point.y == 0) {
            return point.x * 16807
        }
        if (point.x == 0) {
            return point.y * 48271
        }
        val left = grid.getLeft(point)!!
        val up = grid.getUp(point)!!
        return grid.get(left) * grid.get(up)
    }

    private fun buildTypeGrid(grid: MatrixInt): MatrixInt {
        val erosion = grid.copy()
        for (point in erosion.allPoints()) {
            erosion.set(point, erosion.get(point) % 3)
        }
        return erosion
    }
}

data class LocationState(val point: Point, val distance: Int, val tool: Tool) : Comparable<LocationState> {
    override fun compareTo(other: LocationState): Int {
        return distance.compareTo(other.distance)
    }
}

enum class Tool {
    TORCH,
    CLIMB,
    NOTHING;

    companion object {
        val ROCK_TOOLS = setOf(CLIMB, TORCH)
        val WET_TOOLS = setOf(CLIMB, NOTHING)
        val NARROW_TOOLS = setOf(TORCH, NOTHING)
    }
}
