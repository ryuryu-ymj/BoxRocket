package io.github.ryuryu_ymj.box_rocket.play

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.Shader
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.ryuryu_ymj.box_rocket.MyGame
import io.github.ryuryu_ymj.box_rocket.edit.EditScreen
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.math.vec2

var courseIndex = 1

class PlayScreen(private val game: MyGame) : KtxScreen {
    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(25.6f, 14.4f)
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

    init {
        /*val vert = """
            attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};    
            attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
            attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE};
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            void main() {
                v_color = ${ShaderProgram.COLOR_ATTRIBUTE}; 
                v_texCoords = ${ShaderProgram.TEXCOORD_ATTRIBUTE}; 
                gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};  
            }
            """
        val frag = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            void main() {
                gl_FragColor = v_color * texture2D(u_texture, v_texCoords);
            }
            """
        /*"""
        precision highp float;
        uniform sampler2D tex;
        uniform vec2 inv_tex_dims;
        varying vec2 fs_inv_tex_sample_dims;
        varying vec2 fs_tex_vert_rb;
        varying float fs_flash_ratio;
        const vec4 WHITE = vec4(1.0, 1.0, 1.0, 1.0);
        void main() {
            vec2 mid = floor(fs_tex_vert_rb);
            vec2 sample_coords = mid - 0.5 +
                min((fs_tex_vert_rb - mid) * fs_inv_tex_sample_dims, 1.0);
            vec4 color = texture2D(tex, sample_coords * inv_tex_dims);
            gl_FragColor = mix(color, WHITE * color[3], fs_flash_ratio);
        }
    """.trimIndent()*/

        /**/
        batch.shader = ShaderProgram(vert, frag)*/

        val vertex = """
            attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
            attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
            attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
            uniform mat4 u_projTrans;
            varying vec4 v_color;
            varying vec2 v_texCoords;
            
            void main()
            {
                v_color = ${ShaderProgram.COLOR_ATTRIBUTE};
                v_color.a = v_color.a * (255.0/254.0);
                v_texCoords = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
                gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
            }
            """.trimIndent()

        val fragment = """
            #ifdef GL_ES
            #define LOWP lowp
            precision mediump float;
            #else
            #define LOWP 
            #endif
            
            varying LOWP vec4 v_color;
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform float texelsPerPixel;
            void main()
            {
                vec2 texSize = textureSize(u_texture, 0);
                vec2 locationWithinTexel = fract(v_texCoords * texSize);
                vec2 interpolationAmount = clamp(locationWithinTexel / texelsPerPixel, 0.0, 0.5) + 
                    clamp((locationWithinTexel - 1.0) / texelsPerPixel + 0.5, 0.0, 0.5);
                vec2 finalTexCoords = (floor(v_texCoords * texSize) + interpolationAmount) / 
                    texSize;
                gl_FragColor = v_color * texture2D(u_texture, finalTexCoords);
            }
            """.trimIndent()
        batch.shader = ShaderProgram(vertex, fragment)
        batch.shader.setUniformf("texelsPerPixel", 288f / Gdx.graphics.width)
        //batch.shader.setVertexAttribute()
    }

    override fun show() {
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
        batch.shader.setUniformf("texelsPerPixel", 288f / width)
    }

    override fun render(delta: Float) {
        stage.draw()
        //debugRenderer.render(world, camera.combined)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            rocket.rotate()
        } else if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            rocket.jet()
        }
        world.step(1f / 60, 6, 2)
        stage.act()
        camera.position.set(rocket.x + rocket.originX, rocket.y + rocket.originY, 0f)

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