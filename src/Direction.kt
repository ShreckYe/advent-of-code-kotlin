import Direction.*

enum class Direction(val diff: PositionDiff) {
    Up(PositionDiff(-1, 0)), Right(PositionDiff(0, 1)), Down(PositionDiff(1, 0)), Left(PositionDiff(0, -1));

    companion object {
        val smallerDirections = listOf(Up, Left)
    }
}

fun Char.toDirection() =
    when (this) {
        '^' -> Up
        '>' -> Right
        'v' -> Down
        '<' -> Left
        else -> null
    }

fun Direction.turnRight90Degrees() =
    when (this) {
        Up -> Right
        Right -> Down
        Down -> Left
        Left -> Up
    }
