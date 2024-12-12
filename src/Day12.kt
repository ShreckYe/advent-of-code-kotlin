data class Position(val i: Int, val j: Int)
data class Region(/*val first: Position,*/ val area: Int, val perimeter: Int)

fun main() {
    fun part1(input: List<String>): Int {
        val parents = mutableMapOf<Position, Position>() // union find
        val rootRegions = mutableMapOf<Position, Region>()
        fun Position.findRoot(): Position {
            val parent = parents[this]
            return if (parent === null || parent == this) // Workaround here. I don't want to debug anymore.
                this
            else
                parent.findRoot().also {
                    parents[this] = it
                }
        }

        for ((i, line) in input.withIndex())
            for ((j, plot) in line.withIndex()) {
                val countedNeighbors =
                    listOf(Position(i - 1, j), Position(i, j - 1))
                        .mapNotNull { pos ->
                            if (input.getOrNull(pos.i)?.getOrNull(pos.j) == plot)
                                rootRegions[pos.findRoot()]!!.let { pos to it }
                            else null
                        }
                        .distinct()

                val countedNeighborRegions = countedNeighbors.map { it.second }
                val distinctCountedNeighborsByRoot =
                    countedNeighbors.map { it.first.findRoot() to it.second }.distinctBy { it.first }
                println("$i $j: $distinctCountedNeighborsByRoot")
                val distinctCountedNeighborRegions = distinctCountedNeighborsByRoot.map { it.second }

                //countedNeighbors.asSequence().map { regions[it]!! }
                val newRegion = Region(
                    //countedNeighborRegions.firstOrNull()?.first ?: Position(i, j),
                    distinctCountedNeighborRegions.sumOf { it.area } + 1,
                    distinctCountedNeighborRegions.sumOf { it.perimeter } + 4 - countedNeighborRegions.size * 2)

                //val region = regions.getOrDefault(plot, Region(0, 0))
                //println("$plot + ${ 4 - countedNeighbors} ${region.perimeter + 4 - countedNeighbors}")

                val rootsToMerge =
                    (distinctCountedNeighborsByRoot.map { it.first }+listOf(Position(i, j))).map { it.findRoot() }.distinct()
                val newRoot = rootsToMerge.first()
                rootsToMerge.drop(1).forEach {
                    parents[it] = newRoot
                    rootRegions.remove(it) // TODO try removing
                }
                println("$i $j newRoot=$newRoot newRegion=$newRegion")
                rootRegions[newRoot] = newRegion
            }
        println(parents)
        println(rootRegions)

        val allPoss = input.indices.flatMap { i -> input.first().indices.map { j -> Position(i, j) } }
        val allRoots = allPoss.map { it.findRoot() }.distinct()
        println(allRoots)
        println(allRoots.map { rootRegions.getValue(it) })

        //return rootRegions.values.distinct().sumOf { it.area * it.perimeter }
        return allRoots.sumOf { rootRegions.getValue(it).run { area * perimeter } }
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day12_test")
    check(part1(testInput).also { println(it) } == 140)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day12")
    part1(input).println()

    check(part2(testInput) == 1) // TODO note that the test input might be different
    part2(input).println()
}
