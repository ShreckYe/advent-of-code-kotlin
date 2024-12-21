import Direction.*

enum class Direction(val diff: PositionDiff) {
    Up(PositionDiff(-1, 0)), Right(PositionDiff(0, 1)), Down(PositionDiff(1, 0)), Left(PositionDiff(0, -1));

    companion object {
        val smallerDirections = listOf(Up, Left)
    }
}

val directionChars = listOf('^', '>', 'v', '<')

fun Char.toDirection() =
    when (this) {
        '^' -> Up
        '>' -> Right
        'v' -> Down
        '<' -> Left
        else -> null
    }

fun Direction.toChar() =
    when (this) {
        Up -> '^'
        Right -> '>'
        Down -> 'v'
        Left -> '<'
    }

@Suppress("NOTHING_TO_INLINE")
private inline fun Direction.turnRight90DegreesInline() =
    when (this) {
        Up -> Right
        Right -> Down
        Down -> Left
        Left -> Up
    }

fun Direction.turnRight90Degrees() =
    turnRight90DegreesInline()

fun Direction.turnLeft90Degrees() =
    turnRight90DegreesInline().turnRight90DegreesInline().turnRight90DegreesInline()
