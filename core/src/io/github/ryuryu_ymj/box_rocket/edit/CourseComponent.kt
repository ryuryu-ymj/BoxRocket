package io.github.ryuryu_ymj.box_rocket.edit

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor

const val COMPONENT_UNIT_SIZE = 1f

class CourseComponent(
    asset: AssetManager,
    val type: CourseComponentType,
    val ix: Int, val iy: Int
) : Actor() {
    val contact = BooleanArray(4) { false }
    private val texture: Texture = asset.get(type.texturePath)

    init {
        setPosition(ix * COMPONENT_UNIT_SIZE, iy * COMPONENT_UNIT_SIZE)
        setSize(COMPONENT_UNIT_SIZE, COMPONENT_UNIT_SIZE)
        setOrigin(width / 2, height / 2)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(texture, x, y, width, height)
    }

    fun toCourseComponentData() =
        CourseComponentData(type, ix, iy)

    fun setContact(components: List<CourseComponent>) {
        contact[0] = components.findAt(ix + 1, iy) != null
        contact[1] = components.findAt(ix, iy + 1) != null
        contact[2] = components.findAt(ix - 1, iy) != null
        contact[3] = components.findAt(ix, iy - 1) != null
    }
}

fun List<CourseComponent>.findAt(ix: Int, iy: Int) = find {
    ix == it.ix && iy == it.iy
}

class CourseComponentData(
    private val type: CourseComponentType = CourseComponentType.GROUND,
    private val ix: Int = 0,
    private val iy: Int = 0
) {
    fun toCourseComponent(asset: AssetManager) =
        CourseComponent(asset, type, ix, iy)
}

enum class CourseComponentType(
    val texturePath: String
) {
    GROUND("image/ground.png"),
    START("image/start.png")
}