import Direction.*
import java.util.*

fun main() {
    data class Region(/*val first: Position,*/ val area: Int, val perimeter: Int)

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
                //println("$i $j: $distinctCountedNeighborsByRoot")
                val distinctCountedNeighborRegions = distinctCountedNeighborsByRoot.map { it.second }

                //countedNeighbors.asSequence().map { regions[it]!! }
                val newRegion = Region(
                    //countedNeighborRegions.firstOrNull()?.first ?: Position(i, j),
                    distinctCountedNeighborRegions.sumOf { it.area } + 1,
                    distinctCountedNeighborRegions.sumOf { it.perimeter } + 4 - countedNeighborRegions.size * 2)

                //val region = regions.getOrDefault(plot, Region(0, 0))
                //println("$plot + ${ 4 - countedNeighbors} ${region.perimeter + 4 - countedNeighbors}")

                val rootsToMerge =
                    (distinctCountedNeighborsByRoot.map { it.first } + listOf(Position(i, j))).map { it.findRoot() }
                        .distinct()
                val newRoot = rootsToMerge.first()
                rootsToMerge.drop(1).forEach {
                    parents[it] = newRoot
                    rootRegions.remove(it) // TODO try removing
                }
                //println("$i $j newRoot=$newRoot newRegion=$newRegion")
                rootRegions[newRoot] = newRegion
            }
        //println(parents)
        //println(rootRegions)

        val allPoss = input.indices.flatMap { i -> input.first().indices.map { j -> Position(i, j) } }
        val allRoots = allPoss.map { it.findRoot() }.distinct()
        //println(allRoots)
        //println(allRoots.map { rootRegions.getValue(it) })

        //return rootRegions.values.distinct().sumOf { it.area * it.perimeter }
        return allRoots.sumOf { p ->
            rootRegions.getValue(p).run {
                (area * perimeter)/*.also {
                    println("${with(p) { input[i][j] }}: $area * $perimeter = $it")
                }*/
            }
        }//.also { println() }
    }

    data class PlotSide(val p: Position, val d: Direction)

    fun part2(input: List<String>): Int {
        val m = input.size
        val n = input.first().length
        val positions = (0 until m).flatMap { i ->
            (0 until n).map { j -> Position(i, j) }
        }

        val p10 = Position(1, 0) // for debugging

        val plotUnionFind = object : UnionFind<Position> {
            val parents = Array(m) { Array<Position?>(n) { null } }

            override var Position.parent: Position?
                get() = parents[i][j].also {
                    /*if (this == p10)
                        println("$this's parent: $it")*/
                }
                set(value) {
                    //println("Set $this's parent to $value")
                    /*if (this == p10 && value == p10)
                        throw Throwable() //Thread.dumpStack()*/
                    parents[i][j] = value
                }
            override val allElements: Collection<Position>
                get() = positions
        }

        for ((i, line) in input.withIndex())
            for ((j, plot) in line.withIndex()) {
                val p = Position(i, j)

                for (neighborP in listOf(Position(i - 1, j), Position(i, j - 1)))
                    if (input.getOrNull(neighborP.i)?.getOrNull(neighborP.j) == plot)
                        plotUnionFind.unionIfNeeded(neighborP, p)
            }

        //println("$p10's parent before getting all elements: ${with(plotUnionFind) { p10.parent }}")

        val ans = plotUnionFind.sets().sumOf { ps ->
            // see commit 2fc3feb3538b758957271330d02799a4d942ff75 for an incorrect solution using a union find here

            val positionSetsBySideDirectionByPosition =
                Array(m) { Array<EnumMap<Direction, MutableSet<Position>>?>(n) { null } }.apply {
                    for (p in ps)
                        this[p.i][p.j] = EnumMap<Direction, MutableSet<Position>>(Direction::class.java).also { map ->
                            for (d in Direction.entries)
                                map[d] = mutableSetOf(p)
                        }
                }

            for (p in ps/*.sortedBy { it.posSum() }*/ /* This doesn't work. */) {
                val sideSetsByDirection = positionSetsBySideDirectionByPosition[p.i][p.j]!!

                for (neighborDirection in Direction.smallerDirections) {
                    val neighborP = p + neighborDirection.diff
                    val neighborSideSetsByDirection =
                        with(neighborP) { positionSetsBySideDirectionByPosition.getOrNull(i)?.getOrNull(j) }

                    if (neighborSideSetsByDirection !== null) {
                        fun removeAndUnion(
                            neighborConnectingSideDirection: Direction,
                            pConnectingSideDirection: Direction,
                            commonSideDirections: List<Direction>
                        ) {
                            if (neighborConnectingSideDirection in neighborSideSetsByDirection) {
                                neighborSideSetsByDirection[neighborConnectingSideDirection]!!.remove(neighborP)
                                neighborSideSetsByDirection.remove(neighborConnectingSideDirection)
                                sideSetsByDirection[pConnectingSideDirection]!!.remove(p)
                                sideSetsByDirection.remove(pConnectingSideDirection)

                                for (commonSideDirection in commonSideDirections) {
                                    val neighborCommonPlotSide = PlotSide(neighborP, commonSideDirection)
                                    neighborSideSetsByDirection[commonSideDirection]?.let { set ->
                                        sideSetsByDirection[commonSideDirection]?.let {
                                            set.add(p)
                                            sideSetsByDirection[commonSideDirection] = set
                                        }
                                    }
                                }
                            }
                        }

                        when (neighborDirection) {
                            Up -> removeAndUnion(Down, Up, listOf(Left, Right))
                            Left -> removeAndUnion(Right, Left, listOf(Up, Down))
                            else -> throw AssertionError()
                        }
                    }
                }
            }

            val sideSets = positionSetsBySideDirectionByPosition.flatMap {
                it.asSequence().filterNotNull().flatMap { it.map { it.toPair() } }
            }.distinct()

            val splitCountsOfSideSets = sideSets.map {
                it.second.asSequence().sorted().zipWithNext().count { (a, b) ->
                    ((b.i - a.i) + (b.j - a.j) != 1)/*.also {
                        if (it)
                            println("Split: $a $b")
                    }*/
                } + 1
            }

            val price = ps.size * splitCountsOfSideSets.sum()

            //println("Side sets with ${with(ps.first()) { input[i][j] }} and price $price: $sideSets")
            price
        }
        //println()

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 140)
    val testInput4 = readInput("Day12_test4")
    check(part1(testInput4) == 1930)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day12")
    part1(input).println()

    check(part2(testInput) == 80)
    val testInput2 = readInput("Day12_test2")
    check(part2(testInput2) == 236)
    val testInput3 = readInput("Day12_test3")
    check(part2(testInput3) == 368)
    check(part2(testInput4)/*.also { println(it) }*/ == 1206)

    part2(input).println()
}
