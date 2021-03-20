package io.github.ryuryu_ymj.box_rocket.desktop

import kotlin.jvm.JvmStatic
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import io.github.ryuryu_ymj.box_rocket.MyGame

object DesktopLauncher {
    @JvmStatic
    fun main(arg: Array<String>) {
        LwjglApplication(
            MyGame(),
            LwjglApplicationConfiguration().apply {
                width = 800
                height = 450
            })
    }
}