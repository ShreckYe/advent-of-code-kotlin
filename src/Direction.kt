enum class Direction(val diff: PositionDiff) {
    Up(PositionDiff(-1, 0)), Right(PositionDiff(0, 1)), Down(PositionDiff(1, 0)), Left(PositionDiff(0, -1));

    companion object {
        val smallerDirections = listOf(Up, Left)
    }
}
