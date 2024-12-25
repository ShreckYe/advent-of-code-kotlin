enum class LockOrKey {
    Lock, Key
}

fun main() {
    fun part1(input: List<String>): Int {
        val emptyLineIndices = input.asSequence().withIndex().filter { it.value == "" }.map { it.index }
        val lockOrKeyHeightss = (sequenceOf(-1) + emptyLineIndices + sequenceOf(input.size)).zipWithNext { a, b ->
            val schematic = input.subList(a + 1, b)
            if (schematic.first().all { it == '#' })
                LockOrKey.Lock to (0 until 5).map { j ->
                    schematic.subList(1, schematic.size).count { line -> line[j] == '#' }
                }
            else if (schematic.last().all { it == '#' })
                LockOrKey.Key to (0 until 5).map { j ->
                    schematic.subList(0, schematic.size - 1).count { line -> line[j] == '#' }
                }
            else
                throw IllegalArgumentException()
        }.toList()

        val (locks, keys) = lockOrKeyHeightss.partition { it.first == LockOrKey.Lock }
        val ans = locks.sumOf { lock ->
            keys.count { key ->
                (lock.second zip key.second).all { (lh, kh) -> lh + kh <= 5 }
            }
        }

        return ans
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day25_test.txt` file:
    val testInput = readInput("Day25_test")
    check(part1(testInput) == 3)

    // Read the input from the `src/Day25.txt` file.
    val input = readInput("Day25")
    part1(input).println()
}
