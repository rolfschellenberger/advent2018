package com.rolf.day03

import com.rolf.Day
import com.rolf.util.MatrixInt
import com.rolf.util.splitLine

fun main() {
    Day03().run()
}

class Day03 : Day() {
    override fun solve1(lines: List<String>) {
        val claims = lines.map { parseClaim(it) }

        val grid = MatrixInt.buildDefault(2000, 2000, 0)
        for (claim in claims) {
            for (y in claim.y until claim.y + claim.height) {
                for (x in claim.x until claim.x + claim.width) {
                    grid.set(x, y, grid.get(x, y) + 1)
                }
            }
        }
        println(grid.allElements().count { it > 1 })
    }

    override fun solve2(lines: List<String>) {
        val claims = lines.map { parseClaim(it) }

        val grid = MatrixInt.buildDefault(2000, 2000, 0)
        for (claim in claims) {
            for (y in claim.y until claim.y + claim.height) {
                for (x in claim.x until claim.x + claim.width) {
                    grid.set(x, y, grid.get(x, y) + 1)
                }
            }
        }

        for (claim in claims) {
            var sum = 0
            for (y in claim.y until claim.y + claim.height) {
                for (x in claim.x until claim.x + claim.width) {
                    sum += grid.get(x, y)
                }
            }
            if (sum == claim.size) {
                println(claim.id)
                break
            }
        }
    }

    private fun parseClaim(line: String): Claim {
        val (idstr, _, coord, wh) = splitLine(line, " ")
        val id = idstr.removePrefix("#").toInt()
        val (x, y) = coord.removeSuffix(":").split(",").map { it.toInt() }
        val (width, height) = wh.split("x").map { it.toInt() }
        return Claim(id, x, y, width, height)
    }
}

data class Claim(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int) {
    val size = width * height
}
