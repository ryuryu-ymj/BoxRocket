package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.box_rocket.MyGame
import io.github.ryuryu_ymj.box_rocket.edit.COMPONENT_UNIT_SIZE
import io.github.ryuryu_ymj.box_rocket.edit.EditScreen
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.math.vec2
import kotlin.math.round

const val TEXEL = COMPONENT_UNIT_SIZE / 16
var pixel = 0f; private set

var courseIndex = 1

class PlayScreen(private val game: MyGame) : KtxScreen {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(20f, 20f * 9 / 16)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)

    private val gravity = vec2(0f, -2f)
    private lateinit var world: World
    private val debugRenderer = Box2DDebugRenderer()

    private val course = CourseReader()
    private lateinit var rocket: Rocket
    private val bg = Background(game.asset)

    init {
        pixel = stage.height / viewport.screenHeight
    }

    override fun show() {
        stage.addActor(bg)
        world = createWorld(gravity)
        course.readCourse(courseIndex, world, stage, game.asset)
        rocket = Rocket(game.asset, world, 0f, 0f)
        camera.position.set(rocket.x + rocket.originX, rocket.y + rocket.originY, 0f)
        stage.addActor(rocket)
    }

    override fun hide() {
        world.dispose()
        stage.clear()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        pixel = stage.height / viewport.screenHeight
    }

    override fun render(delta: Float) {
        stage.draw()
        //debugRenderer.render(world, camera.combined)

        world.step(1f / 60, 6, 2)
        stage.act()
        //camera.position.set(rocket.x + rocket.originX, rocket.y + rocket.originY, 0f)
        camera.position.set(
            round((rocket.x + rocket.originX) / pixel) * pixel,
            round((rocket.y + rocket.originY) / pixel) * pixel,
            0f
        )

        if (!rocket.isAlive) {
            game.setScreen<PlayScreen>()
        } else if (rocket.isGoal) {
            courseIndex++
            game.setScreen<PlayScreen>()
        }

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.E)
        ) {
            game.setScreen<EditScreen>()
        }
    }

    override fun dispose() {
        debugRenderer.dispose()
        //world.dispose()
        stage.dispose()
        batch.dispose()
    }
}