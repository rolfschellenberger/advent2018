package com.rolf.day24

import com.rolf.Day
import com.rolf.util.groupLines

fun main() {
    Day24().run()
}

class Day24 : Day() {
    override fun solve1(lines: List<String>) {
        val (system1, system2) = fight(lines)
        println((system1 + system2).map { it.units }.sum())
    }

    override fun solve2(lines: List<String>) {
        var immuneWin = false
        var boost = 1
        var lastSum = 0
        while (!immuneWin) {
            val (system1, system2) = fight(lines, boost)
            lastSum = (system1 + system2).map { it.units }.sum()
            immuneWin = system1.any { !it.isDead() } && system2.all { it.isDead() }
            boost++
        }
        println(lastSum)
    }

    private fun fight(lines: List<String>, boost: Int = 0): Pair<List<Group>, List<Group>> {
        val (group1, group2) = groupLines(lines, "")
        val system1 = parseSystem(group1, boost)
        val system2 = parseSystem(group2)

        while (system1.any { it.canAttack() } && system2.any { it.canAttack() }) {
            // Target selection
            val targets1: Map<Group, Group> = selectTargets(system1, system2)
            val targets2: Map<Group, Group> = selectTargets(system2, system1)

            // Attack
            val unitsBefore = (system1 + system2).map { it.units }.sum()
            for (group in (targets1 + targets2).toSortedMap()) {
                val attacker = group.key
                val defender = group.value
                val damage = attacker.calculateDamageTo(defender)
                defender.takeDamage(damage)
            }
            val unitsAfter = (system1 + system2).map { it.units }.sum()

            // This means the fight never ends
            if (unitsAfter == unitsBefore) {
                return Pair(system1, system2)
            }
        }
        return Pair(system1, system2)
    }

    private fun parseSystem(lines: List<String>, boost: Int = 0): List<Group> {
        return lines.subList(1, lines.size).map { parseGroup(it, boost) }
    }

    private fun parseGroup(line: String, boost: Int): Group {
        // 4081 units each with 8009 hit points (immune to slashing, radiation; weak to bludgeoning, cold) with an attack that does 17 fire damage at initiative 7
        // 2599 units each with 11625 hit points with an attack that does 36 bludgeoning damage at initiative 17
        // 4232 units each with 4848 hit points (weak to slashing) with an attack that does 11 bludgeoning damage at initiative 13
        // 4040 units each with 8260 hit points (immune to cold) with an attack that does 17 bludgeoning damage at initiative 20
        val immune = mutableSetOf<String>()
        val weakness = mutableSetOf<String>()

        val from = line.indexOf("(")
        val to = line.indexOf(")")
        var default = line
        if (from >= 0) {
            val immuneWeak = line.substring(from + 1, to)
            default = default.replace(line.substring(from, to + 2), "")

            val parts = immuneWeak.split("; ")
            for (part in parts) {
                if (part.startsWith("immune")) {
                    immune.addAll(part.removePrefix("immune to ").split(", "))
                }
                if (part.startsWith("weak")) {
                    weakness.addAll(part.removePrefix("weak to ").split(", "))
                }
            }
        }

        // 2599 units each with 11625 hit points with an attack that does 36 bludgeoning damage at initiative 17
        val parts = default.split(" ")
        val units = parts[0].toInt()
        val hitPoints = parts[4].toInt()
        val attackPoints = parts[12].toInt() + boost
        val attackType = parts[13]
        val initiative = parts[17].toInt()

        return Group(units, hitPoints, immune, weakness, attackType, attackPoints, initiative)
    }

    private fun selectTargets(attackingSystem: List<Group>, defendingSystem: List<Group>): Map<Group, Group> {
        val targets: MutableMap<Group, Group> = mutableMapOf()
        for (group in sortForTargetSelection(attackingSystem)) {
            val target = getTarget(group, defendingSystem - targets.values)
            if (target != null) {
                targets[group] = target
            }
        }
        return targets
    }

    private fun sortForTargetSelection(list: List<Group>): List<Group> {
        // In decreasing order of effective power, groups choose their targets
        // In a tie, the group with the higher initiative chooses first.
        return list
            .filter { it.canAttack() }
            .sortedWith(
                compareByDescending<Group> { it.effectivePower }
                    .thenByDescending { it.initiative }
            )
    }

    private fun getTarget(attacker: Group, defenders: List<Group>): Group? {
        return defenders
            .filter { !it.isDead() }
            .groupBy { attacker.calculateDamageTo(it) }
            // If it cannot deal any defending groups damage, it does not choose a target.
            .filter { it.key > 0 }
            .maxByOrNull { it.key }
            ?.value
            ?.sortedWith(
                compareByDescending<Group> { it.effectivePower }
                    .thenByDescending { it.initiative }
            )
            ?.firstOrNull()
    }
}

data class Group(
    var units: Int,
    var hitPoints: Int,
    val immune: Set<String>,
    val weakness: Set<String>,
    val attackType: String,
    val attackPoints: Int,
    val initiative: Int
) : Comparable<Group> {
    val effectivePower: Int get() = units * attackPoints

    fun isDead(): Boolean {
        return units <= 0
    }

    fun canAttack(): Boolean {
        return units > 0
    }

    fun calculateDamageTo(other: Group): Int {
        // If the defending group is immune to the attacking group's attack type, the defending group instead takes no damage.
        if (other.immune.contains(attackType)) {
            return 0
        }
        // If the defending group is weak to the attacking group's attack type, the defending group instead takes double damage.
        if (other.weakness.contains(attackType)) {
            return 2 * effectivePower
        }
        // By default, an attacking group would deal damage equal to its effective power to the defending group.
        return effectivePower
    }

    fun takeDamage(strength: Int) {
        val kill = strength / hitPoints
        units = maxOf(0, units - kill)
    }

    override fun compareTo(other: Group): Int {
        return other.initiative.compareTo(initiative)
    }
}
