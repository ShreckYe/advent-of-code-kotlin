import kotlin.io.path.Path
import kotlin.io.path.readText

// adapted
fun readInput(name: String) = Path("$name.txt").readText().trim().lines()