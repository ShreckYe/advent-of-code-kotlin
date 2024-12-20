fun main() {
    fun processDistances(input: List<String>): Triple<List<Array<Int?>>, List<Array<Int?>>, Int> {
        val sPosition = input.positionOf('S')
        val ePosition = input.positionOf('E')

        fun distancesFrom(p: Position): List<Array<Int?>> {
            val distances = input.map { Array<Int?>(it.length) { null } }

            // `-Xss1G` needed TODO why?
            fun setDistances(ps: List<Position>, distance: Int) {
                //println(ps)
                if (ps.isNotEmpty()) {
                    val nexts = ps.flatMap { p ->
                        val c = input.getOrNull(p.i)?.getOrNull(p.j)
                        if (c.isHabitable() && distances[p] === null) {
                            distances[p] = distance
                            Direction.entries.map { direction -> p + direction.diff }
                        } else emptyList()
                    }
                        .distinct()
                    setDistances(nexts, distance + 1)
                }
            }

            setDistances(listOf(p), 0)

            return distances
        }

        val distancesFromS = distancesFrom(sPosition)
        val distancesFromE = distancesFrom(ePosition)
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

                    val cheats = (1..20).flatMap { diffSum ->
                        (0..20).mapNotNull { iDiff ->
                            val jDiff = diffSum - iDiff
                            val end = p + PositionDiff(iDiff, jDiff)
                            if (input.getOrNull(end.i)?.getOrNull(end.j).isHabitable()) diffSum to end
                            else null
                        }
                    }

                    cheats.count { (diffSum, cheatEndP) ->
                        (sToEDistance - (sDistance + diffSum + distancesFromE[cheatEndP]!!) >= leastNumSecondsSaved).also {
                            if (it) {
                                println("$sToEDistance $sDistance $p $diffSum $cheatEndP ${distancesFromE[cheatEndP]!!} ${(sDistance + diffSum + distancesFromE[cheatEndP]!!)}")

                                printMap(input.map { it.toCharArray() }.also {
                                    it[p] = '0'
                                    it[cheatEndP] = diffSum.digitToChar(16)
                                })

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

    check(part2(testInput, 72).also { println(it) } == 29)
    check(part2(testInput, 74) == 7)
    check(part2(testInput, 76) == 3)
    part2(input, 100).println()
}
