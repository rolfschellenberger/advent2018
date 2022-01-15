package com.rolf.day14

import com.rolf.Day

fun main() {
    Day14().run()
}

class Day14 : Day() {
    override fun solve1(lines: List<String>) {
        val recipes = lines.first().toInt()
        val list = ArrayDeque(listOf(3, 7))
        var elf1 = 0
        var elf2 = 1

        while (list.size < recipes + 10) {
            val result = step(list, listOf(elf1, elf2))
            elf1 = result[0]
            elf2 = result[1]
        }
        println(list.subList(recipes, recipes + 10).joinToString(""))
    }

    override fun solve2(lines: List<String>) {
        val recipes = lines.first().toCharArray().map { it.toString().toInt() }
        val list = mutableListOf(3, 7)
        var elf1 = 0
        var elf2 = 1

        while (true) {
            val result = step(list, listOf(elf1, elf2))
            elf1 = result[0]
            elf2 = result[1]
            if (list.size >= recipes.size + 1) {
                val from = list.size - recipes.size
                val to = list.size
                val one = list.slice(from - 1 until to - 1)
                val two = list.slice(from until to)
                if (one == recipes) {
                    println(from - 1)
                    break
                }
                if (two == recipes) {
                    println(from)
                    break
                }
            }
        }
    }

    private fun step(list: MutableList<Int>, pointers: List<Int>): List<Int> {
        val newValue = pointers.map { list[it] }.sum()
        for (char in newValue.toString().toList()) {
            list.add(char.toString().toInt())
        }
        return pointers.map { (it + 1 + list[it]) % list.size }
    }
}
