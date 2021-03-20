package io.github.ryuryu_ymj.box_rocket

import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.body
import ktx.box2d.box

class Box(
    world: World,
    x: Float, y: Float,
    width: Float, height: Float
) : Actor() {
    init {
        setSize(width, height)
        setOrigin(width / 2, height / 2)
        setPosition(x, y)

        world.body {
            box(width, height) {
                friction = 0.5f
            }
            type = BodyDef.BodyType.StaticBody
            userData = this@Box
            position.set(x + originX, y + originY)
        }
    }
}