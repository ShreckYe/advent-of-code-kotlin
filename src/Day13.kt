fun main() {
    data class Vector<T : Number>(val x: T, val y: T)
    data class Machine<T : Number>(val buttonA: Vector<T>, val buttonB: Vector<T>, val prize: Vector<T>)

    fun <T : Number> process(input: List<String>, toNumber: String.() -> T) =
        input.chunked(4) {
            fun String.parseXAndY(): Vector<T> {
                val numbers = substring(indexOf(':') + 2, length).splitToSequence(", ").map {
                    it.substring(2).toNumber()
                }.toList()
                return Vector(numbers[0], numbers[1])
            }
            Machine(it[0].parseXAndY(), it[1].parseXAndY(), it[2].parseXAndY())
        }

    fun det(v1: Vector<Int>, v2: Vector<Int>) =
        v1.x * v2.y - v2.x * v1.y

    fun  det(v1: Vector<Long>, v2: Vector<Long>) =
        v1.x * v2.y - v2.x * v1.y


    fun part1(input: List<String>): Int {
        val processed = process(input) { toInt() }

        /*
        x1a + x2b = xp
        y1a + y2b = yp
         */

        val ans = processed.sumOf {
            with(it) {
                /*
                println(this)
                println(det(buttonA, buttonB))
                println(det(buttonB, prize).toDouble() / det(buttonB, buttonA))
                println(det(buttonA, prize).toDouble() / det(buttonA, buttonB))
                kotlin.io.println()
                */

                val detAB = det(buttonA, buttonB)
                val numA = -det(buttonB, prize).toDouble() / detAB
                val numB = det(buttonA, prize).toDouble() / det(buttonA, buttonB)

                fun Double.isNumValid(): Boolean {
                    val int = toInt()
                    //println(int)
                    return (int in 0..100)/*.also { it.println() }*/ && (int.toDouble() == this)/*.also { it.println() }*/
                }

                if (numA.isNumValid() && numB.isNumValid())
                    numA.toInt() * 3 + numB.toInt()
                else 0
            }
        }

        return ans
    }

    fun part2(input: List<String>): Long {
        val processed = process(input) {toLong()}

        val ans = processed.sumOf {
            with(it) {
                val prize = Vector(prize.x +10000000000000, prize.y +10000000000000 )

                /*
                println(this)
                println(det(buttonA, buttonB))
                println(det(buttonB, prize).toDouble() / det(buttonB, buttonA))
                println(det(buttonA, prize).toDouble() / det(buttonA, buttonB))
                kotlin.io.println()
                */

                val detAB = det(buttonA, buttonB)
                val numA = -det(buttonB, prize).toDouble() / detAB
                val numB = det(buttonA, prize).toDouble() / det(buttonA, buttonB)

                fun Double.isNumValid(): Boolean {
                    val integer = toLong()
                    return integer >= 0 && integer.toDouble() == this
                }

                if (numA.isNumValid() && numB.isNumValid())
                    numA.toLong() * 3 + numB.toLong()
                else 0
            }
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day13_test")
    //part1(testInput)
    check(part1(testInput)/*.also { println(it) }*/ == 480)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day13")
    part1(input).println()

    part2(testInput)
    //check(part2(testInput) == 1)
    part2(input).println()
}
