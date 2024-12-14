data class XAndY(val x: Int, val y: Int)
class Robot(val p: XAndY, val v: XAndY)

infix fun Int.posRemainder(other: Int): Int {
    require(other > 0)
    return (this % other + other) % other
}

fun processInput(input: List<String>) = input.map {
    val list = it.splitToSequence(' ').map {
        val list = it.substringAfter('=').splitToSequence(',').map { it.toInt() }.toList()
        XAndY(list[0], list[1])
    }.toList()
    Robot(list[0], list[1])
}

fun Robot.position(numSeconds: Int, width: Int, height: Int): XAndY =
    XAndY((p.x + numSeconds * v.x) posRemainder width, (p.y + numSeconds * v.y) posRemainder height)

fun List<Robot>.positions(numSeconds: Int, width: Int, height: Int) =
    map { it.position(numSeconds, width, height) }

@OptIn(ExperimentalUnsignedTypes::class)
fun List<XAndY>.toCountVisual(width: Int, height: Int): List<UByteArray> {
    val eachCount = groupingBy { it }.eachCount()
    return List(height) { y -> UByteArray(width) { x -> eachCount[XAndY(x, y)]?.coerceAtMost(256)?.toUByte() ?: 0U } }
}
