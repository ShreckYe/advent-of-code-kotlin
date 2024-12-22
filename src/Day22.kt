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
        val secretNumbers = input.map { it.toLong() }

        val sequenceToPriceMap = secretNumbers.map {
            //println(it)
            val secretNumberSequence = (0 until 2000).scan(it) { acc, _ -> acc.next() }/*.also {
                println(it)
                println(it.size)
            }*/
            val onesDigits = secretNumberSequence.map { it.toInt() % 10 }/*.also {
                println(it)
                println(it.size)
            }*/
            val changes = onesDigits.zipWithNext { a, b -> b - a }//.also { println(it) }

            //println()

            changes.asSequence().windowed(4)
                .mapIndexed { i, sequence ->
                    sequence to onesDigits[i + 4]
                }
                .groupBy { it.first }
                // So, if you gave the monkey that sequence of changes, it would wait until the first time it sees that sequence and then immediately sell your hiding spot information at the current price, winning you 6 bananas.
                .mapValues { it.value.first().second }
        }

        val numTotalBananas = sequenceToPriceMap.flatMap { it.entries }
            .groupBy { it.key }
            .entries
            .map { it.value.sumOf { it.value } }

        val ans = numTotalBananas.max()

        return ans
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

    val testInput2 = readInput("Day22_test2")
    check(part2(testInput2)/*.also { println(it) }*/ == 23)
    part2(input).println()
}
