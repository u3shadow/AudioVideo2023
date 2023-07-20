package com.example.audiovideo.openglpic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MyGLPicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mGLView = MyGLSurfaceView(this);
        setContentView(mGLView)
    }

}
