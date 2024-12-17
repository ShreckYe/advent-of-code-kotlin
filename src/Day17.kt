fun main() {
    fun part1(input: List<String>): Int {
        return input.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day17_test.txt` file:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 1)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day17")
    part1(input).println()

    check(part2(testInput) == 1) // TODO note that the test input might be different
    part2(input).println()
}
