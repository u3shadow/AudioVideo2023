package com.example.audiovideo.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.audiovideo.R

class MyCustomView(context: Context, attrs: AttributeSet? = null) : View(context, attrs){
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val bitmap:Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pic)
        canvas?.drawBitmap(bitmap,0f,0f,null)
    }
}