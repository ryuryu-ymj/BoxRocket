package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE

class Ground(private val region: TextureRegion, x: Float, y: Float) : Actor() {
    init {
        setSize(
            COMPONENT_UNIT_SIZE * region.regionWidth / 16,
            COMPONENT_UNIT_SIZE * region.regionHeight / 16
        )
        setPosition(x, y - height + COMPONENT_UNIT_SIZE)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(region, x, y, width, height)
    }
}