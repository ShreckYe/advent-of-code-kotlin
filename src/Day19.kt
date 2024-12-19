fun main() {
    fun part1(input: List<String>): Int {
        //val emptyLineIndex = input.indexOf("")
        val towels = input.first().split(", ")
        val designs = input.subList(2, input.size)

        val regex = towels.joinToString("|", prefix = "(", postfix = ")").let {
            Regex("$it+")/*.also { println(it) }*/ // `*` works too
        }

        val ans = designs.count { regex matches it }

        return ans
    }

    fun part2(input: List<String>): Long {
        val towels = input.first().split(", ")
        val designs = input.subList(2, input.size)

        //val towelRegex = Regex(towels.joinToString("|", prefix = "(", postfix = ")"))

        val ans = designs.sumOf { design ->
            //println(design)
            val memoizedNumWaysFrom = Array<Long?>(design.length) { null }

            // DP
            fun numWays(from: Int): Long =
                if (from == design.length)
                    1L
                else {
                    val memoized = memoizedNumWaysFrom[from]
                    //println("$from $memoized")
                    if (memoized !== null) memoized
                    else {
                        val designFromI = design.substring(from)
                        towels.sumOf { towel ->
                            if (designFromI.startsWith(towel)) numWays(from + towel.length)
                            else 0L
                        }.also {
                            memoizedNumWaysFrom[from] = it
                        }
                    }
                }

            numWays(0)
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day19_test.txt` file:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 6)

    // Read the input from the `src/Day19.txt` file.
    val input = readInput("Day19")
    part1(input).println()

    check(part2(testInput) == 16L)
    part2(input).println()
}
