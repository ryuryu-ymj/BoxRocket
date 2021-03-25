package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE

class Ground(private val region: TextureRegion, x: Float, y: Float) : Actor() {
    init {
        setPosition(x, y)
        setSize(COMPONENT_UNIT_SIZE, COMPONENT_UNIT_SIZE)
        setScale(GLOBAL_SCALE)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            region, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation
        )
    }
}