package com.rolf.day21

import com.rolf.Day

fun main() {
    Day21().run()
}

class Day21 : Day() {
    override fun solve1(lines: List<String>) {
        val registers = IntArray(6) { 0 }
        // eqrr 5 0 3
        val values = runProgram(lines, registers, 28, 5)
        println(values.first())
    }

    override fun solve2(lines: List<String>) {
        val registers = IntArray(6) { 0 }
        // eqrr 5 0 3
        val values = runProgram(lines, registers, 28, 5, Int.MAX_VALUE)
        println(values.last())
    }

    private fun runProgram(
        lines: List<String>,
        registers: IntArray,
        instructionIndex: Int,
        registerIndex: Int,
        maxResults: Int = 1
    ): List<Int> {
        val ipRegister = lines.first().split(" ")[1].toInt()
        val instructions = lines.subList(1, lines.size).map { parseInstruction(it) }
        val result = mutableListOf<Int>()
        val cache = mutableSetOf<Int>()
        var ip = 0
        while (ip < instructions.size) {
            val instruction = instructions[ip]

            registers[ipRegister] = ip
            execute(instruction, registers)
            ip = registers[ipRegister] + 1

            if (ip == instructionIndex) {
                val value = registers[registerIndex]
                // When we hit a cache result, it means we are in an infinite loop, so return the sequence
                if (!cache.add(value)) {
                    return result
                }
                result.add(value)
                if (result.size > maxResults) {
                    return result
                }
            }
        }
        return result
    }

    private fun parseInstruction(instruction: String): Instruction {
        val parts = instruction.split(" ")
        return Instruction(parts[0], parts.subList(1, parts.size).map { it.toInt() })
    }

    private fun execute(instruction: Instruction, result: IntArray) {
        val parts = instruction.parts
        when (instruction.operation) {
            "addr" -> result[parts[2]] = result[parts[0]] + result[parts[1]]
            "addi" -> result[parts[2]] = result[parts[0]] + parts[1]
            "mulr" -> result[parts[2]] = result[parts[0]] * result[parts[1]]
            "muli" -> result[parts[2]] = result[parts[0]] * parts[1]
            "banr" -> result[parts[2]] = result[parts[0]].and(result[parts[1]])
            "bani" -> result[parts[2]] = result[parts[0]].and(parts[1])
            "borr" -> result[parts[2]] = result[parts[0]].or(result[parts[1]])
            "bori" -> result[parts[2]] = result[parts[0]].or(parts[1])
            "setr" -> result[parts[2]] = result[parts[0]]
            "seti" -> result[parts[2]] = parts[0]
            "gtir" -> result[parts[2]] = if (parts[0] > result[parts[1]]) 1 else 0
            "gtri" -> result[parts[2]] = if (result[parts[0]] > parts[1]) 1 else 0
            "gtrr" -> result[parts[2]] = if (result[parts[0]] > result[parts[1]]) 1 else 0
            "eqir" -> result[parts[2]] = if (parts[0] == result[parts[1]]) 1 else 0
            "eqri" -> result[parts[2]] = if (result[parts[0]] == parts[1]) 1 else 0
            "eqrr" -> result[parts[2]] = if (result[parts[0]] == result[parts[1]]) 1 else 0
            else -> throw Exception("Unknown opcode $instruction")
        }
    }
}

data class Instruction(val operation: String, val parts: List<Int>)
