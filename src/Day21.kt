import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
fun main() {
    val numericCharss =
        listOf(listOf('7', '8', '9'), listOf('4', '5', '6'), listOf('1', '2', '3'), listOf(null, '0', 'A'))
    val numericPositionMap = (('0'..'9') + 'A').associateWith {
        numericCharss.positionOf(it)
    }

    val directionalCharss = listOf(listOf(null, '^', 'A'), listOf('<', 'v', '>'))
    val directionalPositionMap = (directionChars + 'A').associateWith {
        directionalCharss.positionOf(it)
    }

    // see commit 4b2e19849f6e17c4786a55577da5568ef92c5f11 for a version using `Flow<Flow<Char>>`
    fun String.directionalControlSequences(
        positionMap: Map<Char, Position>, prevC: Char, from: Int
    ): Flow<String> =
        if (from == length)
            flowOf("")
        else if (from < length) {
            val c = this[from]
            //acc.flatMapConcat {
            val prevP = positionMap.getValue(prevC)
            val (id, jd) = positionMap.getValue(c) - prevP

            val idDirections =
                if (id == 0) emptyList()
                else if (id > 0) List(id) { Direction.Down }
                else List(-id) { Direction.Up }
            val jdDirections =
                if (jd == 0) emptyList()
                else if (jd > 0) List(jd) { Direction.Right }
                else List(-jd) { Direction.Left }

            val directionss = listOf(idDirections + jdDirections, jdDirections + idDirections)
                .distinct()
                // In particular, if a robot arm is ever aimed at a gap where no button is present on the keypad, even for an instant, the robot will panic unrecoverably.
                .filter {
                    it.asSequence()
                        .scan(prevP) { acc, c -> acc + c.diff }
                        .all { numericCharss[it] !== null }
                }

            val directionalSequences =
                directionss.map { (it.asSequence().map { it.toChar() } + 'A').joinToString("") }

            directionalSequences.asFlow().flatMapConcat { currentDirectionals ->
                directionalControlSequences(positionMap, c, from + 1).map { remainingDirectionals ->
                    currentDirectionals + remainingDirectionals
                }
            }
            //}
        } else
            throw AssertionError()


    fun String.numericToDirectionalControlSequences(
        prevC: Char = 'A', /*acc: Flow<Flow<Char>> = flowOf(emptyFlow()),*/ from: Int = 0
    ) =
        directionalControlSequences(numericPositionMap, prevC, from)

    fun String.directionalToDirectionalControlSequences(prevC: Char = 'A', from: Int = 0) =
        directionalControlSequences(directionalPositionMap, prevC, from)

    suspend fun Flow<Char>.concatToString() =
        toList().joinToString("")

    suspend fun <T : Comparable<T>> Flow<T>.min() =
        reduce { a, b -> if (a <= b) a else b }

    fun part1(input: List<String>): Int {
        val ans = runBlocking(Dispatchers.Default) {
            input.map {
                async {
                    val shortestLength = it.numericToDirectionalControlSequences().flatMapMerge {
                        it.directionalToDirectionalControlSequences().flatMapConcat {
                            it.directionalToDirectionalControlSequences()
                        }
                    }
                        .map { it.count() }
                        .min()

                    shortestLength * it.removeSuffix("A").toInt()
                }
            }
                .awaitAll()
                .sum()
        }

        return ans
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day21_test.txt` file:
    val testInput = readInput("Day21_test")
    check(measureTimeAndPrint { part1(testInput) } == 126384)

    // Read the input from the `src/Day21.txt` file.
    val input = readInput("Day21")
    measureTimeAndPrint { part1(input) }.println()

    //check(part2(testInput) == 1)
    part2(input).println()
}
