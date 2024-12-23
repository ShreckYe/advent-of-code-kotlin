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

    fun part2(input: List<String>): String {
        val connections = input.map {
            with(it.split('-')) {
                this[0] to this[1]
            }
        }

        val connectionMap = (connections + connections.map { it.second to it.first })
            .groupBy { it.first }
            .mapValues { it.value.map { it.second }.toSet() }

        //println(connectionMap)

        val setsOfSizes = Array<Set<Set<String>>?>(connectionMap.size + 1) { null }
        //setsOfSizes[0] = emptySet() // doesn't work
        setsOfSizes[1] = connectionMap.keys.asSequence().map { setOf(it) }.toSet()
        var maxSize = 1
        for (i in 1 until connectionMap.size) {
            val sets = setsOfSizes[i]!!

            val nextSet = sets.flatMap { set ->
                set.flatMap { connectionMap.getValue(it) }.toSet()
                    .asSequence()
                    .filter {
                        connectionMap.getValue(it).containsAll(set)
                    }
                    .map {
                        set + it
                    }
            }.toSet()

            if (nextSet.isEmpty())
                break

            setsOfSizes[i + 1] = nextSet
            maxSize = i + 1
        }

        val ans = setsOfSizes[maxSize]!!.single().sorted().joinToString(",")
        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day23_test.txt` file:
    val testInput = readInput("Day23_test")
    check(part1(testInput)/*.also { println(it) }*/ == 7)

    // Read the input from the `src/Day23.txt` file.
    val input = readInput("Day23")
    part1(input).println()

    check(part2(testInput) == "co,de,ka,ta")
    part2(input).println()
}
