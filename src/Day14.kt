fun main() {
    data class XAndY(val x: Int, val y: Int)
    class Robot(val p: XAndY, val v: XAndY)

    infix fun Int.posRemainder(other: Int): Int {
        require(other > 0)
        return (this % other + other) % other
    }

    fun processInput(input: List<String>) = input.map {
        val list = it.splitToSequence(' ').map {
            val list = it.substringAfter('=').splitToSequence(',').map { it.toInt() }.toList()
            XAndY(list[0], list[1])
        }.toList()
        Robot(list[0], list[1])
    }

    fun List<XAndY>.isSymmetric(midWidth: Int, midWidthT2: Int): Boolean {
        val eachCount = groupingBy { it }.eachCount()
        //(0 until midWidth).all { x -> (0 until height).all {} }
        val foldedCount = eachCount.entries.groupBy {
            val key = it.key
            val x = key.x
            XAndY(if (x < midWidth) x else midWidthT2 - x, key.y)
        }
        return foldedCount.all {
            //println(it)
            if (it.key.x == midWidth)
                true
            else {
                val leftAndRight = it.value
                leftAndRight.size == 2 && run {
                    val l1 = leftAndRight[0]
                    val l2 = leftAndRight[1]
                    assert(l1.key.x + l2.key.x == midWidthT2)

                    l1.value == l2.value
                }
            }
        }
    }

    fun printVisual(ps: List<XAndY>, width: Int, height: Int) {
        val eachCount = ps.groupingBy { it }.eachCount()
        val visual = List(height) { y -> List(width) { x -> eachCount[XAndY(x, y)]?.toString()?.last() ?: '.' } }
        println(visual.joinToString("\n") { it.joinToString("") })
    }


    fun part1(input: List<String>, width: Int, height: Int): Int {
        val robots = processInput(input)

        val psAfter100Seconds = robots.map {
            with(it) {
                XAndY((p.x + 100 * v.x) posRemainder width, (p.y + 100 * v.y) posRemainder height)
            }
        }

        val midWidth = width / 2
        val midHeight = height / 2
        val grouped = psAfter100Seconds.groupBy { (it.x compareTo midWidth) to (it.y compareTo midHeight) }
        println(grouped)
        println(grouped.mapValues { it.value.size })


        val ans = listOf(-1, 1).flatMap { xQuadrant ->
            listOf(-1, 1).map { yQuadrant -> grouped.getValue(xQuadrant to yQuadrant).size }
        }.reduce { a, b -> a * b }

        return ans
    }

    fun part2(input: List<String>, width: Int, height: Int): Int {
        val robots = processInput(input)

        val midWidth = width / 2
        val midWidthT2 = midWidth * 2

        //val midHeight = height / 2

        var ps = robots.map { it.p }
        val vs = robots.map { it.v }

        fun List<XAndY>.transposed() = map { XAndY(it.y, it.x) }
        val midHeight = height / 2
        val midHeightT2 = midHeight * 2

        var i = 0
        while (!(ps.isSymmetric(midWidth, midWidthT2) || ps.transposed().isSymmetric(midHeight, midHeightT2))) {
            /*
            println(i)
            printVisual(ps, width, height)
            System.`in`.read()
            */

            ps = ps.zip(vs) { p, v ->
                XAndY((p.x + v.x) posRemainder width, (p.y + v.y) posRemainder height)
            }
            i++
        }

        return i
    }

    fun part2Method2(input: List<String>, width: Int, height: Int) {
        val robots = processInput(input)

        var ps = robots.map { it.p }
        val vs = robots.map { it.v }

        TODO("find lowest variance")
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day14_test")
    check(part1(testInput, 11, 7) == 12)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day14")
    part1(input, 101, 103).println()

    assert(listOf<XAndY>().isSymmetric(0, 0)) // width 1
    assert(listOf(XAndY(0, 0)).isSymmetric(0, 0)) // width 1
    assert(listOf(XAndY(0, 1), XAndY(2, 1)).isSymmetric(1, 2)) // width 3
    assert(!listOf(XAndY(0, 0), XAndY(2, 1)).isSymmetric(1, 2)) // width 3

    //check(part2(testInput) == 1)
    //part2(testInput, 11, 7)
    part2(input, 101, 103).println()
}
