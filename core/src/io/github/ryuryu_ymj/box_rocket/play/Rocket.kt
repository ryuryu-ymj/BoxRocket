package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE
import ktx.box2d.RayCast
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.rayCast

const val GLOBAL_SCALE = 1.00f

class Rocket(asset: AssetManager, private val world: World, x: Float, y: Float) : Actor() {
    private val region = asset.get<TextureAtlas>("atlas/play.atlas").findRegion("rocket")
    private val body: Body
    private val horizontalSensor: Fixture
    private var horizontalContact = 0
    private val verticalSensor: Fixture
    private var verticalContact = 0

    init {
        setSize(COMPONENT_UNIT_SIZE, COMPONENT_UNIT_SIZE)
        setOrigin(width / 2, height / 2)
        setPosition(x, y)
        setScale(GLOBAL_SCALE)
        rotation = 90f
        body = world.body {
            box(width, height) {
                density = 10f
                friction = 0.5f
            }
            type = BodyDef.BodyType.DynamicBody
            linearDamping = 0.1f
            //angularDamping = 1f
            fixedRotation = true
            position.set(x + originX, y + originY)
        }
        horizontalSensor = body.box(width, height * 0.9f) {
            density = 0f
            isSensor = true
        }
        verticalSensor = body.box(width * 0.9f, height) {
            density = 0f
            isSensor = true
        }

        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                if (contact.fixtureA === horizontalSensor ||
                    contact.fixtureB === horizontalSensor
                ) {
                    horizontalContact++
                } else if (contact.fixtureA === verticalSensor ||
                    contact.fixtureB === verticalSensor
                ) {
                    verticalContact++
                }
            }

            override fun endContact(contact: Contact) {
                if (contact.fixtureA === horizontalSensor ||
                    contact.fixtureB === horizontalSensor
                ) {
                    horizontalContact--
                } else if (contact.fixtureA === verticalSensor ||
                    contact.fixtureB === verticalSensor
                ) {
                    verticalContact--
                }
            }

            override fun preSolve(contact: Contact, oldManifold: Manifold) {
            }

            override fun postSolve(contact: Contact, impulse: ContactImpulse) {
            }
        })
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.draw(
            region, x, y, originX, originY,
            width, height, scaleX, scaleY, rotation
        )
    }

    override fun act(delta: Float) {
        super.act(delta)

        val pos = body.position
        setPosition(
            pos.x.toPixel().toCordi() - originX,
            pos.y.toPixel().toCordi() - originY
        )
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
    }
}

const val PIXEL_SIZE = COMPONENT_UNIT_SIZE / 20
fun Float.toPixel() = MathUtils.round(this / PIXEL_SIZE)
fun Int.toCordi() = this * PIXEL_SIZE