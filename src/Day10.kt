fun main() {
    fun part1(input: List<String>): Int {
        val m = input.size
        val n = input[0].length
        val map = input.map { it.map { it.digitToIntOrNull() } }

        fun nines(i: Int, j: Int, num: Int): List<Pair<Int, Int>> =
            if (map[i][j] == num) {
                println("$i $j $num")
                if (num == 9)
                    listOf(i to j)
                else {
                    val num = num + 1
                    listOf(i + 1 to j, i - 1 to j, i to j + 1, i to j - 1).asSequence()
                        .filter { (i, j) ->
                            i in 0 until m && j in 0 until n
                        }
                        .flatMap { (i, j) ->
                            nines(i, j, num)
                        }
                        .toList()
                }
            } else
                emptyList()


        val ans = (0 until m).sumOf { i ->
            (0 until n).sumOf { j ->
                nines(i, j, 0).distinct().size/*.also {
                    if (it != 0)
                        println("$i $j score: $it")
                }*/
            }
        }

        return ans
    }

    fun part2(input: List<String>): Long {
        val m = input.size
        val n = input[0].length
        val map = input.map { it.map { it.digitToIntOrNull() } }

        fun score(i: Int, j: Int, num: Int): Long =
            if (map[i][j] == num) {
                if (num == 9)
                    1
                else {
                    val num = num + 1
                    listOf(i + 1 to j, i - 1 to j, i to j + 1, i to j - 1).asSequence()
                        .filter { (i, j) ->
                            i in 0 until m && j in 0 until n
                        }
                        .sumOf { (i, j) ->
                            score(i, j, num)
                        }
                }
            } else
                0


        val ans = (0 until m).sumOf { i ->
            (0 until n).sumOf { j ->
                score(i, j, 0)
            }
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day10_test")
    check(part1(testInput).also { println(it) } == 36)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day10")
    part1(input).println()

    check(part2(testInput) == 81L)
    part2(input).println()
}
