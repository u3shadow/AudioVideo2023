package com.example.audiovideo.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.audiovideo.R

class BitmapActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val surfaceView:SurfaceView = findViewById(R.id.surfaceView)
        val holder:SurfaceHolder = surfaceView.holder
        holder.addCallback(object :SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
                val canvas:Canvas =holder.lockCanvas()
                val image: Bitmap = BitmapFactory.decodeResource(resources,
                    R.drawable.pic)
                canvas.drawBitmap(image,0f,0f,null)
                holder.unlockCanvasAndPost(canvas)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
               
            }

        })
        
    }
}