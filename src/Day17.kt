import kotlin.math.pow
import kotlin.streams.asStream

@JvmInline
value class `3BitInteger`(val uByte: UByte) {
    init {
        require(uByte in 0U until 8U)
    }

    override fun toString() =
        uByte.toString()
    /*
    fun equals(other: `3BitInteger`) =
        uByte == other.uByte
    */
}

sealed class ComboOperand {
    class Value(val value: `3BitInteger`) : ComboOperand()
    class Register(val index: Int) : ComboOperand()
}

fun String.to3BitInteger() =
    `3BitInteger`(toUByte())

fun Int.pow(n: Int) =
    toDouble().pow(n).toInt()

fun `3BitInteger`.toInt() =
    uByte.toInt()

fun `3BitInteger`.toLong() =
    uByte.toLong()

fun Int.to3BitInteger() =
    `3BitInteger`(toUByte())

fun Long.to3BitInteger() =
    `3BitInteger`(toUByte())

fun main() {
    fun processInput(input: List<String>): Pair<LongArray, List<`3BitInteger`>> {
        //val emptyLineIndex = input.indexOf("")
        val registers = input.asSequence().take(3).map { it.substringAfter(": ").toLong() }.toList().toLongArray()
        val program = input[4].substringAfter(": ").splitToSequence(',').map { it.to3BitInteger() }.toList()
        return registers to program
    }

    fun runProgram(
        registers: LongArray,
        program: List<`3BitInteger`>,
        onOutput: (`3BitInteger`) -> Boolean
    ): Boolean {
        //println("initial registers: ${registers.toList()}")
        //println("program: $program")

        var ip = 0
        //for ((opcode, literalOperand) in program.chunked(2)) {
        while (ip < program.size) {
            //println("IP: $ip")
            val opcode = program[ip]
            val literalOperand = program[ip + 1]
            //println("instruction pointer: $ip")
            //println("opcode: $opcode, operand number: $literalOperand")
            //println("registers: ${registers.toList()}")

            fun comboOperand() =
                when (val uByte = literalOperand.uByte) {
                    0.toUByte(), 1.toUByte(), 2.toUByte(), 3.toUByte() -> ComboOperand.Value(literalOperand)
                    4.toUByte(), 5.toUByte(), 6.toUByte() -> ComboOperand.Register(uByte.toInt() - 4)
                    7.toUByte() -> throw IllegalArgumentException()
                    else -> throw AssertionError()
                }

            fun comboOperandValue() =
                when (val comboOperand = comboOperand()) {
                    is ComboOperand.Value -> comboOperand.value.toLong()
                    is ComboOperand.Register -> registers[comboOperand.index]
                }

            var incIP = true
            fun dv() =
                registers[0] / 2.pow(comboOperandValue().toInt())
            when (opcode.uByte) {
                0.toUByte() -> registers[0] = dv()
                1.toUByte() -> registers[1] = registers[1] xor literalOperand.toLong()
                2.toUByte() -> registers[1] = comboOperandValue() % 8
                3.toUByte() -> {
                    val registerA = registers[0]
                    //println("Register `A` for jumping judging: $registerA")
                    if (registerA != 0L) {
                        ip = literalOperand.toInt()
                        incIP = false
                        //println("Jumping to $ip")
                    }
                }

                4.toUByte() -> registers[1] = registers[1] xor registers[2]
                5.toUByte() -> if (!onOutput(
                        (comboOperandValue() % 8).to3BitInteger()/*.also {
                        println("output: $it")
                    }*/
                    )
                ) return false

                6.toUByte() -> registers[1] = dv()
                7.toUByte() -> registers[2] = dv()
                else -> throw IllegalArgumentException()
            }
            if (incIP)
                ip += 2
        }
        return true
    }

    fun runProgram(
        registers: LongArray,
        program: List<`3BitInteger`>
    ): List<`3BitInteger`> {
        val outputs = mutableListOf<`3BitInteger`>()
        assert(runProgram(registers, program) {
            outputs.add(it)
            true
        })
        return outputs
    }

    fun part1(input: List<String>): String {
        val (registers, program) = processInput(input)
        return runProgram(registers, program).joinToString(",")
    }

    fun part2Search(input: List<String>): Int {
        val (registers, program) = processInput(input)
        var time = System.currentTimeMillis()
        // To see the debug output, don't use a parallel stream.
        val ans = (1..Int.MAX_VALUE).asSequence().asStream().parallel().filter { registerA ->
            val registerA = registerA.toLong()
            if (registerA % (1 shl 24) == 0L) {
                println("Register A: $registerA")
                val newTime = System.currentTimeMillis()
                println("Took time: ${newTime - time}")
                time = newTime
            }
            val registers = registers.copyOf().also { it[0] = registerA }
            //println(registers.toList())
            //println(program)

            //runProgram(registers, program) == program

            val programIterator = program.iterator()
            runProgram(registers, program) {
                programIterator.hasNext() &&
                        // `equals` not available for a value class
                        it.uByte == programIterator.next().uByte
                /*(it.uByte.also { println("Output: $it") } == programIterator.next().uByte.also { println("Program element: $it") }).also {
                    println("Comp result: $it")
                }*/
            } &&
                    !programIterator.hasNext()// make sure that all the elements are compared
        }
            //.first() // for `Sequence`
            .findFirst().get() // for `Stream`

        return ans
    }

    fun part2SearchWithMyPuzzleInputWithAnalysis(): Long {
        /*
        Program: 2,4,1,4,7,5,4,1,1,4,5,5,0,3,3,0

        initial registers: a, b, c
        Instruction 0 (2,4): a, a mod 8, c
        Instruction 1 (1,4): a, (a mod 8) xor 4, c
        Instruction 2 (7,5): a, (a mod 8) xor 4, a / (2 ^ ((a mod 8) xor 4))
        Instruction 3 (4,1): a, ((a mod 8) xor 4) xor (a / (2 ^ ((a mod 8) xor 4))), a / (2 ^ ((a mod 8) xor 4))
        Instruction 4 (1,4): a, ((a mod 8) xor 4) xor (a / (2 ^ ((a mod 8) xor 4))) xor 4 = (a mod 8) xor (a / (2 ^ ((a mod 8) xor 4))), a / (2 ^ ((a mod 8) xor 4))
        Instruction 5 (5,5): output "(a mod 8) xor (a / (2 ^ ((a mod 8) xor 4))) mod 8 = (a mod 8) xor (a shr ((a mod 8) xor 4) mod 8)"
        Instruction 6 (0,3): a / (2 ^ 3), (a mod 8) xor (a / (2 ^ ((a mod 8) xor 4))), a / (2 ^ ((a mod 8) xor 4))

          for a = 511: 63, 7 xor 63 = 56, a / (2 ^ (7 xor 4 = 3))) = 63 (manually verified correct)
          for a = 63: 7, 7 xor 7 = 0 , 7 (manually verified correct)
          for a = 7: 0, 7, 0 (manually verified correct)

          for a = 512: 64, 0 xor 32 = 32, 512 / 16 = 32 (manually verified correct)
        Instruction 7 (3,0): if a / (2 ^ 3) != 0 set IP to 0

        Therefore, the bit length of initial `a` is 46 to 48.

        Discovery: for the output, only `a`'s low 10 bits are involved as `(a mod 8) xor 4)` maximizes at 7, and only 3 bits out of them are involved, depending on `a mod 8`.
         */


        val bitFullSpace = listOf(false, true)
        val intialSpace = List<Boolean?>(48) { null }
        val program = listOf(2, 4, 1, 4, 7, 5, 4, 1, 1, 4, 5, 5, 0, 3, 3, 0)

        fun search(groupIndex: Int, space: List<Boolean?>): Long? =
            if (groupIndex >= 16) {
                //if (space.all { it !== null })
                println(space)
                if (space.subList(46, 48).any { b -> b!! })
                    space.withIndex().sumOf { (i, b) -> (if (b!!) 1L else 0L) shl i }
                else null
                //else null
            } else {
                // not used
                fun bitSpace(index: Int) =
                    space[index]?.let { listOf(it) } ?: bitFullSpace

                (0 until 8).firstNotNullOfOrNull { am8 ->
                    //println("groupIndex=$groupIndex, am8=$am8")

                    val mutableSpace = space.toMutableList()
                    fun checkAndSetSpace(m8: Int, startIndex: Int): Boolean {
                        val bits = (0 until 3).map { i -> m8 shr i and 1 == 1 }
                        bits.forEachIndexed { i, b ->
                            val index = startIndex + i
                            if (index < 48) {
                                val existing = mutableSpace[index]
                                if (existing === null)
                                    mutableSpace[index] = b
                                else if (existing != b)
                                    return false
                            }
                        }
                        return true
                    }

                    val startIndex = groupIndex * 3
                    if (!checkAndSetSpace(am8, startIndex)) return@firstNotNullOfOrNull null

                    val involvedUpper = program[groupIndex] xor am8
                    //println("programElement=${program[groupIndex]}, involvedUpper=$involvedUpper")
                    val involvedUpperStartIndex = startIndex + (am8 xor 4)
                    if (!checkAndSetSpace(involvedUpper, involvedUpperStartIndex)) return@firstNotNullOfOrNull null

                    search(groupIndex + 1, mutableSpace)
                }
            }

        return search(0, intialSpace)!!.also { registerA ->
            println(registerA.toString(2))
            val output = runProgram(longArrayOf(registerA, 0, 0), program.map { it.to3BitInteger() }).map { it.toInt() }
            println(output)
            check(output == program)
        }
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day17_test.txt` file:
    val testInput = readInput("Day17_test")
    check(part1(testInput)/*.also { println(it) }*/ == "4,6,3,5,6,3,5,2,1,0")

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day17")
    part1(input).println()

    val testInput2 = readInput("Day17_test2")
    //check(part2Search(testInput2).also { println(it) } == 117440)
    println("Check passed.")
    // On my computer it takes 0.5s to search 2 ^ 24 runs, so for 2 ^ 48 runs it's impossible to search.
    //part2Search(input).println()
    part2SearchWithMyPuzzleInputWithAnalysis().println()
}
