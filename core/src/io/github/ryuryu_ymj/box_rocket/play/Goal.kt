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
import ktx.box2d.box

class Goal(asset: AssetManager, world: World, x: Float, y: Float) : Actor() {
    private val region: TextureRegion =
        asset.get<TextureAtlas>("atlas/play.atlas").findRegion("goal")

    init {
        setSize(region.regionWidth * TEXEL, region.regionHeight * TEXEL)
        setOrigin(width / 2, height / 2)
        setPosition(x + COMPONENT_UNIT_SIZE / 2 - originX, y)

        world.body {
            box(width, 9 * TEXEL) {
                userData = ContactInfo.GOAL
            }
            type = BodyDef.BodyType.StaticBody
            position.set(x + COMPONENT_UNIT_SIZE / 2, y + 4.5f * TEXEL)
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(region, x, y, width, height)
    }
}