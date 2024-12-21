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
    val directionalChars = directionalCharss.flatMap { it.filterNotNull() }
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


    fun String.numericToDirectionalControlSequences() =
        directionalControlSequences(numericPositionMap, 'A', 0)

    fun String.directionalToDirectionalControlSequences(prevC: Char) =
        directionalControlSequences(directionalPositionMap, prevC, 0)

    fun String.directionalToDirectionalControlSequences() =
        directionalToDirectionalControlSequences('A')

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

    // This algorithm may or may not be incorrect for the numerical keypad.
    tailrec fun String.bestDirectionalControlSequence(
        acc: StringBuilder,
        keypad: List<List<Char?>>,
        positionMap: Map<Char, Position>,
        prevC: Char,
        prevResultLastDirection: Direction?,
        from: Int
    ): String =
        if (from == length)
            acc.toString()
        else if (from < length) {
            val c = this[from]

            val prevP = positionMap.getValue(prevC)
            val (id, jd) = positionMap.getValue(c) - prevP

            val currentSequenceWithoutA: String
            val resultLastDirection: Direction?
            when {
                id == 0 && jd == 0 -> {
                    currentSequenceWithoutA = ""
                    // can't be all `A`s
                    resultLastDirection = prevResultLastDirection
                }

                jd == 0 -> {
                    val direction = if (id > 0) Direction.Down else Direction.Up
                    val char = direction.toChar()
                    currentSequenceWithoutA = String(CharArray(abs(id)) { char })
                    resultLastDirection = direction
                }

                id == 0 -> {
                    val direction = if (jd > 0) Direction.Right else Direction.Left
                    val char = direction.toChar()
                    currentSequenceWithoutA = String(CharArray(abs(jd)) { char })
                    resultLastDirection = direction
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
                            resultLastDirection = iDirection
                            jDSequence + iDSequence
                        } else if (!prevP.isPathAllOnButtons(keypad, List(abs(jd)) { jDirection })) {
                            assert(prevP.isPathAllOnButtons(keypad, List(abs(id)) { iDirection }))
                            resultLastDirection = jDirection
                            iDSequence + jDSequence
                        } else {
                            // This seems not important anymore
                            if (numStepsIDButtonToA < numStepsJDButtonToA) {
                                resultLastDirection = iDirection
                                jDSequence + iDSequence
                            } else if (numStepsIDButtonToA > numStepsJDButtonToA) {
                                resultLastDirection = jDirection
                                iDSequence + jDSequence
                            } else {
                                /*
                                // The below is not true for the numerical keypad.

                                // For the directional keypad, the case here can only be "^" and ">" actually, and they are equivalent, even in terms of produced control sequences.
                                assert(iDirection == Direction.Up && jDirection == Direction.Right)
                                // always prefer right first for it's possible that it doesn't change direction
                                */

                                // TODO The answer is correct after removing the judgement here! But why?
                                //if (iDirection == prevResultLastDirection) {
                                resultLastDirection = jDirection
                                iDSequence + jDSequence
                                /*} else {
                                    resultLastDirection = iDirection
                                    jDSequence + iDSequence
                                }*/
                            }
                        }
                }
            }

            acc.append(currentSequenceWithoutA).append('A')
            bestDirectionalControlSequence(acc, keypad, positionMap, c, resultLastDirection, from + 1)
        } else
            throw AssertionError()

    fun String.numericalBestDirectionalControlSequence() =
        bestDirectionalControlSequence(StringBuilder(), numericCharss, numericPositionMap, 'A', null, 0)

    fun String.directionalBestDirectionalControlSequence() =
        bestDirectionalControlSequence(StringBuilder(), directionalCharss, directionalPositionMap, 'A', null, 0)

    fun String.directionalTransforms(num: Int): String =
        if (num == 0)
            this
        else
            directionalBestDirectionalControlSequence()/*.also { println(it.length) }*/
                .directionalTransforms(num - 1)

    // WA
    fun part2(input: List<String>, numRobotDirectionalKeypads: Int): Int {
        val ans = input.sumOf {
            //println(it)

            val finalControlSequence =
                it.numericalBestDirectionalControlSequence()/*.also { println(it) }*/
                    .directionalTransforms(numRobotDirectionalKeypads)

            finalControlSequence.length/*.also { println(it) }*/ * it.removeSuffix("A").toInt()/*.also { println(it) }*/
        }

        return ans
    }

    // WA
    fun part2Optimized(input: List<String>, numRobotDirectionalKeypads: Int): Long {
        fun String.directionalBestDirectionalControlSequence(prevC: Char) =
            bestDirectionalControlSequence(StringBuilder(), directionalCharss, directionalPositionMap, prevC, null, 0)

        fun String.directionalTransforms(prevC: Char, num: Int): String =
            if (num == 0)
                this
            else
                directionalBestDirectionalControlSequence(prevC)/*.also { println(it.length) }*/
                    .directionalTransforms(num - 1) // do not pass `prevC` recursively but use `A`

        fun transformsMap(num: Int) = directionalChars.asSequence().flatMap { prevC ->
            directionalChars.asSequence().map { c ->
                (prevC to c) to c.toString().directionalTransforms(prevC, num)
            }
        }.toMap()

        val numHalf = numRobotDirectionalKeypads / 2
        val numOtherHalf = numRobotDirectionalKeypads - numHalf
        val halfTransforms = transformsMap(numHalf)
        val otherHalfTransforms = transformsMap(numOtherHalf)

        @Suppress("ConvertToStringTemplate")
        val ans = runBlocking {
            input.sumOf {
                val firstControlSequences = it.numericToDirectionalControlSequences().toList()
                //val minLength = firstControlSequences.minOf { it.length }
                val length = firstControlSequences
                    //.filter { it.length == minLength }
                    .minOf { firstControlSequence ->
                        //println(it)

                        val halfTransSequence = ('A' + firstControlSequence).asSequence().zipWithNext { prevC, c ->
                            halfTransforms.getValue(prevC to c)
                        }.joinToString("")
                        assert(halfTransSequence == firstControlSequence.directionalTransforms(numHalf))

                        ('A' + halfTransSequence).asSequence().zipWithNext { prevC, c ->
                            otherHalfTransforms.getValue(prevC to c).length.toLong()
                        }.sum()
                    }

                length/*.also { println(it) }*/ * it.removeSuffix("A").toLong()/*.also { println(it) }*/
            }
        }

        return ans
    }

    fun part2Optimized2(input: List<String>, numRobotDirectionalKeypads: Int): Long {
        fun minLengthControlSequences(n: Int): Map<Pair<Char, Char>, List<String>> =
            if (n == 0)
                (directionalChars cartesianProduct directionalChars).associateWith { (_, c) -> listOf(c.toString()) }
            else {
                val halfNSequences = minLengthControlSequences(n / 2)
                val doubleHalfNSequences = halfNSequences.mapValues {
                    it.value.flatMap { s ->
                        (sequenceOf('A') + s.asSequence()).zipWithNext { prevC, c ->
                            // This is not working, assuming each map value is a list of n elements and the length is l, the complexity is n ^ l.
                            halfNSequences[prevC to c]
                        }
                    }
                }

                TODO()
            }
        TODO()
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day21_test.txt` file:
    val testInput = readInput("Day21_test")
    check(measureTimeAndPrint { part1(testInput) } == 126384)

    // Read the input from the `src/Day21.txt` file.
    val input = readInput("Day21")
    measureTimeAndPrint { part1(input) }.println()

    check(part2(testInput, 2)/*.also { println(it) }*/ == 126384)

    // The length ratio after one transformation is about 2.5
    //part2(input, 10)
    // And the result length for an input will be about 1e11
    //part2(input, 25).println()


    check(part2Optimized(testInput, 2)/*.also { println(it) }*/ == 126384L)
    check(part2Optimized(input, 2)/*.also { println(it) }*/ == 128962L)

    part2Optimized(input, 25).also {
        println("Part 2 ans:")
    }.println()
}
