interface PositionOrPositionDiff<T : PositionOrPositionDiff<T>> : Comparable<T> {
    val i: Int
    val j: Int
    override fun compareTo(other: T): Int =
        when (val iCompareTo = (i compareTo other.i)) {
            0 -> j compareTo other.j
            else -> iCompareTo
        }


}

data class Position(override val i: Int, override val j: Int) : PositionOrPositionDiff<Position> {
    operator fun plus(other: PositionDiff) =
        Position(i + other.i, j + other.j)
}

data class PositionDiff(override val i: Int, override val j: Int) : PositionOrPositionDiff<PositionDiff> {
    operator fun plus(other: PositionDiff) =
        PositionDiff(i + other.i, j + other.j)
}
