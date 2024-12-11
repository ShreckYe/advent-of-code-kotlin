package day11.withlong

import println
import readInput

fun Long.blink(): List<Long> {
    val stoneString = toString()
    val length = stoneString.length
    return when {
        this == 0L -> listOf(1L)
        length % 2 == 0 -> listOf(
            stoneString.substring(0, length / 2).toLong(),
            stoneString.substring(length / 2, length).toLong()
        )

        else -> listOf(this * 2024.toLong())
    }
}

fun List<Long>.blink(): List<Long> =
    flatMap { stone -> stone.blink() }

fun Long.nBlinks(memoizationCache: Array<HashMap<Long, List<Long>>>, n: Int): List<Long> =
    if (n == 0) listOf(this)
    else {
        val cachedStones = memoizationCache[n][this]
        cachedStones ?: if (n == 1) blink()
        else {
            val halfN = n / 2
            val odd = n % 2 == 1
            nBlinks(memoizationCache, halfN).flatMap { it.nBlinks(memoizationCache, halfN) }.let {
                if (odd) it.blink() else it
            }
        }
            .also {
                memoizationCache[n][this] = it
            }
    }
/*.also {
    println("n = $n: $it")
}*/

fun List<Long>.nBlinks(memoizationCache: Array<HashMap<Long, List<Long>>>, n: Int): List<Long> =
    flatMap { it.nBlinks(memoizationCache, n) }

fun main() {
    fun process(input: List<String>) =
        input.single().splitToSequence(' ').map { it.toLong() }.toList()

    fun part1(input: List<String>): Int {
        var stones = process(input)

        repeat(25) {
            stones = stones.blink()
        }

        return stones.size
    }

    fun memoizationCache39() =
        Array<HashMap<Long, List<Long>>>(39) { HashMap() }

    fun part2(input: List<String>): Long {
        val stones = process(input)

        // The brute force solution takes long and causes `OutOfMemoryError`

        var i = 0
        val memoizationCache = memoizationCache39()
        val ans = stones.nBlinks(memoizationCache, 38).also {
            println("38 times: ${it.size}")
            println("memoization cache sizes: ${memoizationCache.map { it.size }}")
        }
            /*
            .sumOf {
                it.nBlinks(memoizationCache, 37).size.toLong()
            } // There is not enough memory to reuse `memoizationCache` and it seems different for each number.
            */
            .groupingBy { it }.eachCount()
            .entries.sumOf {
                println(i++)
                it.key.nBlinks(memoizationCache39(), 37).size.toLong() * it.value
            }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 55312)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day11")
    part1(input).println()

    var stones = listOf(0L)
    println(stones.size)
    repeat(40) {
        stones = stones.blink()
        if (it < 10) println("${it + 1} times: " + stones)
        println("${it + 1} times: " + stones.size)
    }
    val memoizationCache = memoizationCache39()
    println(0L.nBlinks(memoizationCache, 38).size)
    println("memoization cache sizes: ${memoizationCache.map { it.size }}")
    check(process(input).nBlinks(memoizationCache, 25).size == 204022)

    part2(input).println()
}
