package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import kotlin.math.floor
import kotlin.math.round

class Background(asset: AssetManager) : Group() {
    init {
        addActor(BGLayer(asset.get<TextureAtlas>("atlas/play.atlas").findRegion("bg2"), 0.1f))
        addActor(BGLayer(asset.get<TextureAtlas>("atlas/play.atlas").findRegion("bg1"), 0.2f))
    }
}

private class BGLayer(
    private val region: TextureRegion,
    private val speedRatio: Float
) : Actor() {
    init {
        setSize(region.regionWidth * TEXEL, region.regionHeight * TEXEL)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(region, x, y, width, height)
        batch.draw(region, x - width, y, width, height)
        batch.draw(region, x, y - height, width, height)
        batch.draw(region, x - width, y - height, width, height)
    }

    override fun act(delta: Float) {
        super.act(delta)
        setPosition(
            stage.camera.position.x * speedRatio,
            stage.camera.position.y * speedRatio
        )
        moveBy(
            -floor((x - stage.camera.position.x + stage.width / 2) / width) * width,
            -floor((y - stage.camera.position.y + stage.height / 2) / height) * height,
        )
        setPosition(
            round(x / pixel) * pixel,
            round(y / pixel) * pixel
        )
    }
}