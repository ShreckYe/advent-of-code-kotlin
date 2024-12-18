fun main() {
    fun processedInput(input: List<String>) = input.map {
        it.splitToSequence(',').map { it.toInt() }.toList().let {
            Position(it[0], it[1]) // They should be swapped actually but it doesn't matter.
        }
    }

    fun part1(input: List<String>, size: Int, numBytesFallen: Int): Int {
        val processedInput = processedInput(input)
        val first1K = processedInput.take(numBytesFallen)
        val memorySpace = List(size) { CharArray(size) { '.' } }.also {
            for (byte in first1K)
                it[byte] = '#'
        }
        //println(memorySpace.joinToString("\n", postfix = "\n") {it.concatToString()})
        val start = Position(0, 0)
        val exit = Position(size - 1, size - 1)

        tailrec fun stepsFrom(acc: Int, ps: Set<Position>, visitedPs: MutableSet<Position>): Int =
        //run {
            //println("$acc $ps $visitedPs")
            if (exit in ps)
                acc
            else stepsFrom(acc + 1, ps.flatMap { p ->
                Direction.entries.mapNotNull { d ->
                    val np = p + d.diff
                    val nc = memorySpace.getOrNull(np.i)?.getOrNull(np.j)
                    if (np !in visitedPs)
                        when (nc) {
                            null, '#' -> null
                            else -> np
                        }
                    else null
                }
            }.toSet(), visitedPs.apply { addAll(ps) })
        /*}.also {
            println("$acc $ps $visitedPs $it")
        }*/

        return stepsFrom(0, setOf(start), mutableSetOf())
    }

    fun part2(input: List<String>, size: Int): String {
        val processedInput = processedInput(input)

        val memorySpace = List(size) { CharArray(size) { '.' } }
        //println(memorySpace.joinToString("\n", postfix = "\n") {it.concatToString()})
        val start = Position(0, 0)
        val exit = Position(size - 1, size - 1)

        val ans = processedInput.first { byteFallen ->
            memorySpace[byteFallen] = '#'

            tailrec fun isCutOff(ps: Set<Position>, visitedPs: MutableSet<Position>): Boolean =
                if (ps.isEmpty())
                    true
                else if (exit in ps)
                    false
                else isCutOff(ps.flatMap { p ->
                    Direction.entries.mapNotNull { d ->
                        val np = p + d.diff
                        val nc = memorySpace.getOrNull(np.i)?.getOrNull(np.j)
                        if (np !in visitedPs)
                            when (nc) {
                                null, '#' -> null
                                else -> np
                            }
                        else null
                    }
                }.toSet(), visitedPs.apply { addAll(ps) })

            isCutOff(setOf(start), mutableSetOf())
        }


        return with(ans) { "$i,$j" }
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day18_test.txt` file:
    val testInput = readInput("Day18_test")
    check(part1(testInput, 7, 12) == 22)

    // Read the input from the `src/Day18.txt` file.
    val input = readInput("Day18")
    part1(input, 71, 1024).println()

    check(part2(testInput, 7) == "6,1")
    part2(input, 71).println()
}
