import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.abs

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

    // In particular, if a robot arm is ever aimed at a gap where no button is present on the keypad, even for an instant, the robot will panic unrecoverably.
    fun Position.isPathAllOnButtons(keypad: List<List<Char?>>, movements: List<Direction>) = movements.asSequence()
        .scan(this) { acc, c -> acc + c.diff }
        .all { keypad[it] !== null }

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
                .filter {
                    prevP.isPathAllOnButtons(numericCharss, it)
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
                    val shortestLength = it.numericToDirectionalControlSequences().flatMapConcat {
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

    val directionalCharAPosition = directionalPositionMap.getValue('A')

    fun String.bestDirectionalControlSequence(
        keypad: List<List<Char?>>,
        positionMap: Map<Char, Position>,
        prevC: Char, /*prevResultLastDirection: Direction?,*/
        from: Int
    ): String =
        if (from == length)
            ""
        else if (from < length) {
            val c = this[from]

            val prevP = positionMap.getValue(prevC)
            val (id, jd) = positionMap.getValue(c) - prevP

            val currentSequenceWithoutA: String
            //val resultLastDirection: Direction
            when {
                id == 0 && jd == 0 -> {
                    currentSequenceWithoutA = ""
                    // can't be all `A`s
                    //resultLastDirection = prevResultLastDirection!!
                }

                jd == 0 -> {
                    val direction = if (id > 0) Direction.Down else Direction.Up
                    val char = direction.toChar()
                    currentSequenceWithoutA = String(CharArray(abs(id)) { char })
                    //resultLastDirection = direction
                }

                id == 0 -> {
                    val direction = if (jd > 0) Direction.Right else Direction.Left
                    val char = direction.toChar()
                    currentSequenceWithoutA = String(CharArray(abs(jd)) { char })
                    //resultLastDirection = direction
                }

                else -> {
                    /*
                    The only case where there is "not changing direction" vs "going the the farther one first" is
                    when it's at "v" with direction right, and it has to go through "<",
                    and in this case "going to the farther one first" should win because it's 4 steps vs 6 steps (minus one when it's transformed to its control sequence)
                     */

                    val iDirection = if (id > 0) Direction.Down else Direction.Up
                    val iDChar = iDirection.toChar()
                    val iDSequence = String(CharArray(abs(id)) { iDChar })
                    val jDirection = if (jd > 0) Direction.Right else Direction.Left
                    val jDChar = jDirection.toChar()
                    val jDSequence = String(CharArray(abs(jd)) { jDChar })
                    val numStepsIDButtonToA =
                        (directionalPositionMap.getValue(iDChar) - directionalCharAPosition).coordinateAbsSum()
                    val numStepsJDButtonToA =
                        (directionalPositionMap.getValue(jDChar) - directionalCharAPosition).coordinateAbsSum()


                    currentSequenceWithoutA =
                            // TODO this could be simplified
                        if (!prevP.isPathAllOnButtons(keypad, List(abs(id)) { iDirection })) {
                            assert(prevP.isPathAllOnButtons(keypad, List(abs(jd)) { jDirection }))
                            jDSequence + iDSequence
                        } else if (!prevP.isPathAllOnButtons(keypad, List(abs(jd)) { jDirection })) {
                            assert(prevP.isPathAllOnButtons(keypad, List(abs(id)) { iDirection }))
                            iDSequence + jDSequence
                        } else {
                            if (numStepsIDButtonToA < numStepsJDButtonToA)
                                jDSequence + iDSequence
                            else if (numStepsIDButtonToA > numStepsJDButtonToA)
                                iDSequence + jDSequence
                            else {
                                // The case here can only be "^" and ">" actually, and they are equivalent, even in terms of produced control sequences
                                assert(iDirection == Direction.Up && jDirection == Direction.Right)
                                // always prefer right first for it's possible that it doesn't change direction
                                jDSequence + iDSequence
                            }
                        }
                }
            }

            currentSequenceWithoutA + 'A' + bestDirectionalControlSequence(
                keypad, positionMap,
                c, /*resultLastDirection,*/
                from + 1
            )
        } else
            throw AssertionError()

    fun String.numericalBestDirectionalControlSequence() =
        bestDirectionalControlSequence(numericCharss, numericPositionMap, 'A', 0)

    fun String.directionalBestDirectionalControlSequence() =
        bestDirectionalControlSequence(directionalCharss, directionalPositionMap, 'A', 0)

    fun part2(input: List<String>, numRobotDirectionalKeypads: Int): Int {
        val ans = input.sumOf {
            fun String.directionalTransforms(num: Int): String =
                if (num == 0)
                    this
                else
                    directionalBestDirectionalControlSequence().directionalTransforms(num - 1)

            //println(it)

            val finalControlSequence =
                it.numericalBestDirectionalControlSequence()//.also { println(it) }
                    .directionalTransforms(numRobotDirectionalKeypads)//.also { println(it) }

            finalControlSequence.length.also { println(it) } * it.removeSuffix("A").toInt().also { println(it) }
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day21_test.txt` file:
    val testInput = readInput("Day21_test")
    check(measureTimeAndPrint { part1(testInput) } == 126384)

    // Read the input from the `src/Day21.txt` file.
    val input = readInput("Day21")
    measureTimeAndPrint { part1(input) }.println()

    check(part2(testInput, 2).also {
        println(it)
    } == 126384)
    part2(input, 25).println()
}
