package com.rolf.day05

import com.rolf.Day
import java.util.*

fun main() {
    Day05().run()
}

class Day05 : Day() {
    override fun solve1(lines: List<String>) {
        println(clean(lines.first()).size)
    }

    override fun solve2(lines: List<String>) {
        val line = lines.first()
        val lowerChars = line.map { it.lowercaseChar() }
            .toSet()

        println(lowerChars
            .map { line.replace(it.toString(), "").replace(it.uppercase(), "") }
            .map { clean(it).size }
            .minOrNull())
    }

    private fun clean(line: String): Stack<Char> {
        val stack = Stack<Char>()
        for (char in line) {
            if (stack.isNotEmpty() && isOpposites(stack.peek(), char)) {
                stack.pop()
            } else {
                stack.push(char)
            }
        }
        return stack
    }

    private fun isOpposites(a: Char, b: Char): Boolean {
        return a != b && (a.uppercaseChar() == b || a == b.uppercaseChar())
    }
}
