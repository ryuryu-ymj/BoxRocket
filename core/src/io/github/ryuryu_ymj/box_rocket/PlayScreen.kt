package io.github.ryuryu_ymj.box_rocket

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.math.vec2

class PlayScreen : KtxScreen {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(25.6f, 14.4f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)

    private val world = createWorld(vec2(0f, -2f))
    private val debugRenderer = Box2DDebugRenderer()

    private val rocket = Rocket(world, 0f, 0f)

    init {
        camera.position.set(0f, 0f, 0f)
        stage.addActor(rocket)
        Box(world, -9f, -8f, 18f, 1f)
        Box(world, -9f, -9f, 18f, 1f)
        Box(world, -10f, -8f, 1f, 18f)
        Box(world, 9f, -8f, 6f, 2f)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()
        debugRenderer.render(world, camera.combined)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            rocket.rotate()
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            rocket.jet()
        }
        world.step(1f / 60, 6, 2)
        stage.act()
        camera.position.set(rocket.x + rocket.originX, rocket.y + rocket.originY, 0f)
    }

    override fun dispose() {
        debugRenderer.dispose()
        world.dispose()
        stage.dispose()
        batch.dispose()
    }
}