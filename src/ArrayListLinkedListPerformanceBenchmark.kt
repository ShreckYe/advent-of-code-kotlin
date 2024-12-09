import java.util.*
import kotlin.system.measureTimeMillis

fun main() {
    val list = List(10000) { it }
    val linkedList = LinkedList(list)
    val arrayList = ArrayList(list)

    fun MutableListIterator<Int>.iteratorRemoveAndAdd() {
        repeat(5000) { next() }
        repeat(10000) {
            remove()
            //previous()
            //next()
            add(4999)
        }
    }

    fun runWithLinkedList() = linkedList.listIterator().iteratorRemoveAndAdd()
    fun runWithArrayList() = arrayList.listIterator().iteratorRemoveAndAdd()

    repeat(2) { runWithLinkedList() }
    println("Linked list: " + measureTimeMillis { runWithLinkedList() })

    repeat(2) { runWithArrayList() }
    println("Array list: " + measureTimeMillis { runWithArrayList() })
}