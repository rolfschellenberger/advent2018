package com.rolf.day08

import com.rolf.Day
import com.rolf.util.splitLine

fun main() {
    Day08().run()
}

class Day08 : Day() {
    override fun solve1(lines: List<String>) {
        val numbers = splitLine(lines.first(), " ").map { it.toInt() }
        val root = parseNode(numbers)
        println(root.sumMetadataRecursive())
    }

    private fun parseNode(numbers: List<Int>, position: Int = 0): Node {
        var pointer = position
        val childrenCount = numbers[pointer++]
        val metadataCount = numbers[pointer++]
        val children: MutableList<Node> = mutableListOf()
        for (c in 0 until childrenCount) {
            val child = parseNode(numbers, pointer)
            children.add(child)
            pointer = child.end
        }
        val metadata: MutableList<Int> = mutableListOf()
        for (i in 0 until metadataCount) {
            metadata.add(numbers[pointer++])
        }
        return Node(position, pointer, children, metadata)
    }

    override fun solve2(lines: List<String>) {
        val numbers = splitLine(lines.first(), " ").map { it.toInt() }
        val root = parseNode(numbers)
        println(root.value())
    }
}

data class Node(val start: Int, val end: Int, val children: List<Node>, val metadata: List<Int>) {
    fun sumMetadataRecursive(): Int {
        return metadata.sum() + children.map { it.sumMetadataRecursive() }.sum()
    }

    fun value(): Int {
        return if (children.isEmpty()) {
            metadata.sum()
        } else {
            var sum = 0
            for (index in metadata) {
                if (index - 1 < children.size) {
                    sum += children[index - 1].value()
                }
            }
            sum
        }
    }
}
