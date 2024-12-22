fun main() {
    infix fun Long.mix(other: Long) =
        this xor other

    fun Long.prune() =
        this % 16777216
    //rem(16777216)

    fun Long.step1() =
        ((this * 64) mix this).prune()

    fun Long.step2() =
        ((this / 32) mix this).prune()

    fun Long.step3() =
        ((this * 2048) mix this).prune()

    fun Long.next() =
        step1().step2().step3()//.also { println(it) }

    fun Long.nextN(n: Int): Long =
        if (n == 0) this
        else next().nextN(n - 1)

    fun part1(input: List<String>): Long {
        val secretNumbers = input.map { it.toLong()/*.also { println(it) }*/ }

        val ans = secretNumbers.sumOf { it.nextN(2000) }

        return ans
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    check(123L.next() == 15887950L)
    check(123L.next().next() == 16495136L)

    // Or read a large test input from the `src/Day22_test.txt` file:
    val testInput = readInput("Day22_test")
    check(part1(testInput)/*.also { println(it) }*/ == 37327623L)

    // Read the input from the `src/Day22.txt` file.
    val input = readInput("Day22")
    part1(input).println()

    check(part2(testInput) == 1) // TODO note that the test input might be different
    part2(input).println()
}
