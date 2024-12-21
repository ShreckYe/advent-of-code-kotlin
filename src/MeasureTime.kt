import kotlin.time.measureTimedValue

fun <T> measureTimeAndPrint(block : () -> T) : T {
    val (value , duration) = measureTimedValue(block)
    println("Measured time: $duration")
    return value
}