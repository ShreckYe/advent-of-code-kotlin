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
        val sp = input.asSequence()
            .mapIndexedNotNull { i, line -> line.indexOf('S').let { j -> if (j != -1) Position(i, j) else null } }
            .first()

        //val minScores = input.map { IntArray(it.length) { Int.MAX_VALUE } } // This can't be used because scores of different directions are not comparable.
        val minScores = input.map {
            it.map {
                EnumMap<Direction, Int>(Direction::class.java)/*.apply {
                    for (d in Direction.entries)
                        this[d] = Int.MAX_VALUE
                }*/
            }
        }
        val prevs =
            input.map { it.map { EnumMap<Direction, MutableList<PositionAndDirection>>(Direction::class.java) } }

        // Dijkstra's algorithm

        data class Candidate(val pd: PositionAndDirection, val score: Int, val prev: PositionAndDirection?)

        // score has to be tracked to see whether the path was invalidated by a lower score.
        val priorityQueue = PriorityQueue<Candidate> { o1, o2 -> o1.score compareTo o2.score }

        priorityQueue.add(Candidate(PositionAndDirection(sp, Direction.Right), 0, null))
        var eMinScore: Int? = null
        var ep: Position? = null
        //var eMin: Candidate? = null
        //val eMins  = mutableListOf<Candidate>()
        while (true) {
            val candidate: Candidate? = priorityQueue.poll()
            if (candidate === null)
                break

            val pd = candidate.pd
            val score = candidate.score

            if (eMinScore !== null && score > eMinScore!!)
                break

            val existingMinScore = minScores[pd]
            //println("$candidate $existingMinScore")
            val p = pd.p
            if (existingMinScore === null) {
                minScores[pd] = score
                // parentheses needed
                if (input[p] == 'E' && (eMinScore/*.also { println(it) }*/ === null || score < eMinScore!!)) {
                    eMinScore = score
                    ep = p
                }
            } else if (score > existingMinScore)
                continue
            else if (score < existingMinScore)
                throw AssertionError()

            //println("added")

            val d = pd.d
            val prevs = prevs[p].getOrPut(d) { mutableListOf() }
            candidate.prev?.let { prevs.add(it) }

            fun PositionAndDirection.walkWithDirection(newD: Direction) =
                PositionAndDirection(this.p + newD.diff, newD)

            // combine turning and walking
            val nexts = listOf(
                pd.walkWithDirection(d) to 1,
                pd.walkWithDirection(d.turnRight90Degrees()) to 1001,
                pd.walkWithDirection(d.turnLeft90Degrees()) to 1001
            ).filter { (nextPd, _) ->
                input.getOrNull(nextPd.p.i)?.getOrNull(nextPd.p.j).let { it !== null && it != '#' }
            }

            for ((nextPd, scoreInc) in nexts) {
                val nextScore = score + scoreInc
                val existingNextMinScore = minScores[nextPd]
                if (existingNextMinScore === null || run {
                        assert(nextScore >= existingNextMinScore)
                        existingNextMinScore == nextScore
                    })
                    priorityQueue.add(Candidate(nextPd, nextScore, pd))
            }
        }

        //println(prevs.joinToString("\n", postfix = "\n") {it.joinToString("") { it.count().toString() }})

        val isOnBestPath = input.map { it.map { EnumSet.noneOf(Direction::class.java) } }
        fun addAllToBestPath(pd: PositionAndDirection) {
            val set = isOnBestPath[pd.p]
            val d = pd.d
            if (d !in set) {
                //println(pd)
                set.add(d)
                for (prev in prevs[pd]!!)
                    addAllToBestPath(prev)
            }
        }


        prevs[ep!!].keys.forEach { d ->
            addAllToBestPath(PositionAndDirection(ep, d))
        }

        val ans = isOnBestPath.sumOf { it.count { it.isNotEmpty() } }

        println(input.withIndex().joinToString("\n", postfix = "\n") { (i, line) ->
            line.withIndex().map { (j, c) -> if (isOnBestPath[i][j].isNotEmpty()) 'O' else c }.joinToString("")
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

    check(part2(testInput)/*.also { println(it) }*/ == 45)
    val testInput2 = readInput("Day16_test2")
    check(part2(testInput2)/*.also { println(it) }*/ == 64)

    part2(input).println()
}
