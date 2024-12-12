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
        return allRoots.sumOf { rootRegions.getValue(it).run { area * perimeter } }
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
                    if (this == p10)
                        println("$this's parent: $it")
                }
                set(value) {
                    println("Set $this's parent to $value")
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

        println("$p10's parent before getting all elements: ${with(plotUnionFind) { p10.parent }}")

        val ans = plotUnionFind.sets().sumOf { ps ->
            val firstP = ps.first()
            println("Set with ${input[firstP.i][firstP.j]}")

            println(ps)
            println(ps.sorted())
            assert(ps.sorted() == ps)

            val sideUnionFind = object : UnionFind<PlotSide> {
                /**
                 * Whether it's in the map indicates whether it's counted, whether it's not null indicates whether it has a parent.
                 */
                val parents = Array(m) { Array<EnumMap<Direction, PlotSide?>?>(n) { null } }.apply {
                    for (p in ps)
                        this[p.i][p.j] = EnumMap<Direction, PlotSide?>(Direction::class.java)
                }

                fun has(plotSide: PlotSide) =
                    with(plotSide) {
                        println("Has $plotSide ${input[p.i][p.j]}?")
                        //parents[p.i][p.j]!!.let { d in it } ?: false
                        d in parents[p.i][p.j]!!
                    }

                /*
                val existingParents = object {
                    operator fun get(plotSide: PlotSide): PlotSide? = with(plotSide) {
                        parents[p.i][p.j].getValue(d)
                    }

                    operator fun set(plotSide: PlotSide, value: PlotSide?) = with(plotSide) {
                        parents[p.i][p.j].run {
                            if (d !in this) throw AssertionError()
                            this[d] = value
                        }
                    }
                }
                */

                override var PlotSide.parent: PlotSide?
                    get() {
                        assert(has(this))
                        return parents[p.i][p.j]!!.getValue(d)
                    }
                    set(value) {
                        assert(has(this))
                        parents[p.i][p.j]!![d] = value
                    }

                override val allElements: Collection<PlotSide>
                    get() = ps.flatMap { p -> parents[p.i][p.j]!!.keys.map { d -> PlotSide(p, d) } }

                // not used
                /**
                 * Can only remove a leaf!
                 */
                fun removeLeaf(plotSide: PlotSide) {
                    with(plotSide) {
                        assert(has(this))
                        parents[p.i][p.j]!!.remove(d)
                    }
                }
            }

            for (p in ps) {
                val pSides = with(p) { sideUnionFind.parents[i][j] }!!
                for (d in Direction.entries)
                    pSides[d] = null

                for (neighborDirection in Direction.smallerDirections) {
                    val neighborP = p + neighborDirection.diff
                    val pNeighborSides = with(neighborP) { sideUnionFind.parents.getOrNull(i)?.getOrNull(j) }
                    if (pNeighborSides !== null) {
                        fun removeAndUnion(
                            neighborConnectingSideDirection: Direction,
                            pConnectingSideDirection: Direction,
                            commonSides: List<Direction>
                        ) {
                            val pNewSide = neighborConnectingSideDirection

                            /*for (commonSide in commonSides) {
                                pSides[commonSide] = null
                            }*/

                            if (neighborConnectingSideDirection in pNeighborSides) {
                                pNeighborSides.remove(neighborConnectingSideDirection)
                                println("Remove neighbor side: ${PlotSide(neighborP, neighborConnectingSideDirection)}")
                                pSides.remove(pConnectingSideDirection)
                                println("Remove own side: ${PlotSide(p, pConnectingSideDirection)}")

                                for (commonSide in commonSides) {
                                    val neighborCommonPlotSide = PlotSide(neighborP, commonSide)
                                    if (sideUnionFind.has(neighborCommonPlotSide))
                                        sideUnionFind.unionIfNeeded(
                                            neighborCommonPlotSide, PlotSide(p, commonSide)
                                        ) // ! For removing a leaf to work, `neighborCommonPlotSide` must be put first.
                                }
                            }
                            /*else
                                pSides[pConnectingSide] = null*/

                            //pSides[pNewSide] = null
                        }

                        when (neighborDirection) {
                            Up -> removeAndUnion(Down, Up, listOf(Left, Right))
                            Left -> removeAndUnion(Right, Left, listOf(Up, Down))
                            else -> throw AssertionError()
                        }
                    }
                }
            }

            ps.size * sideUnionFind.sets().size
        }


        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day12_test")
    check(part1(testInput).also { println(it) } == 140)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day12")
    part1(input).println()

    check(part2(testInput) == 80) // TODO note that the test input might be different
    part2(input).println()
}
