package com.rolf.day07

import com.rolf.Day
import com.rolf.util.splitLines

fun main() {
    Day07().run()
}

class Day07 : Day() {
    override fun solve1(lines: List<String>) {
        val all = parseNodes(lines)
        val options = all.filter { it.value.dependencies.isEmpty() }.map { it.value.value }.toMutableSet()
        val visited: MutableList<String> = mutableListOf()
        while (options.isNotEmpty()) {
            val option = findFirstOption(all, options, visited)!!
            options.remove(option)
            visited.add(option)
            options.addAll(all[option]!!.next)
        }
        println(visited.joinToString(""))
    }

    override fun solve2(lines: List<String>) {
        val all = parseNodes(lines)
        val options = all.filter { it.value.dependencies.isEmpty() }.map { it.value.value }.toMutableSet()
        val inProgress: MutableMap<String, Int> = mutableMapOf()
        val visited: MutableList<String> = mutableListOf()
        var time = 0
        while ((all.keys - visited).isNotEmpty()) {
            // Process the work in progress
            val done = mutableSetOf<String>()
            for (progress in inProgress) {
                progress.setValue(progress.value - 1)
                if (progress.value == 0) {
                    done.add(progress.key)
                    visited.add(progress.key)
                    options.addAll(all[progress.key]!!.next)
                }
            }
            done.forEach { inProgress.remove(it) }

            // Add new work in progress
            for (i in 0 until 5 - inProgress.size) {
                val option = findFirstOption(all, options, visited)
                if (option != null) {
                    options.remove(option)
                    inProgress[option] = 60 + (option.first() - 'A' + 1)
                }
            }
            time++
        }
        println(time - 1)
    }

    private fun parseNodes(lines: List<String>): MutableMap<String, Node> {
        val input = splitLines(lines, " ")
        val all = mutableMapOf<String, Node>()
        val tos = mutableSetOf<String>()
        for (parts in input) {
            val from = parts[1]
            val to = parts[7]
            tos.add(to)

            val fromNode = all.computeIfAbsent(from) { Node(from) }
            fromNode.next.add(to)
            val toNode = all.computeIfAbsent(to) { Node(to) }
            toNode.dependencies.add(from)
        }
        return all
    }

    private fun findFirstOption(
        all: MutableMap<String, Node>,
        options: MutableSet<String>,
        visited: MutableList<String>
    ): String? {
        for (option in options.sorted()) {
            val node = all[option]!!
            node.dependencies.removeAll(visited)
            if (node.dependencies.isEmpty()) {
                return option
            }
        }
        return null
    }
}

data class Node(val value: String) {
    val dependencies: MutableSet<String> = mutableSetOf()
    val next: MutableSet<String> = mutableSetOf()
}
