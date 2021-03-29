package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.GdxRuntimeException
import ktx.box2d.body
import ktx.box2d.loop

class CourseReader {
    fun readCourse(index: Int, world: World, stage: Stage, asset: AssetManager) {
        readBody(index, world)
        readActor(index, stage, asset, world)
    }

    private fun readBody(index: Int, world: World) {
        val file: FileHandle
        try {
            file = Gdx.files.internal("course/${"%02d".format(index)}body")
        } catch (e: GdxRuntimeException) {
            Gdx.app.error("my-error", "コースファイルの読み込みに失敗しました", e)
            return
        }
        for (line in file.readString().lines()) {
            if (line.isBlank()) continue
            val cells = line.split(',')
            when (cells[0]) {
                "ground" -> {
                    val vertices = FloatArray(cells.size - 2) {
                        cells[it + 1].toFloat()
                    }
                    world.body {
                        loop(vertices) {
                            restitution = 0f
                            friction = 0.5f
                            userData = ContactInfo.OBSTACLE
                        }
                        type = BodyDef.BodyType.StaticBody
                    }
                }
            }
        }
    }

    private fun readActor(index: Int, stage: Stage, asset: AssetManager, world: World) {
        val file: FileHandle
        try {
            file = Gdx.files.internal("course/${"%02d".format(index)}actor")
        } catch (e: GdxRuntimeException) {
            Gdx.app.error("my-error", "コースファイルの読み込みに失敗しました", e)
            return
        }

        val atlas = asset.get<TextureAtlas>("atlas/play.atlas")
        for (line in file.readString().lines()) {
            if (line.isBlank()) continue
            val cells = line.split(',')
            val name = cells[0]
            if (name.first() == 'g') {
                val region = atlas.findRegion(name)
                val x = cells[1].toFloat()
                val y = cells[2].toFloat()
                stage.addActor(Obstacle(region, x, y))
            } else {
                when (name) {
                    "block" -> {
                        val region = atlas.findRegion(name)
                        val x = cells[1].toFloat()
                        val y = cells[2].toFloat()
                        stage.addActor(Obstacle(region, x, y))
                    }
                    "thorn" -> {
                        val x = cells[1].toFloat()
                        val y = cells[2].toFloat()
                        val dir = cells[3].toInt()
                        stage.addActor(Thorn(asset, world, x, y, dir))
                    }
                }
            }
        }
    }
}