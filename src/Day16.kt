import java.util.*

fun main() {
    // TODO extract common
    data class PositionAndDirection(val p: Position, val d: Direction)

    fun part1(input: List<String>): Int {
        val s = input.asSequence()
            .mapIndexedNotNull { i, line -> line.indexOf('S').let { j -> if (j != -1) Position(i, j) else null } }
            .first()
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

        operator fun <T> List<List<T>>.get(p: Position) =
            this[p.i][p.j]

        operator fun List<String>.get(p: Position) =
            this[p.i][p.j]

        operator fun <T> List<MutableList<T>>.set(p: Position, value: T) {
            this[p.i][p.j] = value
        }

        operator fun <T> List<Array<T>>.set(p: Position, value: T) {
            this[p.i][p.j] = value
        }

        operator fun <T> List<List<EnumMap<Direction, T>>>.get(pd: PositionAndDirection) =
            with(pd) { this@get[p.i][p.j][d] }

        operator fun <T> List<List<EnumMap<Direction, T>>>.set(pd: PositionAndDirection, value: T) {
            with(pd) {
                this@set[p.i][p.j][d] = value
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
        return input.size
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day06_test.txt` file:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 7036)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day16")
    part1(input).println()

    check(part2(testInput) == 45)
    part2(input).println()
}
