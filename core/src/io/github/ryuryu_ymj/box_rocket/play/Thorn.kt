package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE
import ktx.box2d.body
import ktx.box2d.box
import ktx.math.vec2

class Thorn(asset: AssetManager, world: World, x: Float, y: Float, private val dir: Int) : Actor() {
    private val region = if (dir % 2 == 0) {
        asset.get<TextureAtlas>("atlas/play.atlas").findRegion("thorn_edge")
    } else {
        asset.get<TextureAtlas>("atlas/play.atlas").findRegion("thorn_corner")
    }

    init {
        setSize(COMPONENT_UNIT_SIZE, COMPONENT_UNIT_SIZE)
        setOrigin(width / 2, height / 2)
        setPosition(x, y)
        rotation = dir / 2 * 90f

        world.body {
            box(
                width / 2, height,
                position = vec2(-width / 4, 0f)
            ) {
                isSensor = true
                userData = ContactInfo.DAMAGE
            }
            if (dir % 2 == 1) {
                box(
                    width, height / 2,
                    position = vec2(0f, -height / 4)
                ) {
                    isSensor = true
                    userData = ContactInfo.DAMAGE
                }
            }
            position.set(x + originX, y + originY)
            angle = rotation * MathUtils.degreesToRadians
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            region, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation
        )
    }
}