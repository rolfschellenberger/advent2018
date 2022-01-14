package com.rolf.day07

import com.rolf.Day
import com.rolf.util.splitLines

fun main() {
    Day07().run()
}

class Day07 : Day() {
    override fun solve1(lines: List<String>) {
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

        val options = (all - tos).keys.toMutableSet()
        val visited: MutableList<String> = mutableListOf()
        while (options.isNotEmpty()) {
            val option = findFirstOption(all, options, visited)!!
            options.remove(option)
            visited.add(option)
            options.addAll(all[option]!!.next)
        }
        println(visited.joinToString(""))
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

    override fun solve2(lines: List<String>) {
        solvePart2(lines)
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

        val workers = mutableListOf<Worker>()
        for (i in 0 until 5) {
            workers.add(Worker(i))
        }
        val inProgress: MutableMap<String, Int> = mutableMapOf()

        val options = (all - tos).keys.toMutableSet()
        val visited: MutableList<String> = mutableListOf()
        var time = 0
        while ((all.keys - visited).isNotEmpty()) {
            for (worker in workers) {
                if (worker.finishedWork()) {
                    visited.add(worker.getWorkInput())
                    options.addAll(all[worker.getWorkInput()]!!.next)

                    val option = findFirstOption(all, options, visited)
                    if (option != null) {
                        options.remove(option)
                        worker.giveWork(option)
                    }
                }
                worker.doWork()
//                if (!worker.hasWork()) {
//                    // When the worker just finished his work, log it
//                    if (worker.finishedWork()) {
//                        visited.add(worker.getWorkInput())
//                        options.addAll(all[worker.getWorkInput()]!!.next)
//                    }
//
//                    val option = findFirstOption(all, options, visited)
//                    if (option != null) {
//                        options.remove(option)
//                        worker.giveWork(option)
//                    }
//                }
            }
            time++
        }
        println(visited.joinToString(""))
        println(time - 1)
        // 1327 too high
        // 1265 too high
        // 1054 wrong
        // 1053
    }

    var allPairs: List<Pair<Char, Char>> = mutableListOf()
    var childrenOf: Map<Char, Set<Char>> = generateDependencies(allPairs)
    var parentsOf: Map<Char, Set<Char>> = generateDependencies(allPairs.map { it.second to it.first })
    var allKeys = childrenOf.keys.union(parentsOf.keys)

    fun solvePart2(input: List<String>) {
        allPairs = parseInput(input)
        childrenOf = generateDependencies(allPairs)
        parentsOf = generateDependencies(allPairs.map { it.second to it.first })
        allKeys = childrenOf.keys.union(parentsOf.keys)

        println(solvePart2(5, ::actualCostFunction))
    }

    private fun parseInput(input: List<String>): List<Pair<Char, Char>> =
        input.map { row ->
            row.split(" ").run { this[1].first() to this[7].first() }
        }

    private fun generateDependencies(input: List<Pair<Char, Char>>): Map<Char, Set<Char>> =
        input
            .groupBy { it.first }
            .mapValues { (_, value) -> value.map { it.second }.toSet() }

    fun solvePart2(workers: Int, costFunction: (Char) -> Int): Int {
        val ready = allKeys.filterNot { it in parentsOf }.map { it to costFunction(it) }.toMutableList()
        val done = mutableListOf<Char>()
        var time = 0

        while (ready.isNotEmpty()) {
            // Work on things that are ready.
            // Do one unit of work per worker, per item at the head of the queue.
            ready.take(workers).forEachIndexed { idx, work ->
                ready[idx] = Pair(work.first, work.second - 1)
            }

            // These are done
            ready.filter { it.second == 0 }.forEach { workItem ->
                done.add(workItem.first)

                // Now that we are done, add some to ready that have become unblocked
                childrenOf[workItem.first]?.let { maybeReadySet ->
                    ready.addAll(
                        maybeReadySet.filter { maybeReady ->
                            parentsOf.getValue(maybeReady).all { it in done }
                        }
                            .map { it to costFunction(it) }
                            .sortedBy { it.first }
                    )
                }
            }

            // Remove anything that we don't need to look at anymore.
            ready.removeIf { it.second == 0 }

            time++
        }
        return time
    }

    companion object {
        fun exampleCostFunction(c: Char): Int = actualCostFunction(c) - 60
        fun actualCostFunction(c: Char): Int = 60 + (c - 'A' + 1)
    }
}

data class Node(val value: String) {
    val dependencies: MutableSet<String> = mutableSetOf()
    val next: MutableSet<String> = mutableSetOf()
}

class Worker(val id: Int) {
    private var work: Int = -1
    private var workInput = ""

    fun hasWork(): Boolean {
        return work > 0
    }

    fun finishedWork(): Boolean {
        return work == 0
    }

    fun giveWork(work: String) {
        val time = 61 + (work.first() - 'A')
        this.work = time
        this.workInput = work
    }

    fun getWorkInput(): String {
        return workInput
    }

    fun doWork() {
        work = maxOf(0, work - 1)
    }
}
