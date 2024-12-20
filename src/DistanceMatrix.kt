typealias IntDistances = List<Array<Int?>>

fun IntDistances.intDistancesString() =
    joinToString("\n", postfix = "\n") { it.toList().toString() }
