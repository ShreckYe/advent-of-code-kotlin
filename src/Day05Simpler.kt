fun main() {
    fun List<Int>.median(): Int {
        val size = size
        require(size % 2 == 1)
        return this[size / 2]
    }
    fun List<Int>.sortedUpdate(ruleSet: Set<Pair<Int,Int>>)=
    sortedWith { a, b -> if (a to b in ruleSet) -1 else if (b to a in ruleSet) 1 else throw AssertionError() }
    fun part1(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val rules = input.subList(0, emptyLineIndex).map { it.splitToSequence('|').map { it.toInt() }.toList() }
        val updates =
            input.subList(emptyLineIndex + 1, input.size).map { it.splitToSequence(',').map { it.toInt() }.toList() }

        // not actually superior to `ruleSet` below
        val ruleMatrix: Map<Int, Map<Int, Int>> = mutableMapOf<Int, MutableMap<Int, Int>>().apply {
            for (rule in rules) {
                getOrPut(rule[0]) { mutableMapOf() }[rule[1]] = -1
                getOrPut(rule[1]) { mutableMapOf() }[rule[0]] = 1
            }
        }

        val ruleSet = rules.asSequence().map { it[0] to it[1] }.toSet()
        val ans = updates.sumOf { update ->
            val sortedUpdate = update.sortedUpdate(ruleSet)
            if (update == sortedUpdate)
                update.median()
            else 0
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
            val sortedUpdate = update.sortedUpdate(ruleSet)
            if (update != sortedUpdate)
                sortedUpdate.median()
            else 0
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
