package com.example.audiovideo.camerapreview

import android.R
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.TextureView.SurfaceTextureListener
import androidx.appcompat.app.AppCompatActivity
import com.example.audiovideo.databinding.ActivityPreviewBinding
import java.io.IOException


class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    private lateinit var camera:Camera
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        camera = Camera.open()
        val parameters = camera.parameters
        parameters.setPreviewSize(200, 200) // 设置预览尺寸，根据需要设置合适的宽度和高度
        parameters.previewFormat = ImageFormat.NV21 // 设置预览格式为 NV21
        camera.parameters = parameters
        val surfaceView = binding.svPreview
        camera.setPreviewDisplay(surfaceView.holder)
        val textureView = binding.tvPreview
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
        camera.startPreview();
    }

    override fun onPause() {
        super.onPause()
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();

    }
}