package io.github.ryuryu_ymj.box_rocket

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import io.github.ryuryu_ymj.box_rocket.edit.EditScreen
import io.github.ryuryu_ymj.box_rocket.play.PlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.assets.load
import ktx.scene2d.Scene2DSkin

class MyGame : KtxGame<KtxScreen>() {
    val asset = AssetManager()

    override fun create() {
        asset.load<Texture>("image/ground.png")
        asset.load<Texture>("image/start.png")
        asset.load<Texture>("image/rocket.png")
        asset.load<TextureAtlas>("atlas/ground.atlas")
        asset.load<Skin>("skin/test-skin.json")

        asset.finishLoading()

        Scene2DSkin.defaultSkin = asset.get("skin/test-skin.json")

        addScreen(PlayScreen(this))
        addScreen(EditScreen(this))
        setScreen<EditScreen>()
    }
}