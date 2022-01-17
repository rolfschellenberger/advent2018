package com.rolf.day16

import com.rolf.Day
import com.rolf.util.groupLines
import com.rolf.util.trim

fun main() {
    Day16().run()
}

class Day16 : Day() {
    val allOpCodes = listOf(
        "addr", "addi",
        "mulr", "muli",
        "banr", "bani",
        "borr", "bori",
        "setr", "seti",
        "gtir", "gtri", "gtrr",
        "eqir", "eqri", "eqrr"
    )

    override fun solve1(lines: List<String>) {
        val (examples, _) = group(lines)

        var sum = 0
        for (example in groupLines(examples, "")) {
            val options = findMatches(example)
            if (options.size >= 3) {
                sum++
            }
        }
        println(sum)
    }

    override fun solve2(lines: List<String>) {
        val (examples, testProgram) = group(lines)
        val toTest = groupLines(examples, "")

        val codeToOpCode = mutableMapOf<Int, String>()
        while (codeToOpCode.size < allOpCodes.size) {
            val opCodeToCode = mutableMapOf<Int, MutableSet<String>>()
            for (example in toTest) {
                val options = findMatches(example)

                // Store the possible opcode matches
                for (option in options) {
                    val list = opCodeToCode.computeIfAbsent(option.value) { mutableSetOf() }
                    list.add(option.key)
                }
            }

            // Remove known codes
            codeToOpCode.keys.forEach { opCodeToCode.remove(it) }
            opCodeToCode.forEach { it.value.removeAll(codeToOpCode.values) }

            // Single matches left?
            for ((key, value) in opCodeToCode.filter { it.value.size == 1 }) {
                codeToOpCode[key] = value.first()
            }

            // Unique matches left?
            val opCounts = opCodeToCode.values.flatten().groupingBy { it }.eachCount()
            for (unique in opCounts.filter { it.value == 1 }) {
                val operand = unique.key
                for ((key, value) in opCodeToCode) {
                    if (value.contains(operand)) {
                        codeToOpCode[key] = operand
                    }
                }
            }

            // One last unknown code left? 14?
            val diffId = (0..15).toSet() - codeToOpCode.keys
            val diffOp = allOpCodes - codeToOpCode.values
            if (diffId.size == 1 && diffOp.size == 1) {
                codeToOpCode[diffId.first()] = diffOp.first()
            }
        }

        println(codeToOpCode)
        runProgram(testProgram, codeToOpCode)
    }

    private fun runProgram(testProgram: List<String>, codeToOpCode: MutableMap<Int, String>) {
        var registers = IntArray(4) { 0 }
        println(testProgram.size)
        for (instruction in testProgram) {
            val parts = instruction.split(" ").map { it.toInt() }
            registers = execute(codeToOpCode[parts[0]]!!, parts, registers)
//            println(registers.toList())
        }
        println(registers.toList())
        println(registers[0])
    }

    private fun group(lines: List<String>): Pair<List<String>, List<String>> {
        var first = true
        var blankLineCount = 0
        val a = mutableListOf<String>()
        val b = mutableListOf<String>()
        for (line in lines) {
            if (line == "") {
                blankLineCount++
                if (blankLineCount > 2) {
                    first = false
                }
            } else {
                blankLineCount = 0
            }
            if (first) {
                a.add(line)
            } else {
                b.add(line)
            }
        }
        return trim(a) to trim(b)
    }

    private fun findMatches(example: List<String>): Map<String, Int> {
        val before = stateToArray(example[0])
        val instruction = example[1]
        val after = stateToArray(example[2])

        val options = mutableMapOf<String, Int>()
        for (opCode in allOpCodes) {
            val parts = instruction.split(" ").map { it.toInt() }
            val result = execute(opCode, parts, before)
            if (result.contentEquals(after)) options[opCode] = parts[0]
        }
        return options
    }

    private fun stateToArray(input: String): IntArray {
        return input.split("[")[1]
            .removeSuffix("]")
            .split(", ")
            .map { it.toInt() }
            .toIntArray()
    }

    private fun execute(opcodeOverride: String, parts: List<Int>, register: IntArray): IntArray {
        val result = register.clone()
        when (opcodeOverride) {
            "addr" -> result[parts[3]] = result[parts[1]] + result[parts[2]]
            "addi" -> result[parts[3]] = result[parts[1]] + parts[2]
            "mulr" -> result[parts[3]] = result[parts[1]] * result[parts[2]]
            "muli" -> result[parts[3]] = result[parts[1]] * parts[2]
            "banr" -> result[parts[3]] = result[parts[1]].and(result[parts[2]])
            "bani" -> result[parts[3]] = result[parts[1]].and(parts[2])
            "borr" -> result[parts[3]] = result[parts[1]].or(result[parts[2]])
            "bori" -> result[parts[3]] = result[parts[1]].or(parts[2])
            "setr" -> result[parts[3]] = result[parts[1]]
            "seti" -> result[parts[3]] = parts[1]
            "gtir" -> result[parts[3]] = if (parts[1] > result[parts[2]]) 1 else 0
            "gtri" -> result[parts[3]] = if (result[parts[1]] > parts[2]) 1 else 0
            "gtrr" -> result[parts[3]] = if (result[parts[1]] > result[parts[2]]) 1 else 0
            "eqir" -> result[parts[3]] = if (parts[1] == result[parts[2]]) 1 else 0
            "eqri" -> result[parts[3]] = if (result[parts[1]] == parts[2]) 1 else 0
            "eqrr" -> result[parts[3]] = if (result[parts[1]] > result[parts[2]]) 1 else 0
            else -> throw Exception("Unknown opcode $opcodeOverride")
        }
        return result
    }
}
