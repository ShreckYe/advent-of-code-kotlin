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
    data class GateIn(val input1: String, val type: String, val input2: String) {
        val inputSet = setOf(input1, input2)
    }

    data class Gate(val gateIn: GateIn, val output: String)

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

    fun part2Incorrect(input: List<String>): Int {
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

    @Suppress("UNCHECKED_CAST")
    fun part2(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val inputWires = input.subList(0, emptyLineIndex).map {
            it.split(": ").let { it[0] to (it[1].toInt() == 1) }
        }
        val gates = input.subList(emptyLineIndex + 1, input.size).map {
            it.split(" -> ").let {
                Gate(it[0].split(" ").let { GateIn(it[0], it[1], it[2]) }, it[1])
            }
        }

        /*
        Regexes used:
        find carries for inputs: .\d\d XOR .\d\d
        check if their numbers are the same: .(\d\d) XOR .\1
        find sums for inputs: .\d\d AND .\d\d
        check if their numbers are the same: .(\d\d) AND .\1
         */

        var swapped = mutableListOf<String>()

        val firstLayer = gates.filter {
            it.gateIn.input1 matches inputRegex && it.gateIn.input2 matches inputRegex
        }

        println(firstLayer)

        // There are no cases such as "x00 XOR y01" as checked by the regexes above.
        fun GateIn.inputIndex() =
            input1.substring(1).toInt().also {
                assert(it == input2.substring(1).toInt())
            }


        //val twoSums = firstLayer.filter { it.gateIn.type == "XOR" }.associate { it.output to it.gateIn.inputIndex() }
        //val twoSumCarries = firstLayer.filter { it.gateIn.type == "AND" }.associate { it.output to it.gateIn.inputIndex() }
        //val firstLayerWires = twoSums.keys + twoSumCarries.keys

        val twoSums =
            firstLayer.asSequence()
                .filter { it.gateIn.type == "XOR" }
                .map { it.output to it.gateIn.inputIndex() }
                .toList()
        val twoSumCarries =
            firstLayer.asSequence()
                .filter { it.gateIn.type == "AND" }
                .associate { it.output to it.gateIn.inputIndex() }
                .toList()

        println(twoSums.size)
        println(twoSumCarries.size)
        assert(twoSums.size == 45 && twoSumCarries.size == 45)
        //println(firstLayerWires)

        /*
        val secondLayer = gates.filter {
            it.gateIn.input1 in firstLayerWires || it.gateIn.input2 in firstLayerWires
        }


        secondLayer.filter { it.gateIn.input1 !in firstLayerWires }.println()
        secondLayer.filter { it.gateIn.input2 !in firstLayerWires }.println()
        secondLayer.filter { it.gateIn.input1 !in firstLayerWires }
        */

        val remainingGates = (gates - firstLayer.toSet()).groupBy { it.gateIn.type }
        val threeSumGates = remainingGates.getValue("XOR")
        val threeSumCarryGates = remainingGates.getValue("AND")
        val carryGates = remainingGates.getValue("OR")

        /*
        val carries = remainingGates.getValue("OR")
        carries.filter {  }

        val threeSums = remainingGates.getValue("XOR")
        threeSums.filter {
            val gateIn = it.gateIn
            gateIn.input1
        }
        */


        //val remainingGateMap = remainingGates.mapValues { it.value.associateBy { it.output } }
        val twoSumOutputWires = arrayOfNulls<String>(45).also {
            for (twoSum in twoSums) {
                it[twoSum.second] = twoSum.first
            }
        } as Array<String>
        val twoSumCarryOutputWires = arrayOfNulls<String>(45).also {
            for (twoSumCarry in twoSumCarries) {
                it[twoSumCarry.second] = twoSumCarry.first
            }
        } as Array<String>

        val threeSumOutputWires = arrayOfNulls<String>(45)
        val threeSumCarryOutputWires = arrayOfNulls<String>(45)
        val carryOutputWires = arrayOfNulls<String>(45)

        threeSumOutputWires[0] = twoSumOutputWires[0]
        threeSumCarryOutputWires[0] = null
        carryOutputWires[0] = twoSumCarryOutputWires[0]

        for (i in 1 until 45) {
            var twoSumOutputWire = twoSumOutputWires[i]
            var preCarryOutputWire = carryOutputWires[i - 1]!!
            fun threeSumInputSet() = setOf(twoSumOutputWire, preCarryOutputWire)
            var threeSumInputSet = threeSumInputSet()

            var threeSumGate = threeSumGates.firstOrNull { it.gateIn.inputSet == threeSumInputSet }
            //var threeSumCarryGate = threeSumCarryGates.firstOrNull { it.gateIn.inputSet == threeSumInputSet }

            val expectedZWire = "z${i.toString().padStart(2, '0')}"

            if (threeSumGate === null /*&& threeSumCarryGate === null*/) {
                val threeSumGate = threeSumGates.single { twoSumOutputWire in it.gateIn.inputSet }
                if (threeSumGate.output == expectedZWire) {
                    val corrected = (threeSumGate.gateIn.inputSet - twoSumOutputWire).single()

                    swapped.add(preCarryOutputWire)
                    swapped.add(corrected)

                    carryOutputWires[i - 1] = corrected
                    preCarryOutputWire = corrected
                } else {
                    val threeSumGate = threeSumGates.single { preCarryOutputWire in it.gateIn.inputSet }
                    if (threeSumGate.output == expectedZWire) {
                        val corrected = (threeSumGate.gateIn.inputSet - preCarryOutputWire).single()

                        swapped.add(twoSumOutputWire)
                        swapped.add(corrected)

                        twoSumOutputWires[i] = corrected
                        twoSumOutputWire = corrected
                    }
                }
            }

            threeSumInputSet = threeSumInputSet()
            threeSumGate = threeSumGates.single { it.gateIn.inputSet == threeSumInputSet }
            if (threeSumGate.output != expectedZWire) {
                swapped.add(threeSumGate.output)
                swapped.add(expectedZWire)
            }

            threeSumOutputWires[i] = expectedZWire

            val threeSumCarryGate = threeSumCarryGates.single { it.gateIn.inputSet == threeSumInputSet }
            threeSumCarryOutputWires[i] = threeSumCarryGate.output

            val carryGate = carryGates.firstOrNull { twoSumCarryOutputWires[i] in it.gateIn.inputSet }
            // copied and not completely adapted yet
            if (carryGate === null) {
                val threeSumGate = threeSumGates.single { twoSumOutputWire in it.gateIn.inputSet }
                if (threeSumGate.output == expectedZWire) {
                    val corrected = (threeSumGate.gateIn.inputSet - twoSumOutputWire).single()

                    swapped.add(preCarryOutputWire)
                    swapped.add(corrected)

                    carryOutputWires[i - 1] = corrected
                    preCarryOutputWire = corrected
                } else {
                    val threeSumGate = threeSumGates.single { preCarryOutputWire in it.gateIn.inputSet }
                    if (threeSumGate.output == expectedZWire) {
                        val corrected = (threeSumGate.gateIn.inputSet - preCarryOutputWire).single()

                        swapped.add(twoSumOutputWire)
                        swapped.add(corrected)

                        twoSumOutputWires[i] = corrected
                        twoSumOutputWire = corrected
                    }
                }
            }


            remainingGates.getValue("OR")
        }

        return TODO()
    }

    fun part2Manually(input: List<String>): String {
        val emptyLineIndex = input.indexOf("")
        val inputWires = input.subList(0, emptyLineIndex).map {
            it.split(": ").let { it[0] to (it[1].toInt() == 1) }
        }

        var gates = input.subList(emptyLineIndex + 1, input.size).map {
            it.split(" -> ").let {
                Gate(it[0].split(" ").let { GateIn(it[0], it[1], it[2]) }, it[1])
            }
        }

        fun firstLayer() = gates.filter {
            it.gateIn.input1 matches inputRegex && it.gateIn.input2 matches inputRegex
        }

        fun GateIn.inputIndex() =
            input1.substring(1).toInt().also {
                assert(it == input2.substring(1).toInt())
            }


        fun twoSumByOutputWire() =
            firstLayer().filter { it.gateIn.type == "XOR" }.associate { it.output to it.gateIn.inputIndex() }

        fun twoSumCarryByOutputWire() =
            firstLayer().filter { it.gateIn.type == "AND" }.associate { it.output to it.gateIn.inputIndex() }

        fun firstLayerWires() = twoSumByOutputWire().keys + twoSumCarryByOutputWire().keys

        fun remainingGates() = gates - firstLayer().toSet()
        fun remainingGateMap() = remainingGates().groupBy { it.gateIn.type }
        fun threeSumGates() = remainingGateMap().getValue("XOR")
        fun threeSumCarryGates() = remainingGateMap().getValue("AND")
        fun carryGates() = remainingGateMap().getValue("OR")

        fun String.renameIfPossible(wireIndices: Map<String, Int>, prefix: String) =
            wireIndices[this]?.let { "$prefix${it.toString().padStart(2, '0')}($this)" } ?: this

        fun String.renameTwoSumIfPossible() =
            renameIfPossible(twoSumByOutputWire(), "2sum")

        fun printThreeSums() =
            println(threeSumGates().sortedBy { it.output }.joinToString("\n", postfix = "\n") {
                it.gateIn.input1.renameTwoSumIfPossible() + " XOR " + it.gateIn.input2.renameTwoSumIfPossible() + " -> " + it.output
            })
        printThreeSums()

        val swapPairs0 = listOf(
            "fbq" to "z36", "pbv" to "z16", "qqp" to "z23"
        )

        fun List<Gate>.swap(swapPairs: List<Pair<String, String>>) =
            map { gate ->
                swapPairs.firstOrNull { it.first == gate.output }?.let {
                    gate.copy(output = it.second)
                } ?: swapPairs.firstOrNull { it.second == gate.output }?.let {
                    gate.copy(output = it.first)
                } ?: gate
            }

        gates = gates.swap(swapPairs0)
        println("Swapped first time")
        printThreeSums()

        println(twoSumByOutputWire().filter { it.value == 11 })

        val swapPair1 = "ncw" to "qff" /* 11 */

        gates = gates.swap(listOf(swapPair1))
        println("Swapped second time")

        printThreeSums()

        fun printThreeSumCarries() =
            println(threeSumCarryGates().map {
                it.copy(
                    gateIn = it.gateIn.copy(
                        input1 = it.gateIn.input1.renameTwoSumIfPossible(),
                        input2 = it.gateIn.input2.renameTwoSumIfPossible()
                    )
                )
            }.sortedBy { it.gateIn.inputSet.single { it.startsWith("2") } }.joinToString("\n", postfix = "\n") {
                it.gateIn.input1 + " AND " + it.gateIn.input2 + " -> " + it.output
            })
        printThreeSumCarries()

        fun String.renameTwoSumCarryIfPossible() =
            renameIfPossible(twoSumCarryByOutputWire(), "2sumCarry")


        fun threeSumCarryByOutputWire(): Map<String, Int> {
            val twoSumByOutputWire = twoSumByOutputWire()
            return threeSumCarryGates().mapNotNull { gate ->
                twoSumByOutputWire[gate.gateIn.input1]?.let {
                    gate.output /*gate.gateIn.input2*/ to it
                } ?: twoSumByOutputWire[gate.gateIn.input2]?.let {
                    gate.output /*gate.gateIn.input1*/ to it
                }
            }.toMap()
        }

        fun String.renameThreeSumCarryIfPossible() =
            renameIfPossible(threeSumCarryByOutputWire(), "3sumCarry")

        fun printCarries() =
            println(carryGates().map {
                it.copy(
                    gateIn = it.gateIn.copy(
                        input1 = it.gateIn.input1.renameTwoSumCarryIfPossible().renameThreeSumCarryIfPossible(),
                        input2 = it.gateIn.input2.renameTwoSumCarryIfPossible().renameThreeSumCarryIfPossible()
                    )
                )
            }.sortedBy { it.gateIn.inputSet.min() }.joinToString("\n", postfix = "\n") {
                it.gateIn.input1 + " OR " + it.gateIn.input2 + " -> " + it.output
            })
        printCarries()


        val swapPair2 = "qff" to "qnw"
        gates = gates.swap(listOf(swapPair2))
        println("Swapped third time")
        printThreeSums()
        printThreeSumCarries()
        printCarries()


        val swapPairs = swapPairs0 /*+ swapPair1*/ + swapPair2
        return swapPairs.flatMap { it.toList() }.sorted().joinToString(",")
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day24_test.txt` file:
    val testInput = readInput("Day24_test")
    check(part1(testInput)/*.also { println(it) }*/ == 4L)

    // Read the input from the `src/Day24.txt` file.
    val input = readInput("Day24")
    part1(input).println()

    //check(part2(testInput) == 1)
    part2Manually(input).println()
}
