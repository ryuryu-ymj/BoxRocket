package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import ktx.box2d.*
import ktx.math.vec2

const val ROCKET_SIZE = 1f

class Rocket(private val world: World, x: Float, y: Float) : Actor() {
    private val body: Body

    init {
        setSize(ROCKET_SIZE, ROCKET_SIZE)
        setOrigin(width / 2, height / 2)
        setPosition(x, y)
        rotation = 90f
        body = world.body {
            /*box(width, height) {
                density = 10f
                friction = 0.5f
            }*/
            val cnr = width / 8
            polygon(
                floatArrayOf(
                    -originX + cnr, -originY,
                    originX - cnr, -originY,
                    originX, -originY + cnr,
                    originX, originY - cnr,
                    originX - cnr, originY,
                    -originX + cnr, originY,
                    -originX, originY - cnr,
                    -originX, -originY + cnr,
                )
            ) {
                density = 10f
                friction = 0.5f
            }
            box(
                width = width / 4, height = height / 2,
                position = vec2(-height * 3 / 8, 0f)
            ) {
                density = 0f
            }
            type = BodyDef.BodyType.DynamicBody
            linearDamping = 0.1f
            //angularDamping = 1f
            fixedRotation = true
            position.set(x + originX, y + originY)
        }
        body.setTransform(body.position, rotation * MathUtils.degreesToRadians)
    }

    override fun act(delta: Float) {
        super.act(delta)

        body.position.let { setPosition(it.x - originX, it.y - originY) }
        rotation = body.angle * MathUtils.radiansToDegrees
    }

    fun jet() {
        var minFrac = 1f
        world.rayCast(
            x + originX - width / 2 * MathUtils.cosDeg(rotation),
            y + originY - width / 2 * MathUtils.sinDeg(rotation),
            x + originX - width * 2 * MathUtils.cosDeg(rotation),
            y + originY - width * 2 * MathUtils.sinDeg(rotation)
        ) { _, _, _, fraction ->
            if (fraction < minFrac) minFrac = fraction
            RayCast.IGNORE
        }
        //println("min=${minFrac}")

        val f = 40f / (minFrac + 0.1f)
        //println("jet: $f, weight: ${body.mass * world.gravity.len()}")
        body.applyForceToCenter(
            f * MathUtils.cosDeg(rotation),
            f * MathUtils.sinDeg(rotation),
            true
        )
    }

    fun rotate() {
        rotateBy(-90f)
        body.setTransform(body.position, rotation * MathUtils.degreesToRadians)
    }
}