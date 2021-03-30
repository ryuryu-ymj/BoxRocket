package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE
import ktx.box2d.body
import ktx.box2d.polygon

class Goal(asset: AssetManager, world: World, x: Float, y: Float) : Actor() {
    private val region: TextureRegion =
        asset.get<TextureAtlas>("atlas/play.atlas").findRegion("goal")

    init {
        setSize(region.regionWidth * TEXEL, region.regionHeight * TEXEL)
        setOrigin(width / 2, height / 2)
        setPosition(x + COMPONENT_UNIT_SIZE / 2 - originX, y)

        world.body {
            polygon(
                floatArrayOf(
                    0f * TEXEL, 0f * TEXEL,
                    16f * TEXEL, 0f * TEXEL,
                    21f * TEXEL, 9f * TEXEL,
                    -5f * TEXEL, 9f * TEXEL,
                )
            ) {
                userData = ContactInfo.GOAL
            }
            type = BodyDef.BodyType.StaticBody
            position.set(x, y)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(region, x, y, width, height)
    }
}