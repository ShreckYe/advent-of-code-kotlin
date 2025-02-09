import kotlin.math.abs

/**
 * Note that this is coordinated like a matrix, with [i] indicating the row counting from top, and [j] indicating the column counting from left.
 */
interface PositionOrPositionDiff<T : PositionOrPositionDiff<T>> : Comparable<T> {
    val i: Int
    val j: Int
    override fun compareTo(other: T): Int =
        when (val iCompareTo = (i compareTo other.i)) {
            0 -> j compareTo other.j
            else -> iCompareTo
        }

    fun coordinateSum() =
        i + j
    fun coordinateAbsSum() =
        abs(i) + abs(j)
}

data class Position(override val i: Int, override val j: Int) : PositionOrPositionDiff<Position> {
    operator fun plus(other: PositionDiff) =
        Position(i + other.i, j + other.j)

    operator fun minus(other: Position) =
        PositionDiff(i - other.i, j - other.j)

    override fun toString() =
        "($i, $j)"
}

// TODO `id` and `jd`
data class PositionDiff(override val i: Int, override val j: Int) : PositionOrPositionDiff<PositionDiff> {
    operator fun plus(other: PositionDiff) =
        PositionDiff(i + other.i, j + other.j)

    override fun toString() =
        "($i, $j)"
}

object DiagonalComparator : Comparator<PositionOrPositionDiff<*>> {
    override fun compare(o1: PositionOrPositionDiff<*>, o2: PositionOrPositionDiff<*>): Int {
        return o1.coordinateSum() compareTo o2.coordinateSum()
    }
}


// TODO `getOrNull`

@JvmName("getListOfList")
operator fun <T> List<List<T>>.get(p: Position) =
    this[p.i][p.j]

@JvmName("setListOfList")
operator fun <T> List<MutableList<T>>.set(p: Position, value: T) {
    this[p.i][p.j] = value
}

@JvmName("getListOfString")
operator fun List<String>.get(p: Position) =
    this[p.i][p.j]

@JvmName("getOrNullListOfString")
fun List<String>.getOrNull(p: Position) =
    this.getOrNull(p.i)?.getOrNull(p.j)

@JvmName("getListOfArray")
operator fun <T> List<Array<T>>.get(p: Position) =
    this[p.i][p.j]

@JvmName("setListOfArray")
operator fun <T> List<Array<T>>.set(p: Position, value: T) {
    this[p.i][p.j] = value
}

operator fun List<BooleanArray>.get(p: Position) =
    this[p.i][p.j]

operator fun List<BooleanArray>.set(p: Position, value: Boolean) {
    this[p.i][p.j] = value
}

operator fun List<IntArray>.get(p: Position) =
    this[p.i][p.j]

operator fun List<IntArray>.set(p: Position, value: Int) {
    this[p.i][p.j] = value
}

@JvmName("getListOfCharArray")
operator fun List<CharArray>.get(p: Position) =
    this[p.i][p.j]

@JvmName("getOrNullListOfCharArray")
fun List<CharArray>.getOrNull(p: Position) =
    this.getOrNull(p.i)?.getOrNull(p.j)

operator fun List<CharArray>.set(p: Position, value: Char) {
    this[p.i][p.j] = value
}


fun List<String>.positionOf(c: Char) =
    withIndex().asSequence().mapNotNull { (i, line) ->
        line.indexOf(c).let { j ->
            if (j != -1) Position(i, j) else null
        }
    }.first()

fun <T> List<List<T>>.positionOf(value: T) =
    withIndex().asSequence().mapNotNull { (i, line) ->
        line.indexOf(value).let { j ->
            if (j != -1) Position(i, j) else null
        }
    }.first()
