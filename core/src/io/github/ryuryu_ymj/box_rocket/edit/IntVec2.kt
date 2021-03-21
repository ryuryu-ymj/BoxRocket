package io.github.ryuryu_ymj.box_rocket.edit

data class IntVec2(val x: Int, val y: Int) {
    operator fun minus(v: IntVec2) = IntVec2(x - v.x, y - v.y)

    fun crs(v: IntVec2) = x * v.y - y * v.x
}

data class Edge(val begin: IntVec2, val end: IntVec2)