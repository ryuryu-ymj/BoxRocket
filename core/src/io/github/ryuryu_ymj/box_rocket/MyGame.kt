package io.github.ryuryu_ymj.box_rocket

import io.github.ryuryu_ymj.box_rocket.play.PlayScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen

class MyGame : KtxGame<KtxScreen>() {
    override fun create() {
        addScreen(PlayScreen())
        setScreen<PlayScreen>()
    }
}