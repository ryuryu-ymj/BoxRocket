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
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.box_rocket.MyGame
import io.github.ryuryu_ymj.box_rocket.play.PlayScreen
import io.github.ryuryu_ymj.box_rocket.play.courseIndex
import ktx.app.KtxScreen
import ktx.graphics.use
import ktx.json.addClassTag
import ktx.json.fromJson
import ktx.math.vec2
import ktx.scene2d.actors
import ktx.scene2d.table
import ktx.scene2d.textField
import java.io.PrintWriter
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
    private var start: CourseComponent? = null

    private var isSelecting = false
    private val selectBegin = vec2()
    private val selectEnd = vec2()

    private val brush = Brush()
    private val courseIndexText: TextField

    private val json = Json().apply {
        addClassTag<CourseComponentData>("CCD")
    }

    init {
        stage.addActor(bg)

        //uiStage.addActor(brush)
        uiStage.actors {
            table {
                setFillParent(true)
                //debug = true
                top()
                add(brush).expandX().left()
                courseIndexText = textField(text = courseIndex.toString()) {
                    it.expandX().right()
                }
            }
        }
    }

    override fun show() {
        stage.addActor(bg)

        try {
            val file = Gdx.files.internal("course/${"%02d".format(courseIndex)}raw")
            val dataList = json.fromJson<Array<CourseComponentData>>(file.readString())
            dataList.forEach {
                val component = it.toCourseComponent(game.asset)
                courseComponents.add(component)
                stage.addActor(component)
                if (component.type == CourseComponentType.START) {
                    start = component
                }
            }
        } catch (e: Exception) {
            println(e)
        }
        if (start == null) {
            start = CourseComponent(game.asset, CourseComponentType.START, 0, 0).also {
                courseComponents.add(it)
                stage.addActor(it)
            }
        }

        start?.let { camera.position.set(it.x + it.originX, it.y + it.originY, 0f) }
        Gdx.input.inputProcessor = input
    }

    override fun hide() {
        start = null
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
                    selectBegin.x,
                    selectBegin.y,
                    selectEnd.x - selectBegin.x,
                    selectEnd.y - selectBegin.y
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
            val data = courseComponents.map { it.toCourseComponentData() }
            val file = Gdx.files.local("course/${"%02d".format(courseIndex)}raw")
            json.toJson(data, file)
            println("save body file to ${file.path()}")

            saveBodyFile()
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.A)
        ) {
            // clear course components
            courseComponents.forEach {
                if (it !== start) {
                    it.remove()
                }
            }
            courseComponents.clear()
            start?.let { courseComponents.add(it) }
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) &&
            Gdx.input.isKeyJustPressed(Input.Keys.P)
        ) {
            // move to playScreen
            game.setScreen<PlayScreen>()
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) &&
            courseIndexText.hasKeyboardFocus()
        ) {
            uiStage.unfocusAll()
            // open a new course
            try {
                courseIndex = courseIndexText.text.toInt()
                game.setScreen<EditScreen>()
            } catch (e: NumberFormatException) {
                println("invalid input : " + courseIndexText.text)
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
        if (old === start) return false
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

    private fun saveBodyFile() {
        val file = Gdx.files.local("course/${"%02d".format(courseIndex)}body")
        val writer = PrintWriter(file.writer(false))

        val start = start ?: return
        val courseComponents = courseComponents.toMutableList()
        courseComponents.forEach {
            it.setContact(courseComponents)
        }

        // horizontal box
        while (true) {
            val left = courseComponents.filter {
                (!it.topContacted || !it.bottomContacted)
            }.minByOrNull {
                it.ix
            } ?: break
            courseComponents.remove(left)
            var iw = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == left.ix + iw && it.iy == left.iy &&
                            (!it.topContacted || !it.bottomContacted)
                } ?: break
                courseComponents.remove(next)
                iw++
            }
            val x = (left.ix - start.ix) * COMPONENT_UNIT_SIZE
            val y = (left.iy - start.iy - 1) * COMPONENT_UNIT_SIZE
            val w = iw * COMPONENT_UNIT_SIZE
            val h = 1 * COMPONENT_UNIT_SIZE
            writer.println("ground,$x,$y,$w,$h,")
        }

        // vertical box
        while (true) {
            val bottom = courseComponents.filter {
                (!it.rightContacted || !it.leftContacted)
            }.minByOrNull {
                it.iy
            } ?: break
            courseComponents.remove(bottom)
            var ih = 1
            while (true) {
                val next = courseComponents.find {
                    it.ix == bottom.ix && it.iy == bottom.iy + ih &&
                            (!it.rightContacted || !it.leftContacted)
                } ?: break
                courseComponents.remove(next)
                ih++
            }
            val x = (bottom.ix - start.ix) * COMPONENT_UNIT_SIZE
            val y = (bottom.iy - start.iy - 1) * COMPONENT_UNIT_SIZE
            val w = 1 * COMPONENT_UNIT_SIZE
            val h = ih * COMPONENT_UNIT_SIZE
            writer.println("ground,$x,$y,$w,$h,")
        }

        writer.close()
        println("save body file to course/${"%02d".format(courseIndex)}body")
    }
}