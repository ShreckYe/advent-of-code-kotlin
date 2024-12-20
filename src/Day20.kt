fun main() {

    fun distancesOfNulls(input: List<String>) =
        input.map { Array<Int?>(it.length) { null } }

    // `-Xss1G` needed TODO why?
    fun bfsSetDistances(
        input: List<String>,
        ps: List<Position>,
        distances: List<Array<Int?>>,
        distance: Int,
        isNewDistance: (Char, Int?, Int) -> Boolean
    ) {
        //println(ps)
        if (ps.isNotEmpty()) {
            val nextPs = ps.flatMap { p ->
                val c = input.getOrNull(p.i)?.getOrNull(p.j)
                if (c !== null && isNewDistance(c, distances[p], distance)) {
                    distances[p] = distance
                    Direction.entries.map { direction -> p + direction.diff }
                } else emptyList()
            }
                .distinct()
            bfsSetDistances(input, nextPs, distances, distance + 1, isNewDistance)
        }
    }

    fun distancesFrom(input: List<String>, p: Position): List<Array<Int?>> {
        val distances = distancesOfNulls(input)
        bfsSetDistances(input, listOf(p), distances, 0) { c, old, _ -> c != '#' && old === null }
        return distances
    }

    fun processDistances(input: List<String>): Triple<List<Array<Int?>>, List<Array<Int?>>, Int> {
        val sPosition = input.positionOf('S')
        val ePosition = input.positionOf('E')

        val distancesFromS = distancesFrom(input, sPosition)
        val distancesFromE = distancesFrom(input, ePosition)
        //println(distancesFromS.joinToString("\n", postfix = "\n") {it.toList().toString()})
        //println(distancesFromE.joinToString("\n", postfix = "\n") { it.toList().toString()})

        val sToEDistance = distancesFromS[ePosition]!!
        return Triple(distancesFromS, distancesFromE, sToEDistance)
    }

    fun part1(input: List<String>, leastNumSecondsSaved: Int): Int {
        val (distancesFromS, distancesFromE, sToEDistance) = processDistances(input)

        val ans = distancesFromS.withIndex().sumOf { (i, line) ->
            line.withIndex().sumOf { (j, distance) ->
                if (distance !== null) {
                    val p = Position(i, j)
                    Direction.entries.count { direction ->
                        val cheatP1 = p + direction.diff
                        val cheatP2 = cheatP1 + direction.diff
                        input.getOrNull(cheatP1.i)?.getOrNull(cheatP1.j) == '#' &&
                                input.getOrNull(cheatP2.i)?.getOrNull(cheatP2.j).isHabitable() &&
                                sToEDistance - (distance + 2 + distancesFromE[cheatP2]!!) >= leastNumSecondsSaved
                    }
                } else 0
            }
        }

        return ans
    }

    fun part2(input: List<String>, leastNumSecondsSaved: Int): Int {
        println(leastNumSecondsSaved)
        val (distancesFromS, distancesFromE, sToEDistance) = processDistances(input)

        val cheatStartMap = input.map { it.toCharArray() }
        val cheatEndMap = input.map { it.toCharArray() }
        val ans = distancesFromS.withIndex().sumOf { (i, line) ->
            line.withIndex().sumOf { (j, sDistance) ->
                if (sDistance !== null) {
                    val p = Position(i, j)

                    // This is incorrect: "start position (the position where the cheat is activated, just before the first move that is allowed to go through walls)"
                    val cheats = Direction.entries.flatMap { direction ->
                        val firstWallP = p + direction.diff
                        if (p == Position(1, 3) && firstWallP == Position(2, 3))
                            println("(1, 3) via (2, 3): $direction $firstWallP ${input.getOrNull(firstWallP)}")
                        if (input.getOrNull(firstWallP) == '#')
                            (0..19).flatMap { diffSum ->
                                (0..diffSum).flatMap { iDiff ->
                                    val jDiff = diffSum - iDiff
                                    listOf(iDiff, -iDiff).flatMap { iDiff ->
                                        listOf(jDiff, -jDiff).mapNotNull { jDiff ->
                                            val end = firstWallP + PositionDiff(iDiff, jDiff)
                                            if (p == Position(1, 3) && end == Position(7, 3))
                                                println("(1, 3) to (7, 3): $direction $firstWallP $diffSum $iDiff $jDiff")
                                            if (input.getOrNull(end.i)?.getOrNull(end.j).isHabitable())
                                                diffSum + 1 to end
                                            else null
                                        }
                                    }
                                }
                            }
                        else
                            emptyList()
                    }
                        .groupBy { it.second }
                        .mapValues { it.value.minBy { it.first } }
                        .values
                        .filter { it.second != p } // This can actually be omitted


                    /*
                    val cheatDistances = distancesOfNulls(input)
                    cheatDistances[p] = 0
                    for (direction in Direction.entries) {
                        val np = p + direction.diff
                        val nc = input.getOrNull(np)
                        if (nc == '#')
                            bfsSetDistances(
                                input,
                                listOf(np),
                                cheatDistances,
                                1
                            ) { _, old, new ->
                                //println("$old $new")
                                new <= 20 && (old === null || new < old)
                            }
                    }
                    println(cheatDistances.intDistancesString())

                    val cheats = cheatDistances.withIndex().flatMap { (i, line) ->
                        // TODO filter out 0?
                        line.withIndex().asSequence().filter { it.value !== null }.mapNotNull { (j, distance) ->
                            val p = Position(i, j)
                            input.getOrNull(p)?.let { distance!! to p }
                        }
                    }
                    */

                    cheats.count { (distance, cheatEndP) ->
                        (sToEDistance - (sDistance + distance + distancesFromE[cheatEndP]!!) >= leastNumSecondsSaved).also {
                            if (it) {
                                if (sDistance + distance + distancesFromE[cheatEndP]!! == 12) {
                                    println("$sToEDistance $sDistance $p $distance $cheatEndP ${distancesFromE[cheatEndP]!!} ${sDistance + distance + distancesFromE[cheatEndP]!!}")

                                    printMap(input.map { it.toCharArray() }.also {
                                        it[p] = '0'
                                        it[cheatEndP] = distance.coerceAtMost(9).digitToChar()
                                    })
                                }
                                cheatStartMap[p] = 'c'
                                cheatEndMap[cheatEndP] = 'C'
                            }
                        }
                    }
                } else 0
            }
        }

        printMap(cheatStartMap)
        printMap(cheatEndMap)

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day20_test.txt` file:
    val testInput = readInput("Day20_test")
    check(part1(testInput, 64)/*.also { println(it) }*/ == 1)

    // Read the input from the `src/Day20.txt` file.
    val input = readInput("Day20")
    part1(input, 100).println()

    check(part2(testInput, 76) == 3)
    check(part2(testInput, 74) == 7)
    check(part2(testInput, 72).also { println(it) } == 29)
    check(part2(testInput, 70).also { println(it) } == 41)
    check(part2(testInput, 68).also { println(it) } == 55)

    part2(input, 100).println()
}
