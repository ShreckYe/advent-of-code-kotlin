interface UnionFindOps<T> {
    var T.parent: T?

    fun T.findRoot(): T {
        //println("find root of $this with parent $parent")
        val parent = parent
        return if (parent === null)
            this
        else
            parent.findRoot().also {
                this.parent = it
                //println("Set $this's parent to $it")
            }
    }

    fun unionIfNeeded(a: T, b: T) {
        println("union $a and $b")
        val aRoot = a.findRoot()
        val bRoot = b.findRoot()
        if (aRoot != bRoot/*aRoot !== bRoot /* This caused bugs and took me a lot of time to debug! */*/)
            bRoot.parent = aRoot
        //println("Set root $b's parent to root $a\n")
    }
}

interface UnionFind<T> : UnionFindOps<T> {
    val allElements: Collection<T>
    fun mapByRoot() =
        allElements.groupBy { it.findRoot() }

    fun sets() =
        mapByRoot().values
}
