package com.example.audiovideo.opengl

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MyGLActivity : AppCompatActivity() {
    private var glSurfaceView: MyGLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建MyGLSurfaceView
        glSurfaceView = MyGLSurfaceView(this)

        // 设置为Activity的内容视图
        setContentView(glSurfaceView)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView?.onPause()
    }
}
