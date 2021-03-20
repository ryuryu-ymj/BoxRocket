package io.github.ryuryu_ymj.box_rocket

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.box2d.body
import ktx.box2d.box
import ktx.math.vec2

class CourseReader {
    fun readCourse(index: Int, world: World) {
        createBody(index, world)
    }

    private fun createBody(index: Int, world: World) {
        val file: FileHandle
        try {
            file = Gdx.files.internal("course/${"%02d".format(index)}body")
        } catch (e: GdxRuntimeException) {
            Gdx.app.error("my-error", "コースファイルの読み込みに失敗しました", e)
            return
        }
        for (line in file.readString().lines()) {
            val cells = line.split(',')
            if (cells.isEmpty()) continue
            when (cells[0]) {
                "ground" -> {
                    val x = cells[1].toFloat()
                    val y = cells[2].toFloat()
                    val w = cells[3].toFloat()
                    val h = cells[4].toFloat()
                    world.body {
                        box(width = w, height = h, position = vec2(w / 2, h / 2)) {
                            restitution = 0f
                            friction = 0.5f
                        }
                        position.set(x, y)
                        type = BodyDef.BodyType.StaticBody
                    }
                }
            }
        }
    }
}