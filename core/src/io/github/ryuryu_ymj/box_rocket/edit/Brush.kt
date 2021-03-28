package io.github.ryuryu_ymj.box_rocket.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.ui.Label
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.defaultStyle

class Brush : Label("MOVE", Scene2DSkin.defaultSkin, defaultStyle) {
    var type: BrushType = BrushType.MOVE; private set

    override fun act(delta: Float) {
        super.act(delta)
        when {
            Gdx.input.isKeyJustPressed(Input.Keys.M) -> {
                type = BrushType.MOVE
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.D) -> {
                type = BrushType.DELETE
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.S) &&
                    !Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) -> {
                type = BrushType.START
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.G) -> {
                type = BrushType.GROUND
                setText(type.name)
            }
            Gdx.input.isKeyJustPressed(Input.Keys.B) -> {
                type = BrushType.BLOCK
                setText(type.name)
            }
        }
    }
}

enum class BrushType {
    MOVE, DELETE,
    START,
    GROUND, BLOCK
}