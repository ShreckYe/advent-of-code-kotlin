import Direction.*
import java.util.EnumSet

val guardChars = listOf('^', '>', 'v', '<')

fun Char.toDirection() =
    when (this) {
        '^' -> Up
        '>' -> Right
        'v' -> Down
        '<' -> Left
        else -> null
    }

fun Direction.turnRight90Degrees() =
    when (this) {
        Up -> Right
        Right -> Down
        Down -> Left
        Left -> Up
    }

fun main() {
    fun part1(input: List<String>): Int {
        val m = input.size
        val n = input[0].length
        val guardPosition = input.asSequence()
            .mapIndexed { i, s -> i to s.indexOfFirst { it in guardChars } }
            .find { it.second != -1 }!!

        var (i, j) = guardPosition
        val map = input.map { it.toCharArray() }
        var direction = map[i][j].toDirection()!!

        //var numVisits = 1
        fun mark() {
            map[i][j] = 'X'
        }
        mark()
        outer@ while (true) {
            while (true) {
                var ni = i
                var nj = j
                when (direction) {
                    Up -> ni--
                    Right -> nj++
                    Down -> ni++
                    Left -> nj--
                }

                if (ni !in 0 until m || nj !in 0 until n)
                    break@outer

                if (map[ni][nj] == '#') {
                    direction = direction.turnRight90Degrees()
                    continue
                }

                i = ni
                j = nj
                mark()
                //println("$i $j $direction ${map[ni][nj]}")
                //println(map.joinToString("\n") {it.joinToString("")})
                //println()
                break
            }
        }

        return map.sumOf { it.count { it == 'X' } }
    }

    fun part2(input: List<String>): Int {
        val m = input.size
        val n = input[0].length
        val guardPosition = input.asSequence()
            .mapIndexed { i, s -> i to s.indexOfFirst { it in listOf('^', '>', 'v', '<') } }
            .find { it.second != -1 }!!

        fun isLoop(map : List<CharArray>) : Boolean {
            var (i, j) = guardPosition
            var direction = map[i][j].toDirection()!!

            //var numVisits = 1
            val mapWalkedDirection = List(m) { List(n) { EnumSet.noneOf(Direction::class.java)} }
            fun mark(direction: Direction) {
                mapWalkedDirection[i][j].add(direction)
            }
            outer@ while (true) {
                while (true) {
                    mark(direction)

                    var ni = i
                    var nj = j
                    when (direction) {
                        Up -> ni--
                        Right -> nj++
                        Down -> ni++
                        Left -> nj--
                    }

                    if (ni !in 0 until m || nj !in 0 until n)
                        break@outer

                    if (map[ni][nj] == '#') {
                        direction = direction.turnRight90Degrees()
                        continue
                    }

                    val waledDirectionSet = mapWalkedDirection[ni][nj]
                    if (waledDirectionSet.contains(direction))
                        return true

                    i = ni
                    j = nj
                    //println("$i $j $direction ${map[ni][nj]}")
                    //println(map.joinToString("\n") {it.joinToString("")})
                    //println()
                    break
                }
            }

            return false
        }

        val ans = (0 until m).sumOf { oi ->
            (0 until n).count { oj ->
                val oc = input[oi][oj]
                if (oc in guardChars || oc == '#')
                    false
                else {
                    val map = input.map { it.toCharArray() }
                    map[oi][oj] = '#'
                    isLoop(map)
                }
            }
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day06_test")
    check(part1(testInput)/*.also { println(it) }*/ == 41)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day06")
    part1(input).println()

    check(part2(testInput)/*.also { println(it) }*/ == 6)
    part2(input).println()
}
