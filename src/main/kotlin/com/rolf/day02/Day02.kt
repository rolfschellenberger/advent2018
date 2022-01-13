package com.rolf.day02

import com.rolf.Day
import com.rolf.util.getCharacterCounts
import com.rolf.util.getCombinations

fun main() {
    Day02().run()
}

class Day02 : Day() {
    override fun solve1(lines: List<String>) {
        var two = 0
        var three = 0
        for (line in lines) {
            val input = line.toCharArray()
            input.sort()
            val counts = getCharacterCounts(String(input))
            if (counts.contains(2)) two++
            if (counts.contains(3)) three++
        }
        println(two * three)
    }

    override fun solve2(lines: List<String>) {

        fun onNextCombination(combination: List<String>) {
            if (combination.size == 2) {
                val equal = mutableListOf<Char>()
                val a = combination[0]
                val b = combination[1]
                for (i in a.indices) {
                    if (a[i] == b[i]) equal.add(a[i])
                }
                if (equal.size + 1 == a.length) {
                    println(equal.joinToString(""))
                }
            }
        }

        fun earlyTermination(combination: List<String>): Boolean {
            return combination.size > 2
        }

        getCombinations(lines, ::onNextCombination, ::earlyTermination)
    }
}
