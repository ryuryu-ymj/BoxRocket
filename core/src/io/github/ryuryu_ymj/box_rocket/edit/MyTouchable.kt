package io.github.ryuryu_ymj.box_rocket.edit

interface MyTouchable {
    fun touchDown(x: Float, y: Float): Boolean
    fun touchDragged(x: Float, y: Float): Boolean
    fun touchUp(x: Float, y: Float): Boolean
}