package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Actor
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE
import ktx.assets.pool
import ktx.box2d.*
import ktx.collections.GdxArray
import kotlin.math.round

class Rocket(asset: AssetManager, private val world: World, x: Float, y: Float) : Actor() {
    private val region = asset.get<TextureAtlas>("atlas/play.atlas").findRegion("rocket")
    private val body: Body
    private val horizontalSensor: Fixture
    private var horizontalContact = 0
    private val verticalSensor: Fixture
    private var verticalContact = 0

    private val activeSmokes = GdxArray<Smoke>(8)
    private val smokePool = pool(8) { Smoke(asset) }
    private var counter = 0

    init {
        setSize(COMPONENT_UNIT_SIZE, COMPONENT_UNIT_SIZE)
        setOrigin(width / 2, height / 2)
        setPosition(x, y)
        rotation = 90f
        body = world.body {
            val half = width * 0.985f / 2
            val corner = width / 20 * 1.2f
            polygon(
                floatArrayOf(
                    -half + corner, -half,
                    half - corner, -half,
                    half, -half + corner,
                    half, half - corner,
                    half - corner, half,
                    -half + corner, half,
                    -half, half - corner,
                    -half, -half + corner,
                )
            ) {
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
        activeSmokes.forEach { it.draw(batch, parentAlpha) }
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            rotateBy(-90f)
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            jet()
        }

        val pos = body.position
        x = if (horizontalContact > 0) {
            round(pos.x / TEXEL) * TEXEL - originX
        } else {
            round(pos.x / pixel) * pixel - originX
        }
        y = if (verticalContact > 0) {
            round(pos.y / TEXEL) * TEXEL - originY
        } else {
            round(pos.y / pixel) * pixel - originY
        }

        activeSmokes.forEach {
            it.act(delta)
            if (!it.active) {
                activeSmokes.removeValue(it, true)
                smokePool.free(it)
            }
        }
    }

    private fun jet() {
        val cos = MathUtils.cosDeg(rotation)
        val sin = MathUtils.sinDeg(rotation)

        var minFrac = 1f
        world.rayCast(
            x + originX - width / 2 * cos,
            y + originY - width / 2 * sin,
            x + originX - width * 2 * cos,
            y + originY - width * 2 * sin
        ) { _, _, _, fraction ->
            if (fraction < minFrac) minFrac = fraction
            RayCast.IGNORE
        }
        //println("min=${minFrac}")

        val f = 40f / (minFrac + 0.1f)
        //println("jet: $f, weight: ${body.mass * world.gravity.len()}")
        body.applyForceToCenter(
            f * cos,
            f * sin,
            true
        )

        if (counter % 10 == 0) {
            val rnd = MathUtils.random(width / 2) - width / 4
            smokePool.obtain().let {
                activeSmokes.add(it)
                it.init(
                    x + originX - width / 2 * cos + rnd * sin,
                    y + originY - height / 2 * sin - rnd * cos,
                    rotation + 180
                )
            }
        }
        counter++
    }
}