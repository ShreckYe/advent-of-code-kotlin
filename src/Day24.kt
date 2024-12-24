/*enum class AdderIntermediaryOutputType {
    Sum, Carry
}*/

enum class AdderOutputType {
    TwoSum, Carry, Sum
}

// TODO `name` -> `wireName`
sealed class AdderOutput(val wire: String, val index: Int) {
    /*data*/ class TwoSum(wire: String, index: Int) : AdderOutput(wire, index)

    /**
     * @param index for the input
     */
    class TwoSumCarry(wire: String, index: Int) : AdderOutput(wire, index)


    abstract class ThreeSumCommon(wire: String, index: Int, val twoSum: TwoSum, val prevCarry: Carry?) :
        AdderOutput(wire, index) {
        init {
            require(index == twoSum.index)
            if (index != 0) {
                require(prevCarry !== null)
                require(index == prevCarry!!.index + 1)
            }
        }
    }

    class ThreeSum(wire: String, index: Int, twoSum: TwoSum, prevCarry: Carry?) :
        ThreeSumCommon(wire, index, twoSum, prevCarry)

    class ThreeSumCarry(wire: String, index: Int, twoSum: TwoSum, prevCarry: Carry?) :
        ThreeSumCommon(wire, index, twoSum, prevCarry)


    class Carry(wire: String, index: Int, twoSumCarry: TwoSumCarry, threeSumCarry: ThreeSumCarry) :
        AdderOutput(wire, index) {
        init {
            require(index == twoSumCarry.index)
            require(index == threeSumCarry.index)
        }
    }
}

//data class AdderOutput(val type: AdderOutputType, val name: Int, val index: Int)

fun main() {
    class GateIn(val input1: String, val type: String, val input2: String)
    class Gate(val gateIn: GateIn, val output: String)

    fun part1(input: List<String>): Long {
        val emptyLineIndex = input.indexOf("")
        val inputWires = input.subList(0, emptyLineIndex).map {
            it.split(": ").let { it[0] to (it[1].toInt() == 1) }
        }
        val gates = input.subList(emptyLineIndex + 1, input.size).map {
            it.split(" -> ").let {
                it[0].split(" ").let { GateIn(it[0], it[1], it[2]) } to it[1]
            }
        }

        val wireMap = inputWires.toMap().toMutableMap()
        val gateMapByOutputWire = gates.associateBy { it.second }.mapValues { it.value.first }
        fun getWireValue(wire: String): Boolean =
            if (wireMap.contains(wire))
                wireMap.getValue(wire)
            else {
                val gateIn = gateMapByOutputWire.getValue(wire)
                val input1Value = getWireValue(gateIn.input1)
                val input2Value = getWireValue(gateIn.input2)
                when (gateIn.type) {
                    "AND" -> input1Value and input2Value
                    "OR" -> input1Value or input2Value
                    "XOR" -> input1Value xor input2Value
                    else -> throw IllegalArgumentException()
                }.also {
                    wireMap[wire] = it
                }
            }

        for (gateIn in gates)
            getWireValue(gateIn.second)

        //println(wireMap)

        val ans = wireMap.entries.filter { it.key.startsWith("z") }.sumOf {
            (1L shl it.key.substring(1).toInt()) * if (it.value) 1 else 0
        }

        return ans
    }

    val inputRegex = Regex("[xy]\\d\\d")

    fun part2(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val inputWires = input.subList(0, emptyLineIndex).map {
            it.split(": ").let { it[0] to (it[1].toInt() == 1) }
        }
        val gates = input.subList(emptyLineIndex + 1, input.size).map {
            it.split(" -> ").let {
                it[0].split(" ").let { GateIn(it[0], it[1], it[2]) } to it[1]
            }
        }

        /*
        val output2SumIndexMap = mutableMapOf<String, Int>()
        val outputCarryIndexMap = mutableMapOf<String, Int>()
        */
        //val intermediaryOutputMap = mutableMapOf<String, Pair<AdderIntermediaryOutputType, Int>>()
        //val outputSumIndexMap = mutableMapOf<String, Int>()

        //val adderOutputMap = Array<EnumMap<AdderOutputType, AdderOutput>>(46) {}
        val adderOutputMap = mutableMapOf<String, AdderOutput>()
        val gateMapByOutputWire = gates.associateBy { it.second }.mapValues { it.value.first }
        fun getAdderOutput(wire: String): AdderOutput =
            if (adderOutputMap.contains(wire))
                adderOutputMap.getValue(wire)
            else {
                val gateIn = gateMapByOutputWire.getValue(wire)
                val input1 = gateIn.input1
                val input2 = gateIn.input2
                fun requireInputAndGetIndex(): Int {
                    require(input2 matches inputRegex)
                    val index1 = input1.substring(1).toInt()
                    val index2 = input2.substring(1).toInt()
                    require(index1 == index2)

                    return index1
                }

                fun input1Output() = getAdderOutput(input1)
                fun input2Output() = getAdderOutput(input2)
                when (gateIn.type) {
                    "XOR" ->
                        if (input1 matches inputRegex) {
                            val index = requireInputAndGetIndex()
                            AdderOutput.TwoSum(wire, index)
                        } else {
                            val twoSum = input1Output() as AdderOutput.TwoSum
                            val prevCarry = input2Output() as AdderOutput.Carry
                            AdderOutput.ThreeSum(wire, twoSum.index, twoSum, prevCarry)
                            // TODO null?
                        }

                    "AND" ->
                        if (input1 matches inputRegex) {
                            val index = requireInputAndGetIndex()
                            AdderOutput.TwoSumCarry(wire, index)
                        } else {
                            val twoSum = input1Output() as AdderOutput.TwoSum
                            val prevCarry = input2Output() as AdderOutput.Carry
                            AdderOutput.ThreeSumCarry(wire, twoSum.index, twoSum, prevCarry)
                        }

                    "OR" -> {
                        val twoSumCarry = input1Output() as AdderOutput.TwoSumCarry
                        val threeSumCarry = input2Output() as AdderOutput.ThreeSumCarry
                        AdderOutput.Carry(wire, twoSumCarry.index, twoSumCarry, threeSumCarry)
                    }

                    else -> throw IllegalArgumentException()
                }.also {
                    adderOutputMap[wire] = it
                }
            }

        for (gate in gates)
            getAdderOutput(gate.second)

        val bitSums = adderOutputMap.values.mapNotNull { it as? AdderOutput.ThreeSum }

        return TODO()
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
