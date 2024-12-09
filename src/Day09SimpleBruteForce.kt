fun main() {
    fun part2(input: List<String>): Long {
        data class File(val startingIndex: Int, val length: Int, val id: Int)

        val blocks = mutableListOf<Int?>()
        val files = mutableListOf<File>()
        var isFile = true
        var id = 0
        var i = 0
        for (lengthC in input.single()) {
            val length = lengthC.digitToInt()
            if (isFile) {
                files.add(File(i, length, id))
                repeat(length) {
                    blocks.add(id)
                    i++
                }
                id++
            } else
                repeat(length) {
                    blocks.add(null)
                    i++
                }
            isFile = !isFile
        }

        //println(blocks)
        for (file in files.reversed()) {
            outer@ for (i in 0 until file.startingIndex) {
                if (i + file.length <= blocks.size && blocks.subList(i, i + file.length).all { it === null }) {
                    for (j in i until i + file.length)
                        blocks[j] = file.id
                    for (j in file.startingIndex until file.startingIndex + file.length)
                        blocks[j] = null
                    break@outer
                }
            }
            //println(blocks)
        }

        val ans = blocks.asSequence().withIndex()
            .sumOf { (i, id) -> id?.let { i.toLong() * it.toLong() } ?: 0 } // `Int` is not enough

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day09_test")
    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day09")

    check(part2(testInput).also { println(it) } == 2858L)
    part2(input).println()
}
