fun main() {
    fun part1(input: List<String>): Int {
        val connections = input.map {
            with(it.split('-')) {
                this[0] to this[1]
            }
        }

        val connectionMap = (connections + connections.map { it.second to it.first })
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }

        //println(connectionMap)

        val sets = connectionMap.asSequence()
            .flatMap {
                val connecteds = it.value
                connecteds.asSequence().flatMap { second ->
                    connecteds.asSequence().mapNotNull { third ->
                        if (connectionMap[second]?.contains(third) == true)
                            setOf(it.key, second, third)
                        else
                            null
                    }
                }
            }
            //.also { println(it.toList()) }
            .distinct()
            //.also { println(it.toList()) }
            .filter {
                it.any { it[0] == 't' }
            }
            //.also { println(it.toList()) }
            .toSet()

        return sets.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day23_test.txt` file:
    val testInput = readInput("Day23_test")
    check(part1(testInput)/*.also { println(it) }*/ == 7)

    // Read the input from the `src/Day23.txt` file.
    val input = readInput("Day23")
    part1(input).println()

    check(part2(testInput) == 1) // TODO note that the test input might be different
    part2(input).println()
}
