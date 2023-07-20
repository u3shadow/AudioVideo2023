package com.example.audiovideo.openglpic

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.opengl.GLSurfaceView
import com.example.audiovideo.R
import com.example.audiovideo.opengl.MyGLRenderer


/**
 * Created by u3-linux on 18-3-1.
 */
internal class MyGLSurfaceView(context: Context?) : GLSurfaceView(context) {
    // private final MyGLRenderer mRenderer;
    private val mRenderer: MyGLRenderer
    private val mPicRenderer: com.example.audiovideo.openglpic.MyGLRenderer

    init {

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2)

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = MyGLRenderer()
        val db = resources.getDrawable(R.drawable.pic)
        val drawable = db as BitmapDrawable
        val bitmap = drawable.bitmap
        mPicRenderer = MyGLRenderer(bitmap)
        setRenderer(mPicRenderer)

        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}