package com.rolf.day12

import com.rolf.Day
import com.rolf.util.groupLines

fun main() {
    Day12().run()
}

class Day12 : Day() {
    override fun solve1(lines: List<String>) {
        val (state, grow) = iteratePlants(lines, 20)
        println(calculateSum(state, grow))
    }

    override fun solve2(lines: List<String>) {
        val (state, grow) = iteratePlants(lines, 400)

        // At this point, the growth is 0 at the front, but 1 per round at the end.
        // The score increments with 8 every iteration
        val sum = calculateSum(state, grow)
        val remaining = 50000000000 - 400
        println(sum + 8 * remaining)
    }

    private fun iteratePlants(lines: List<String>, iterations: Int): Pair<ArrayDeque<Char>, Int> {
        val groups = groupLines(lines, "")
        val (_, initialState) = groups[0].first().split(": ")
        val rules = groups[1].map { parseRule(it) }.toMap()
        var state = parseState(initialState)

        var grow = grow(state)
        for (i in 0 until iterations) {
            val (newState, growth) = iterate(state, rules)
            state = newState
            grow += growth
        }
        return Pair(state, grow)
    }

    private fun calculateSum(state: ArrayDeque<Char>, grow: Int): Int {
        var sum = 0
        for (i in 0 until state.size) {
            val pos = i - grow
            if (state[i] == '#') sum += pos
        }
        return sum
    }

    private fun iterate(state: ArrayDeque<Char>, rules: Map<List<Char>, Char>): Pair<ArrayDeque<Char>, Int> {
        val growth = grow(state)
        val newState = ArrayDeque(".".repeat(state.size).toList())
        for (i in 0 until state.size - 4) {
            val pointer = i + 2
            val window = state.subList(i, i + 5)
            val replacement = rules[window]
            if (replacement != null) {
                newState[pointer] = replacement
            } else {
                newState[pointer] = '.'
            }
        }
        return newState to growth
    }

    private fun grow(state: ArrayDeque<Char>): Int {
        // There should be enough empty pots at the beginning or end of the state.
        val firstIndex = state.indexOfFirst { it == '#' }
        val lastIndex = state.indexOfLast { it == '#' }
        val frontAdd = 5 - firstIndex
        val backAdd = 5 - (state.size - lastIndex - 1)
        repeat(frontAdd) {
            state.addFirst('.')
        }
        repeat(backAdd) {
            state.addLast('.')
        }
        return maxOf(0, frontAdd)
    }

    private fun parseState(initialState: String): ArrayDeque<Char> {
        return ArrayDeque(initialState.toList())
    }

    private fun parseRule(line: String): Pair<List<Char>, Char> {
        val (from, to) = line.split(" => ")
        return from.toList() to to.first()
    }
}
