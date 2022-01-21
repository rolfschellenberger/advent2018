package com.rolf.day24

import com.rolf.Day
import com.rolf.util.groupLines

fun main() {
    Day24().run()
}

class Day24 : Day() {
    override fun solve1(lines: List<String>) {
        val (group1, group2) = groupLines(lines, "")
        val system1 = parseSystem(group1)
        val system2 = parseSystem(group2)
//        val system1 = listOf(
//            Group(17, 5390, emptySet(), setOf("radiation", "bludgeoning"), "fire", 4507, 2),
//            Group(989, 1274, setOf("fire"), setOf("bludgeoning", "slashing"), "slashing", 25, 3)
//        )
//        val system2 = listOf(
//            Group(801, 4706, emptySet(), setOf("radiation"), "bludgeoning", 116, 1),
//            Group(4485, 2961, setOf("radiation"), setOf("fire", "cold"), "slashing", 12, 4)
//        )

        while (system1.any { it.canAttack() } && system2.any { it.canAttack() }) {
            // Target selection
            val targets1: Map<Group, Group> = selectTargets(system1, system2)
            val targets2: Map<Group, Group> = selectTargets(system2, system1)

            // Attack
            for (group in (targets1 + targets2).toSortedMap()) {
                val attacker = group.key
                val defender = group.value
                val damage = attacker.calculateDamageTo(defender)
                val before = defender.units
                defender.takeDamage(damage)
            }
        }

        println((system1 + system2).map { it.units }.sum())
    }

    private fun parseSystem(lines: List<String>): List<Group> {
        return lines.subList(1, lines.size).map { parseGroup(it) }
    }

    private fun parseGroup(line: String): Group {
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
        val attackPoints = parts[12].toInt()
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
                    .thenBy { it.initiative }
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
                    .thenBy { it.initiative }
            )
            ?.firstOrNull()
    }

    override fun solve2(lines: List<String>) {
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
