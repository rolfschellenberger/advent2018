package com.rolf.day23

import com.rolf.Day
import com.rolf.util.EdgeType
import com.rolf.util.Graph
import com.rolf.util.Location
import com.rolf.util.Vertex

fun main() {
    Day23().run()
}

class Day23 : Day() {
    override fun solve1(lines: List<String>) {
        val bots = lines.map { parseBot(it) }
        val maxRadiusBot = bots.maxByOrNull { it.radius }!!
        println(bots.filter { it.location.distance(maxRadiusBot.location) <= maxRadiusBot.radius }.count())
    }

    override fun solve2(lines: List<String>) {
        val bots = lines.map { parseBot(it) }
        val graph = Graph<Bot>()
        for (bot in bots) {
            graph.addVertex(Vertex(bot.location.toString(), bot))
        }
        for (bot in bots) {
            for (other in bots) {
                if (bot != other && bot.inRange(other)) {
                    graph.addEdge(bot.location.toString(), other.location.toString(), EdgeType.UNDIRECTED)
                }
            }
        }
        // There  is only 1 largest group, so taking a short cut here
        val clique = graph.largestCliques().first().map { graph.getVertex(it)!! }
        val origin = Location(0, 0, 0)
        // Calculate the (minimum) distance from origin to each bot it's range (radius).
        val distancesToAllBotsFromOrigin = clique.map { it.data!!.location.distance(origin) - it.data.radius }
        // Next would be to get the maximum value of these distances, because this is the location where we are on the
        // border of the bot furthers away from the origin. This is the location where all meet.
        println(distancesToAllBotsFromOrigin.maxOrNull())
    }

    private fun parseBot(line: String): Bot {
        // pos=<0,0,0>, r=4
        val (pos, r) = line.split(">, r=")
        val (x, y, z) = pos.removePrefix("pos=<")
            .split(",")
            .map { it.toInt() }
        return Bot(Location(x, y, z), r.toInt())
    }
}

data class Bot(val location: Location, val radius: Int) {
    fun inRange(other: Bot): Boolean {
        return location.distance(other.location) <= radius + other.radius
    }
}
