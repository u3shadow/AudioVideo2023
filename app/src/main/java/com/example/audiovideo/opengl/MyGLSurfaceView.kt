package com.example.audiovideo.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        // 创建OpenGL ES 2.0上下文
        setEGLContextClientVersion(2)

        // 初始化渲染器
        renderer = MyGLRenderer()
        setRenderer(renderer)
    }
}
