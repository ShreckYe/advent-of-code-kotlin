import java.util.*

fun main() {
    fun part1(input: List<String>): Long {
        val blocks = mutableListOf<Int?>()
        val freeSpaceIndices = mutableListOf<Int>()
        var isFile = true
        var id = 0
        var i = 0
        for (lengthC in input.single()) {
            val length = lengthC.digitToInt()
            if (isFile) {
                repeat(length) {
                    blocks.add(id)
                    i++
                }
                id++
            } else
                repeat(length) {
                    blocks.add(null)
                    freeSpaceIndices.add(i++)
                }
            isFile = !isFile
        }

        //println(blocks)

        i = 0
        var j = blocks.lastIndex
        while (i < freeSpaceIndices.size && freeSpaceIndices[i] < j) {
            val toMove = blocks[j]
            blocks[j--] = null
            if (toMove !== null)
                blocks[freeSpaceIndices[i++]] = toMove
        }

        //println(blocks.take(50))
        //println(blocks.takeLast(50))
        //println(blocks.take(j + 1).take(50))
        //println(blocks.take(j + 1).takeLast(50))
        check(blocks.asSequence().drop(j + 1).all { it === null })

        val ans = blocks.asSequence().take(j + 1).withIndex()
            .sumOf { (i, id) -> i.toLong() * id!!.toLong() } // `Int` is not enough

        return ans
    }

    fun part2(input: List<String>): Long {
        data class WholeSpace(val startingIndex: Int, val length: Int, val id: Int?)

        val spaces = LinkedList<WholeSpace>() // TODO try `MutableList`
        //val wholeFiles = mutableListOf<WholeSpace>()
        //val freeSpaces = mutableListOf<WholeSpace>()

        var isFile = true
        var id = 0
        var i = 0
        for (lengthC in input.single()) {
            val length = lengthC.digitToInt()
            if (isFile) {
                spaces.add(WholeSpace(i, length, id))
                i += length
                id++
            } else {
                spaces.add(WholeSpace(i, length, null))
                i += length
            }
            isFile = !isFile
        }

        /*val movedFiles = mutableListOf<WholeSpace>()
        var freeSpaceI = 0
        val iterator = wholeFiles.asReversed().listIterator()
        var file = iterator.next()
        while (true) {
            val free = freeSpaces[freeSpaceI]
            if (file.length <= free.length) {
                iterator.remove()
                movedFiles.add(WholeSpace(free.startingIndex, file.length, file.id))
                freeSpaces[freeSpaceI] = free.copy(
                    startingIndex = free.startingIndex + file.length,
                    length = free.length - file.length
                )
            }
        }*/

        val movedFiles = mutableListOf<WholeSpace>()

        val iterator = spaces.asReversed().listIterator()
        while (iterator.hasNext()) {
            //println(spaces.map { with(it) { "($startingIndex, $length, ${this.id})" } })

            val file = iterator.next()
            if (file.id === null)
                continue

            val indexedFree =
                spaces.takeWhile { it.startingIndex < file.startingIndex }.withIndex().firstOrNull { (_, free) -> free.id === null && free.length >= file.length }
            if (indexedFree === null)
                continue


            println(spaces.map { with(it) { "($startingIndex, $length, ${this.id})" } })

            // TODO move to end if OK
            val (freeI, free) = indexedFree
            println("free: $free")
            spaces[freeI] = free.copy(
                startingIndex = free.startingIndex + file.length,
                length = free.length - file.length
            )
            // TODO remove if length 0
            //spaces.add(freeI, file.copy(startingIndex = free.startingIndex)) // ConcurrentModificationException
           movedFiles.add( file.copy(startingIndex = free.startingIndex))

            fun <T> MutableListIterator<T>.getAndRemovePrevious() =
                if (hasPrevious()/*hasPrevious().also { previous() } && hasPrevious()*/)
                    previous().also {
                        println("previous: $it")
                        remove()
                        //next()
                    }
                else null

            fun <T> MutableListIterator<T>.getAndRemoveNext() =
                if (hasNext())
                    next().also {
                        println("next: $it")
                        remove()
                        //previous()
                    }
                else null

            iterator.remove() // remove current first
            println("current: $file")
            var newFreeLength = 0
            iterator.getAndRemovePrevious()?.let {
                newFreeLength += it.length
            }
            newFreeLength += file.length
            var newFreeStartingIndex = file.startingIndex
            iterator.getAndRemoveNext()?.let {
                newFreeLength += it.length
                newFreeStartingIndex = it.startingIndex
            }
            iterator.add(WholeSpace(newFreeStartingIndex, newFreeLength, null))
        }

        val ans = (spaces + movedFiles).sumOf {
            it.id?.let { id ->
                (it.startingIndex until it.startingIndex + it.length).sum().toLong() * id.toLong()
            } ?: 0L
        }

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    //check(part1(listOf("test_input")) == 1)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 1928L)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day09")
    part1(input).println()

    check(part2(testInput).also { println(it) } == 2858L) // TODO note that the test input might be different
    part2(input).println()
}
