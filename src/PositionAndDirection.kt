import java.util.*

data class PositionAndDirection(val p: Position, val d: Direction)

operator fun <T> List<List<EnumMap<Direction, T>>>.get(pd: PositionAndDirection) =
    with(pd) { this@get[p.i][p.j][d] }

operator fun <T> List<List<EnumMap<Direction, T>>>.set(pd: PositionAndDirection, value: T) {
    with(pd) {
        this@set[p.i][p.j][d] = value
    }
}
