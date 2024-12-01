import kotlin.math.abs

fun main() {
    fun processInput(input: List<String>) =
        input.map { it.split(Regex(" +")).map { it.toInt() } }

    fun part1(input: List<String>): Int {
        val inputPairs = processInput(input)
        val paired = inputPairs.map { it[0] }.sorted() zip inputPairs.map { it[1] }.sorted()
        return paired.sumOf { abs(it.first - it.second) } // The bound should be 100000 * 2 * 1000 so `Int` should be fine.
    }

    fun part2(input: List<String>): Long {
        val inputPairs = processInput(input)
        val leftSet = inputPairs.asSequence().map { it[0] }.groupingBy { it }.eachCount()
        val right = inputPairs.asSequence().map { it[1] }//.also { println(it.toList()) }
        return right.map { it.toLong() * (leftSet[it] ?: 0).toLong() }/*.also { println(it.toList()) }*/.sum() // bigger bound
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("1  2")) == 1)

    // Or read a large test input from the `src/Day01_test.txt` file:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 11)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day01")
    part1(input).println()

    check(part2(testInput)/*.also { println(it) }*/ == 31L)
    part2(input).println()
}
