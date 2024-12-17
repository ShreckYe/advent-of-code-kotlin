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

fun Int.to3BitInteger() =
    `3BitInteger`(toUByte())

fun main() {
    fun processInput(input: List<String>): Pair<IntArray, List<`3BitInteger`>> {
        //val emptyLineIndex = input.indexOf("")
        val registers = input.asSequence().take(3).map { it.substringAfter(": ").toInt() }.toList()
            .toIntArray() // TODO `BigInteger` instead?
        val program = input[4].substringAfter(": ").splitToSequence(',').map { it.to3BitInteger() }.toList()
        return Pair(registers, program)
    }

    fun runProgram(
        registers: IntArray,
        program: List<`3BitInteger`>,
        onOutput: (`3BitInteger`) -> Boolean
    ): Boolean {
        var ip = 0
        //for ((opcode, literalOperand) in program.chunked(2)) {
        while (ip < program.size) {
            //println("IP: $ip")
            val opcode = program[ip]
            val literalOperand = program[ip + 1]

            fun comboOperand() =
                when (val uByte = literalOperand.uByte) {
                    0.toUByte(), 1.toUByte(), 2.toUByte(), 3.toUByte() -> ComboOperand.Value(literalOperand)
                    4.toUByte(), 5.toUByte(), 6.toUByte() -> ComboOperand.Register(uByte.toInt() - 4)
                    7.toUByte() -> throw IllegalArgumentException()
                    else -> throw AssertionError()
                }

            fun comboOperandValue() =
                when (val comboOperand = comboOperand()) {
                    is ComboOperand.Value -> comboOperand.value.toInt()
                    is ComboOperand.Register -> registers[comboOperand.index]
                }

            var incIP = true
            fun dv() =
                registers[0] / 2.pow(comboOperandValue())
            when (opcode.uByte) {
                0.toUByte() -> registers[0] = dv()
                1.toUByte() -> registers[1] = registers[1] xor literalOperand.toInt()
                2.toUByte() -> registers[1] = comboOperandValue() % 8
                3.toUByte() -> {
                    val aRegister = registers[0]
                    if (aRegister != 0) {
                        ip = literalOperand.toInt()
                        incIP = false
                    }
                }

                4.toUByte() -> registers[1] = registers[1] xor registers[2]
                5.toUByte() -> if (!onOutput((comboOperandValue() % 8).to3BitInteger())) return false
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
        registers: IntArray,
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

    fun part2(input: List<String>): Int {
        val (registers, program) = processInput(input)
        val ans = (1..Int.MAX_VALUE).asSequence().asStream().parallel().filter { registerA ->
            if (registerA % 100000000 == 0)
                println("Register A: $registerA")
            val registers = registers.copyOf().also { it[0] = registerA }
            //println(registers.toList())
            //println(program)
            val programIterator = program.iterator()
            runProgram(registers, program) {
                // `equals` not available for a value class
                (it.uByte/*.also { println("Output: $it") }*/ == programIterator.next().uByte/*.also { println("Program element: $it") }*/)/*.also {
                    println("Comp result: $it")
                }*/
            } &&
                    !programIterator.hasNext()// make sure that all the elements are compared
        }
            //.first() // for `Sequence`
            .findFirst().get() // for `Stream`

        return ans
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
    check(part2(testInput2).also { println(it) } == 117440)
    println("Check passed.")
    part2(input).println()
}
