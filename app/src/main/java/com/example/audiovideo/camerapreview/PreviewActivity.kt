package com.example.audiovideo.camerapreview

import android.R
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.TextureView.SurfaceTextureListener
import androidx.appcompat.app.AppCompatActivity
import com.example.audiovideo.databinding.ActivityPreviewBinding
import java.io.IOException


class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var camera: Camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        camera = Camera.open()
        val parameters = camera.parameters
        val allSizes: List<Camera.Size> = parameters.getSupportedPictureSizes()
        var size: Camera.Size = allSizes[0] // get top size

        for (i in allSizes.indices) {
            if (allSizes[i].width > size.width) size = allSizes[i]
        }
        //set max Picture Size
        parameters.setPictureSize(size.width, size.height)
        parameters.previewFormat = ImageFormat.NV21 // 设置预览格式为 NV21
        camera.parameters = parameters
        val surfaceView = binding.svPreview
        camera.setPreviewDisplay(surfaceView.holder)
        val textureView = binding.tvPreview
        val  mSurfaceCallback= object :SurfaceHolder.Callback{
            override fun surfaceCreated(p0: SurfaceHolder) {
                camera.setPreviewDisplay(surfaceView.holder)
                camera.startPreview();
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {

            }

        }
        textureView.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                try {
                    camera.setPreviewTexture(surfaceTexture)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } // 其他回调方法

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
               return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

            }
        }
        camera.setPreviewCallback { data, camera ->
            // 在这里处理预览数据，data 参数即为 NV21 格式的数据
        }
        surfaceView.holder.addCallback(mSurfaceCallback)
    }

    override fun onPause() {
        super.onPause()
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();

    }
}