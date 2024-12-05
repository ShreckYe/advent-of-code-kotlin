fun main() {
    fun hasViolation(
        update: List<Int>,
        ruleSet: Set<Pair<Int, Int>>
    ) = update.withIndex().any { (i, a) ->
        update.subList(i + 1, update.size).any { b ->
            (b to a) in ruleSet
        }
    }

    fun List<Int>.median( ): Int {
        val size = size
        require(size % 2 == 1)
        return this[size / 2]
    }

    fun part1(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val rules = input.subList(0, emptyLineIndex).map { it.splitToSequence('|').map { it.toInt() }.toList() }
        val updates =
            input.subList(emptyLineIndex + 1, input.size).map { it.splitToSequence(',').map { it.toInt() }.toList() }

        val ruleSet = rules.asSequence().map { it[0] to it[1] }.toSet()
        val ans = updates.sumOf { update ->
            val hasViolation = hasViolation(update, ruleSet)
            if (hasViolation) 0
            else update.median()
        }
        return ans
    }

    fun part2(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val rules = input.subList(0, emptyLineIndex).map { it.splitToSequence('|').map { it.toInt() }.toList() }
        val updates =
            input.subList(emptyLineIndex + 1, input.size).map { it.splitToSequence(',').map { it.toInt() }.toList() }

        val ruleSet = rules.asSequence().map { it[0] to it[1] }.toSet()
        val ans = updates.sumOf { update ->
            if (hasViolation(update, ruleSet)) {
                val resultSegs = mutableListOf<MutableList<Int>>()
                for (a in update) {
                    fun block() {
                        for (seg in resultSegs) {
                            seg.forEachIndexed { i, segE ->
                                if (a to segE in ruleSet) {
                                    seg.add(i, a)
                                    return
                                }
                            }
                            seg.withIndex().reversed().forEach { (i, segE) ->
                                if (segE to a in ruleSet) {
                                    seg.add(i + 1, a)
                                    return
                                }
                            }
                        }
                        resultSegs.addLast(mutableListOf(a))
                    }
                    block()
                }

                resultSegs.single()/*.also { println("Result seg: " + it) }*/.median()
            } else 0
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 143)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day05")
    part1(input).println()

    check(part2(testInput)/*.also { println(it) }*/ == 123)
    part2(input).println()
}
