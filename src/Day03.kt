val p1Regex = Regex("mul\\((\\d+),(\\d+)\\)")
val p2Regex = Regex("(don't)|(do)|(${p1Regex.pattern})")/*.also { println(it) }*/ // "don't" has to be put before "do"

sealed class P2Exp {
    data object Do : P2Exp()
    data object Dont : P2Exp()
    data class Mul(val a: Long, val b: Long) : P2Exp()
}

fun main() {
    fun part1(input: List<String>): Long {
        val ans = input.sumOf {
            p1Regex.findAll(it).sumOf {
                val groups = it.groups
                val a = groups[1]!!.value.toLong()
                val b = groups[2]!!.value.toLong()
                a * b
            }
        }
        return ans
    }

    fun part2(input: List<String>): Long {
        //println("Input lines: ${input.size}")
        val inputS = input.joinToString() // The input should be viewed as a whole!
        val exps = p2Regex.findAll(inputS).map {
            val groups = it.groups
            //println("Groups: " + it.groupValues)
            val dont = groups[1]?.value
            val `do` = groups[2]?.value
            val mul = groups[3]?.value
            when {
                `do` !== null -> P2Exp.Do
                dont !== null -> P2Exp.Dont
                mul !== null -> P2Exp.Mul(groups[4]!!.value.toLong(), groups[5]!!.value.toLong())
                else -> throw AssertionError()
            }
        }
        //println(exps.toList())

        val ans = exps.fold(0L to true) { (sum, `do`), exp ->
            when (exp) {
                P2Exp.Do -> sum to true
                P2Exp.Dont -> sum to false
                is P2Exp.Mul -> (if (`do`) sum + exp.a * exp.b else sum) to `do`
            }
        }.first

        return ans
    }

    // Test if implementation meets criteria from the description, like:
    check(part1(listOf("test_input")) == 0L)

    // Or read a large test input from the `src/Day02_test.txt` file:
    val testInputPart1 = readInput("Day03_test_Part1")
    check(part1(testInputPart1) == 161L)
    val testInputPart2 = readInput("Day03_test_Part2")
    check(part2(testInputPart2)/*.also { println(it) }*/ == 48L)

    // Read the input from the `src/Day01.txt` file.
    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
