fun main() {
    class GateIn(val input1: String, val type: String, val input2: String)

    fun part1(input: List<String>): Long {
        val emptyLineIndex = input.indexOf("")
        val inputWires = input.subList(0, emptyLineIndex).map {
            it.split(": ").let { it[0] to (it[1].toInt() == 1) }
        }
        val gateIns = input.subList(emptyLineIndex + 1, input.size).map {
            it.split(" -> ").let {
                it[0].split(" ").let { GateIn(it[0], it[1], it[2]) } to it[1]
            }
        }

        val wireMap = inputWires.toMap().toMutableMap()
        val gateMapByOutputWire = gateIns.associateBy { it.second }.mapValues { it.value.first }
        fun getWireValue(wire: String): Boolean =
            if (wireMap.contains(wire))
                wireMap.getValue(wire)
            else {
                val gate = gateMapByOutputWire.getValue(wire)
                val input1Value = getWireValue(gate.input1)
                val input2Value = getWireValue(gate.input2)
                when (gate.type) {
                    "AND" -> input1Value and input2Value
                    "OR" -> input1Value or input2Value
                    "XOR" -> input1Value xor input2Value
                    else -> throw IllegalArgumentException()
                }.also {
                    wireMap[wire] = it
                }
            }

        for (gateIn in gateIns)
            getWireValue(gateIn.second)

                //println(wireMap)

        val ans = wireMap.entries.filter { it.key.startsWith("z") }.sumOf {
            (1L shl it.key.substring(1).toInt()) * if (it.value) 1 else 0
        }

        return ans
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day24_test.txt` file:
    val testInput = readInput("Day24_test")
    check(part1(testInput)/*.also { println(it) }*/ == 4L)

    // Read the input from the `src/Day24.txt` file.
    val input = readInput("Day24")
    part1(input).println()

    check(part2(testInput) == 1) // TODO note that the test input might be different
    part2(input).println()
}
