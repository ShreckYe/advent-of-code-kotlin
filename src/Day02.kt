import kotlin.math.abs

fun <T> Sequence<T>.removeAt(index: Int) =
    withIndex().filter { (i, _) -> i != index }.map { it.value }

fun main() {
    fun process(input: List<String>) =
        input.map { it.splitToSequence(' ').map { it.toInt() }.toList() }

    fun part1(input: List<String>): Int {
        val processed = process(input)
        val ans = processed.count {
            val zipped = it.asSequence().zipWithNext()
            (zipped.all { (a, b) -> a < b } || zipped.all { (a, b) -> a > b }) &&
                    zipped.all { (a, b) -> abs(a - b) <= 3 }
        }
        return ans
    }

    fun part2(input: List<String>): Int {
        val processed = process(input)/*.also { println("size: ${it.size}") }*/
        val ans = processed.count {
            val levelSequence = it.asSequence()
            val zipped = levelSequence.zipWithNext()

            fun isP2Safe(ltOrGt: (a: Int, b: Int) -> Boolean): Boolean {
                fun Pair<Int, Int>.isP1Safe(): Boolean {
                    val (a, b) = this
                    return ltOrGt(a, b) && abs(a - b) <= 3
                }

                fun Sequence<Int>.isP1Safe() =
                    zipWithNext().all { it.isP1Safe() }

                val violation = zipped.withIndex().find { (_, z) -> !z.isP1Safe() }
                //.also { println("violation: $it") }
                return if (violation === null)
                    true
                else {
                    val index = violation.index

                    levelSequence.removeAt(index)/*.also { println("removed second, seq: ${it.toList()}") }*/
                        .isP1Safe() ||
                            levelSequence.removeAt(index + 1)/*.also { println("removed second, seq: ${it.toList()}") }*/
                                .isP1Safe()
                }/*.also {
                    println("line safe: $it")
                }*/
            }

            //println("new line")
            isP2Safe { a, b -> a < b } || isP2Safe { a, b -> a > b }
        }/*.also {
            println("ans:$it")
        }*/
        return ans
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("1 2 3 4 5")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 2)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day02")
    part1(input).println()

    check(part2(testInput)/*.also { println(it) }*/ == 4)
    part2(input).println()
}
