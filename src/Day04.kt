const val XMAS = "XMAS"

class Diff(val id: Int, val jd: Int)
class XMasDiff(val m1: Diff, val m2: Diff, val s1: Diff, val s2: Diff)

val p2XMasDiffs = listOf(
    XMasDiff(Diff(-1, -1), Diff(-1, 1), Diff(1, -1), Diff(1, 1)),
    XMasDiff(Diff(1, -1), Diff(1, 1), Diff(-1, -1), Diff(-1, 1)),
    XMasDiff(Diff(-1, -1), Diff(1, -1), Diff(-1, 1), Diff(1, 1)),
    XMasDiff(Diff(-1, 1), Diff(1, 1), Diff(-1, -1), Diff(1, -1))
)

fun main() {
    fun part1(input: List<String>): Int {
        val m = input.size
        val ns = input.asSequence().map { it.length }.toSet()
        val n = ns.single()

        var count = 0
        for (i in 0 until m)
            for (j in 0 until n)
                for (iInc in -1..1)
                    startingAndDirection@ for (jInc in -1..1)
                        if (!(iInc == 0 && jInc == 0)) {
                            var k = 0
                            //println("Check $i $j $iInc $jInc")
                            var i = i
                            var j = j
                            while (k < 4) {
                                if (input.getOrNull(i)?.getOrNull(j) != XMAS[k])
                                    continue@startingAndDirection
                                k++
                                i += iInc
                                j += jInc
                            }
                            //println("Found $iInc $jInc")
                            count++
                        }

        return count
    }

    fun part2(input: List<String>): Int {

        val m = input.size
        val ns = input.asSequence().map { it.length }.toSet()
        val n = ns.single()

        var count = 0
        for (i in 0 until m)
            for (j in 0 until n)
                startingAndDirection@ for (xMaxDiff in p2XMasDiffs) {
                    with(xMaxDiff) {
                        if (input[i][j] == 'A' &&
                            input.getOrNull(i + m1.id)?.getOrNull(j + m1.jd) == 'M' &&
                            input.getOrNull(i + m2.id)?.getOrNull(j + m2.jd) == 'M' &&
                            input.getOrNull(i + s1.id)?.getOrNull(j + s1.jd) == 'S' &&
                            input.getOrNull(i + s2.id)?.getOrNull(j + s2.jd) == 'S'
                        )
                            count++
                    }
                }

        return count
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("test_input")) == 0)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day04_test")
    check(part1(testInput)/*.also { println(it) }*/ == 18)
    check(part2(testInput)/*.also { println(it) }*/ == 9)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
