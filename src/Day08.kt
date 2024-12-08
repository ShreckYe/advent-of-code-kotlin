fun Char.isAntenna() =
    this in 'a'..'z' || this in 'A'..'Z' || this in '0'..'9'

fun main() {
    fun part1(input: List<String>): Int {
        val m = input.size
        val n = input[0].length
        val processed = input.flatMapIndexed { i, s -> s.mapIndexed { j, c -> c to (i to j) } }
        val antennaLocationMap = processed.asSequence()
            .filter { it.first.isAntenna() }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }
        val ans = antennaLocationMap.values.asSequence().flatMap {
            it.asSequence().flatMap { a1 ->
                it.asSequence().mapNotNull { a2 ->
                    if (a1 !== a2) {
                        val ani = a2.first * 2 - a1.first
                        val anj = a2.second * 2 - a1.second
                        if (ani in 0 until m && anj in 0 until n)
                            ani to anj
                        else null
                    } else null
                }
            }
        }
            .distinct()
            .count()
        return ans
    }

    fun part2(input: List<String>): Int {
        val m = input.size
        val n = input[0].length
        val processed = input.flatMapIndexed { i, s -> s.mapIndexed { j, c -> c to (i to j) } }
        val antennaLocationMap = processed.asSequence()
            .filter { it.first.isAntenna() }
            .groupBy { it.first }
            .mapValues { it.value.map { it.second } }


        val ans = (0 until m).sumOf { ani ->
            (0 until n).count { anj ->
                antennaLocationMap.values.any {
                    it.any { a1 ->
                        it.any { a2 ->
                            a1 !== a2 &&
                                    (ani - a1.first) * (anj - a2.second) == (ani - a2.first) * (anj - a1.second)
                        }
                    }
                }
            }
        }
        return ans
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 14)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day08")
    part1(input).println()

    check(part2(testInput) == 34)
    part2(input).println()
}
