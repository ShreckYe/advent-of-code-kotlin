fun Char?.isHabitable() =
    this !== null && this != '#'

fun printMap(map: List<CharArray>) =
    println(map.joinToString("\n", postfix = "\n") {
        it.concatToString()
    })
