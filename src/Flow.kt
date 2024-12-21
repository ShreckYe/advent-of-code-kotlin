import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
operator fun <T> Flow<T>.plus(other : Flow<T>) =
    flowOf(this, other).flattenConcat()

operator fun <T> Flow<T>.plus(other : T) =
    this + flowOf(other)