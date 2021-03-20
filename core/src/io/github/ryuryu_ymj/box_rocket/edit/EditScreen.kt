package io.github.ryuryu_ymj.box_rocket.edit

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.box_rocket.MyGame
import io.github.ryuryu_ymj.box_rocket.play.PlayScreen
import io.github.ryuryu_ymj.box_rocket.play.courseIndex
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textField
import kotlin.math.max
import kotlin.math.min

class EditScreen(private val game: MyGame) : KtxScreen, MyTouchable {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(25.6f, 14.4f)
    private val viewport = FitViewport(
        camera.viewportWidth,
        camera.viewportHeight, camera
    )
    private val stage = Stage(viewport, batch)
    private val uiViewport = FitViewport(1600f, 900f)
    private val uiStage = Stage(uiViewport, batch)
    private val input = InputMultiplexer().also {
        it.addProcessor(uiStage)
        it.addProcessor(stage)
        it.addProcessor(MyInputProcessor(viewport, this))
    }
    private val shape = ShapeRenderer()

    private val bg = BackGround(stage.width, stage.height)
    private val courseComponents = mutableListOf<CourseComponent>()
    private lateinit var tee: CourseComponent
    private lateinit var hole: CourseComponent

    private var isSelecting = false
    private val selectBegin = vec2()
    private val selectEnd = vec2()

    private val brush = Brush()
    private val courseIndexField: TextField

    init {
        stage.addActor(bg)

        //uiStage.addActor(brush)
        uiStage.actors {
            table {
                setFillParent(true)
                //debug = true
                top()
                add(brush).expandX().left()
                courseIndexField = textField(text = courseIndex.toString()) {
                    it.expandX().right()
                }
            }
        }
    }

    override fun show() {
        stage.addActor(bg)
        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        courseComponents.clear()
        stage.clear()
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
        uiViewport.update(width, height)
    }

    override fun render(delta: Float) {
        stage.draw()
        uiStage.draw()
        if (isSelecting) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shape.use(ShapeRenderer.ShapeType.Filled, camera.combined) {
                it.setColor(1f, 0f, 0f, 0.2f)
                it.rect(
                    selectBegin.x, selectBegin.y,
                    selectEnd.x - selectBegin.x, selectEnd.y - selectBegin.y
                )
            }
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }

        if (brush.type != BrushType.MOVE) {
            val margin = viewport.screenWidth / 8
            val speed = 0.1f
            if (Gdx.input.x < margin) {
                camera.position.x -= speed
            } else if (Gdx.input.x > viewport.screenWidth - margin) {
                camera.position.x += speed
            }
            if (Gdx.input.y < margin) {
                camera.position.y += speed
            } else if (Gdx.input.y > viewport.screenHeight - margin) {
                camera.position.y -= speed
            }
        }
        stage.act()
        uiStage.act()
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.S)
        ) {
            // save
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.A)
        ) {
            // clear course components
            courseComponents.forEach {
                if (it !== tee && it !== hole) {
                    it.remove()
                }
            }
            courseComponents.clear()
            courseComponents.add(tee)
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.P)
        ) {
            // move to playScreen
            game.setScreen<PlayScreen>()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) &&
            courseIndexField.hasKeyboardFocus()
        ) {
            uiStage.unfocusAll()
            // open a new course
            try {
                courseIndex = courseIndexField.text.toInt()
                game.setScreen<EditScreen>()
            } catch (e: NumberFormatException) {
                println("invalid input : " + courseIndexField.text)
            }
        }
    }

    override fun touchDown(x: Float, y: Float): Boolean {
        if (brush.type == BrushType.MOVE) return false
        selectBegin.set(x, y)
        selectEnd.set(x, y)
        isSelecting = true
        return true
    }

    override fun touchDragged(x: Float, y: Float): Boolean {
        if (isSelecting) {
            selectEnd.set(x, y)
            return true
        }
        return false
    }

    override fun touchUp(x: Float, y: Float): Boolean {
        if (isSelecting) {
            isSelecting = false
            val beginIX = MathUtils.floor(selectBegin.x / COMPONENT_UNIT_SIZE)
            val beginIY = MathUtils.floor(selectBegin.y / COMPONENT_UNIT_SIZE)
            val endIX = MathUtils.floor(selectEnd.x / COMPONENT_UNIT_SIZE)
            val endIY = MathUtils.floor(selectEnd.y / COMPONENT_UNIT_SIZE)
            val rangeX = min(beginIX, endIX)..max(beginIX, endIX)
            val rangeY = min(beginIY, endIY)..max(beginIY, endIY)

            when (brush.type) {
                BrushType.DELETE -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            removeCourseComponent(ix, iy)
                        }
                    }
                }
                BrushType.GROUND -> {
                    for (ix in rangeX) {
                        for (iy in rangeY) {
                            addCourseComponent(CourseComponentType.GROUND, ix, iy)
                        }
                    }
                }
            }
            return true
        }
        return false
    }

    private fun addCourseComponent(
        type: CourseComponentType, ix: Int, iy: Int
    ): CourseComponent? {
        val old = courseComponents.findAt(ix, iy)
        if (old != null) return null
        val new = CourseComponent(game.asset, type, ix, iy)
        stage.addActor(new)
        courseComponents.add(new)
        return new
    }

    private fun removeCourseComponent(ix: Int, iy: Int): Boolean {
        val old = courseComponents.findAt(ix, iy) ?: return false
        if (old === tee) return false
        old.remove()
        courseComponents.remove(old)
        return true
    }

    override fun dispose() {
        bg.dispose()
        shape.dispose()
        stage.dispose()
        uiStage.dispose()
        batch.dispose()
    }
}