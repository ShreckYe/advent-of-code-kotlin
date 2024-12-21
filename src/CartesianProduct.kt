infix fun <T1, T2> List<T1>.cartesianProduct(other: List<T2>) =
    flatMap { a -> other.map { b -> a to b } }
