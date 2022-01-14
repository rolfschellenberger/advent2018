package com.rolf.day09

import com.rolf.Day
import com.rolf.util.shift
import com.rolf.util.splitLine

fun main() {
    Day09().run()
}

class Day09 : Day() {
    override fun solve1(lines: List<String>) {
        val (playerCount, marbles) = parsePlayerAndMarbles(lines)

        val players = playGame(playerCount, marbles)
        println(players.maxOrNull())
    }

    override fun solve2(lines: List<String>) {
        val (playerCount, marbles) = parsePlayerAndMarbles(lines)

        val players = playGame(playerCount, marbles * 100)
        println(players.maxOrNull())
    }

    private fun parsePlayerAndMarbles(lines: List<String>): Pair<Int, Int> {
        val parts = splitLine(lines.first(), pattern = "\\D+".toPattern())
        val playerCount = parts[0].toInt()
        val marbles = parts[1].toInt()
        return Pair(playerCount, marbles)
    }

    private fun playGame(playerCount: Int, marbles: Int): LongArray {
        val players = LongArray(playerCount) { 0 }
        val list = ArrayDeque<Int>().also { it.add(0) }
        for (move in 1..marbles) {
            if (move % 23 == 0) {
                list.shift(7)
                val score = move + list.removeLast() + 0L
                val player = (move - 1) % players.size
                players[player] += score
                list.shift(-1)
            } else {
                list.shift(-1)
                list.add(move)
            }
        }
        return players
    }
}
