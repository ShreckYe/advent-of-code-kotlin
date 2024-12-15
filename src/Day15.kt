fun main() {
    fun part1(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val map = input.subList(0, emptyLineIndex).map { it.toCharArray() }
        val moves = input.subList(emptyLineIndex + 1, input.size).joinToString("")

        var robotPosition = map.asSequence().withIndex().mapNotNull {
            val j = it.value.indexOf('@')
            if (j != -1)
                Position(it.index, j)
            else null
        }.first()

        fun moveToIfPossible(c: Char, from: Position, direction: Direction): Position? {
            //val fromC = map[position.i][position.j]
            val to = from + direction.diff
            val toC = map.getOrNull(to.i)?.getOrNull(to.j)

            fun move(c: Char, from: Position, to: Position) {
                map[from.i][from.j] = '.'
                map[to.i][to.j] = c
            }

            fun move(): Position {
                move(c, from, to)
                return to
            }

            return when (toC) {
                null, '#' -> null
                '.' -> move()
                'O' -> moveToIfPossible(toC, to, direction)?.let { move() }
                else -> throw IllegalArgumentException()
            }
            /*
            when (c) {
                '.' -> {}
                'O' -> {}
                '#' -> {}
                else -> throw IllegalArgumentException()
            }
            */
        }

        for (move in moves)
            robotPosition = moveToIfPossible('@', robotPosition, move.toDirection()!!) ?: robotPosition

        val ans = map.withIndex().sumOf { (i, line) ->
            line.withIndex().sumOf { (j, c) ->
                if (c == 'O') i * 100 + j /*(i + 1) * 100 + (j + 1)*/ else 0
            }
        }

        return ans
    }

    fun part2(input: List<String>): Int {
        val emptyLineIndex = input.indexOf("")
        val map = input.subList(0, emptyLineIndex).map {
            it.asIterable().joinToString("") { c ->
                when (c) {
                    '#' -> "##"
                    'O' -> "[]"
                    '.' -> ".."
                    '@' -> "@."
                    else -> throw IllegalArgumentException()
                }
            }
                .toCharArray()
        }

        fun printMap() =
            println(map.joinToString("\n", postfix = "\n") { it.concatToString() })

        val moves = input.subList(emptyLineIndex + 1, input.size).joinToString("")

        var robotPosition = map.asSequence().withIndex().mapNotNull {
            val j = it.value.indexOf('@')
            if (j != -1)
                Position(it.index, j)
            else null
        }.first()

        println("Robot position: " + robotPosition)

        fun moveToIfPossible(froms: List<Pair<Char, Position>>, direction: Direction): Boolean {
            val diff = direction.diff
            val fromAndTos = froms.map { (c, p) ->
                val toP = p + diff
                val toC = map.getOrNull(toP.i)?.getOrNull(toP.j)

                (c to p) to toC?.let { it to toP }
            }
            println("fromAndTos: $fromAndTos")

            if (fromAndTos.any { it.second === null }) return false
            //fromAndTos as List<List<Pair<Char, Position>>>

            val pushedss = fromAndTos.map { (_, to) ->
                /*if (to === null)
                    null
                else {*/
                val (toC, toP) = to!!
                when (toC) {
                    '#' -> null
                    '.' -> emptyList() //listOf(toC to toP)
                    '[' -> listOf(map[toP.i][toP.j + 1] to toP.copy(j = toP.j + 1), toC to toP) // The order can't be swapped!
                    /*
                    // wrong
                    if (direction == Direction.Right) emptyList()
                    else listOf(toC to toP, map[toP.i][toP.j + 1] to toP.copy(j = toP.j + 1))
                    */
                    ']' -> listOf(map[toP.i][toP.j - 1] to toP.copy(j = toP.j - 1), toC to toP) // The order can't be swapped!
                    /*
                    // wrong
                    if (direction == Direction.Left) emptyList()
                    else listOf(map[toP.i][toP.j - 1] to toP.copy(j = toP.j - 1), toC to toP)
                    */
                    else -> throw IllegalArgumentException(toC.toString())
                }
                //}
            }
            println("pushedss: $pushedss")

            if (pushedss.any { it === null }) return false

            val fromSet = fromAndTos.asSequence().map { it.first }.toSet()
            val pusheds = pushedss.asSequence()
                .flatMap { it!! }
                .filter { it !in fromSet }
                .distinctBy { it.second }
                .toList()
            println("pusheds: $pusheds")

            return if (pusheds.isEmpty() /* neglected at first */ || moveToIfPossible(pusheds, direction)) {

                fun move(c: Char, from: Position, to: Position) {
                    map[from.i][from.j] = '.'
                    map[to.i][to.j] = c
                }

                for ((from, to) in fromAndTos) {
                    to!!
                    move(from.first, from.second, to.second)
                }

                /*
                // With this approach so the list order in processing `pushedss` can be more flexible.

                for ((from, _) in fromAndTos) {
                    val fromP = from.second
                    map[fromP.i][fromP.j] = '.'
                }
                for ((from, to) in fromAndTos) {
                    val toP = to!!.second
                    map[toP.i][toP.j] = from.first
                }
                */

                true
            } else false
        }

        printMap()
        for (move in moves) {
            val direction = move.toDirection()!!
            println(direction)
            if (moveToIfPossible(listOf('@' to robotPosition), direction).also {
                    println(it)
                })
                robotPosition += direction.diff
            printMap()
        }

        val ans = map.withIndex().sumOf { (i, line) ->
            line.withIndex().sumOf { (j, c) ->
                if (c == '[') i * 100 + j else 0
            }
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInputSmall = readInput("Day15_test_small")
    val testInputLarge = readInput("Day15_test_large")
    println(part1(testInputSmall))
    check(part1(testInputLarge)/*.also { println(it) }*/ == 10092)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day15")
    part1(input).println()

    println(part2(testInputSmall))
    check(part2(testInputLarge).also { println(it) } == 9021)
    part2(input).println()
}
