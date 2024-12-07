fun results(numbers: List<Long>): List<Long> =
    if (numbers.size == 1)
        numbers
    else {
        val a = numbers[0]
        val b = numbers[1]
        val remaining = numbers.drop(2)
        results(listOf(a + b) + remaining) + results(listOf(a * b) + remaining)
    }

fun results2(numbers: List<Long>): List<Long> =
    if (numbers.size == 1)
        numbers
    else {
        val a = numbers[0]
        val b = numbers[1]
        val remaining = numbers.drop(2)
        results2(listOf(a + b) + remaining) +
                results2(listOf(a * b) + remaining) +
                results2(listOf((a.toString() +  b.toString()).toLong()) + remaining)
    }

fun main() {
    fun part1(input: List<String>): Long {
        val processed = input.map {
            val splitted = it.split(' ')
            val testValue = splitted.first().removeSuffix(":").toLong()
            val numbers = splitted.asSequence().drop(1).map { it.toLong() }.toList()
            testValue to numbers
        }

        val ans = processed.sumOf {
            if (it.first in results(it.second))
                it.first
            else 0
        }

        return ans
    }

    fun part2(input: List<String>): Long {
        val processed = input.map {
            val splitted = it.split(' ')
            val testValue = splitted.first().removeSuffix(":").toLong()
            val numbers = splitted.asSequence().drop(1).map { it.toLong() }.toList()
            testValue to numbers
        }

        val ans = processed.sumOf {
            if (it.first in results2(it.second))
                it.first
            else 0
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 3749L)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day07")
    part1(input).println()

    check(part2(testInput) == 11387L)
    part2(input).println()
}
