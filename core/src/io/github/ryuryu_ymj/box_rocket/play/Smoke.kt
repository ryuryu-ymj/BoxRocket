package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Pool

class Smoke(asset: AssetManager) : Actor(), Pool.Poolable {
    private val animation = Array(4) {
        asset.get<TextureAtlas>("atlas/play.atlas").findRegion("smoke$it")
    }
    private var counter = 0f
    private var dx = 0f
    private var dy = 0f
    var active = false; private set

    init {
        setSize(
            animation[0].regionWidth * TEXEL,
            animation[0].regionHeight * TEXEL
        )
        setOrigin(width / 2, height / 2)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            animation[MathUtils.floor(counter)], x, y, originX, originY,
            width, height, scaleX, scaleY, rotation
        )
    }

    override fun act(delta: Float) {
        super.act(delta)
        moveBy(dx, dy)
        if (counter < animation.lastIndex) {
            counter += 0.05f
        } else {
            active = false
        }
    }

    fun init(centerX: Float, centerY: Float, rotation: Float) {
        setPosition(centerX - originX, centerY - originY)
        this.rotation = rotation
        dx = 0.02f * MathUtils.cosDeg(rotation)
        dy = 0.02f * MathUtils.sinDeg(rotation)
        active = true
    }

    override fun reset() {
        counter = 0f
    }
}