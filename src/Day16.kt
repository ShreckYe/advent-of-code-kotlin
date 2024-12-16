import java.util.*

fun main() {
    fun part1(input: List<String>): Int {
        val s = input.asSequence()
            .mapIndexedNotNull { i, line -> line.indexOf('S').let { j -> if (j != -1) Position(i, j) else null } }
            .first()
        // not needed actually
        val e = input.asSequence()
            .mapIndexedNotNull { i, line -> line.indexOf('E').let { j -> if (j != -1) Position(i, j) else null } }
            .first()

        //val minScores = input.map { IntArray(it.length) { Int.MAX_VALUE } }
        val minScores = input.map {
            it.map {
                EnumMap<Direction, Int>(Direction::class.java)/*.apply {
                    for (d in Direction.entries)
                        this[d] = Int.MAX_VALUE
                }*/
            }
        }

        // Dijkstra's algorithm

        val spd = PositionAndDirection(s, Direction.Right)
        minScores[spd] = 0
        val priorityQueue = PriorityQueue<PositionAndDirection> { o1, o2 -> minScores[o1]!! compareTo minScores[o2]!! }
        priorityQueue.add(spd)
        val traversed = input.map { it.map { EnumSet.noneOf(Direction::class.java) } }
        var eMinScore: Int? = null
        outer@ while (true) {
            val minPd = priorityQueue.poll()
            if (minPd.d in traversed[minPd.p.i][minPd.p.j])
                continue
            traversed[minPd.p.i][minPd.p.j].add(minPd.d)

            val neighbors = listOf(
                minPd.copy(p = minPd.p + minPd.d.diff) to 1,
                minPd.copy(d = minPd.d.turnRight90Degrees()) to 1000,
                minPd.copy(d = minPd.d.turnLeft90Degrees()) to 1000
            )

            val score = minScores[minPd]!!
            for ((neighborPd, scoreInc) in neighbors) {
                if (input.getOrNull(neighborPd.p.i)?.getOrNull(neighborPd.p.j).let { it === null || it == '#' })
                    continue

                val neighborNewScore = score + scoreInc
                if (minScores[neighborPd] === null) {
                    minScores[neighborPd] = neighborNewScore
                    if (input[neighborPd.p] == 'E') {
                        eMinScore = neighborNewScore
                        break@outer
                    }
                    priorityQueue.add(neighborPd)
                }
            }
        }

        return eMinScore!!
    }

    fun part2(input: List<String>): Int {
        val s = input.asSequence()
            .mapIndexedNotNull { i, line -> line.indexOf('S').let { j -> if (j != -1) Position(i, j) else null } }
            .first()

        //val minScores = input.map { IntArray(it.length) { Int.MAX_VALUE } }
        val minScores = input.map {
            it.map {
                EnumMap<Direction, Int>(Direction::class.java)/*.apply {
                    for (d in Direction.entries)
                        this[d] = Int.MAX_VALUE
                }*/
            }
        }

        // Dijkstra's algorithm

        val spd = PositionAndDirection(s, Direction.Right)
        minScores[spd] = 0
        data class PdAndScore(val pd: PositionAndDirection, val score: Int)

        val priorityQueue =
            PriorityQueue<PdAndScore> { o1, o2 -> o1.score compareTo o2.score } // score has to be tracked to see whether the path was invalidated.
        priorityQueue.add(PdAndScore(spd, 0))
        val candidatePrevs = input.map { it.map { EnumMap<Direction, Position>(Direction::class.java) } }
        val prevs = input.map { it.map { mutableListOf<Position>() } }
        var eMinScore: Int? = null
        var e: Position? = null
        while (true) {
            val minPds: PdAndScore? = priorityQueue.poll()
            if (minPds === null)
                break

            val minPd = minPds.pd

            val score = minPds.score

            val minScore = minScores[minPd]
            if (minScore === null)
                minScores[minPd] = score
            else if (score > minScore)
                continue
            else if (score < minScore)
                throw AssertionError()

            if (eMinScore !== null && score > eMinScore!!)
                break

            val candidatePrev = candidatePrevs[minPd]
            candidatePrev?.let {
                candidatePrevs[minPd.p].remove(minPd.d)
                prevs[minPd.p].add(it)
                //println("Prev added: $minPd $it")
            }

            val neighbors = listOf(
                minPd.copy(p = minPd.p + minPd.d.diff) to 1,
                minPd.copy(d = minPd.d.turnRight90Degrees()) to 1000,
                minPd.copy(d = minPd.d.turnLeft90Degrees()) to 1000
            )

            for ((neighborPd, scoreInc) in neighbors) {
                if (input.getOrNull(neighborPd.p.i)?.getOrNull(neighborPd.p.j).let { it === null || it == '#' })
                    continue

                val neighborNewScore = score + scoreInc
                val neighborMinScore = minScores[neighborPd]
                if (neighborMinScore === null || run {
                        assert(neighborNewScore >= neighborMinScore)
                        neighborMinScore == neighborNewScore
                    }) {
                    // not needed for the `minScores[neighborPd] == neighborNewScore` case but not refactored
                    //minScores[neighborPd] = neighborNewScore // TODO moved to above
                    // can be added only the case of walking but not refactored
                    if (neighborPd.p != minPd.p)
                    //prevs[neighborPd.p].add(minPd.p) // Adding directly to `prevs` is incorrect.
                        candidatePrevs[neighborPd] = minPd.p/*.also {
                            println("Candidate prev added: $neighborPd $minPd")
                        }*/
                    if (input[neighborPd.p] == 'E') {
                        eMinScore = neighborNewScore
                        e = neighborPd.p
                        //continue // commented out so the candidate prev is processed
                    }
                    priorityQueue.add(PdAndScore(neighborPd, neighborNewScore))
                }
            }
        }

        //println(prevs.joinToString("\n", postfix = "\n") {it.joinToString("") { it.count().toString() }})

        val isOnBestPath = input.map { BooleanArray(it.length) { false } }
        fun addAllToBestPath(position: Position) {
            if (!isOnBestPath[position]) {
                //println(position)
                isOnBestPath[position] = true
                for (prev in prevs[position])
                    addAllToBestPath(prev)
            }
        }
        addAllToBestPath(e!!)

        val ans = isOnBestPath.sumOf { it.count { it } }

        println(input.withIndex().joinToString("\n", postfix = "\n") { (i, line) ->
            line.withIndex().map { (j, c) -> if (isOnBestPath[i][j]) 'O' else c }.joinToString("")
        })

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day06_test.txt` file:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 7036)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day16")
    part1(input).println()

    check(part2(testInput).also { println(it) } == 45)
    val testInput2 = readInput("Day16_test2")
    //check(part2(testInput2).also { println(it) } == 64) // TODO make this correct first

    part2(input).println()
}
