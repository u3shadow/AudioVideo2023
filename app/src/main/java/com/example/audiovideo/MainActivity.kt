package com.example.audiovideo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.audiovideo.camerapreview.PreviewActivity
import com.example.audiovideo.databinding.ActivityMainListBinding
import com.example.audiovideo.image.BitmapActivity
import com.example.audiovideo.mp4.Mp4Handler
import com.example.audiovideo.opengl.MyGLActivity
import com.example.audiovideo.pcmandwav.PCMActivity
import com.example.audiovideo.pcmandwav.WAVActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainListBinding.inflate(layoutInflater)
        val view  = binding.root
        setContentView(view)
        val PERMISSION_REQUEST_CODE = 1
        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        binding.btnShowImage.setOnClickListener {
                val intent = Intent(this,BitmapActivity::class.java)
                startActivity(intent)
        }
        binding.btnPcm.setOnClickListener {
            val intent = Intent(this,PCMActivity::class.java)
            startActivity(intent)
        }
        binding.btnWav.setOnClickListener {
            val intent = Intent(this,WAVActivity::class.java)
            startActivity(intent)
        }
        binding.btnPreview.setOnClickListener {
            val intent = Intent(this,PreviewActivity::class.java)
            startActivity(intent)
        }
        binding.btnExtraMp4.setOnClickListener {
            Mp4Handler.muxerMediaVideo("/input.mp4","/output_video.mp4")
            Mp4Handler.muxerMediaAudio("/input.mp4","/output_audio.mp4")
        }
        binding.btnMuxMp4.setOnClickListener {
            Mp4Handler.muxerAudioVideo(
                Environment.getExternalStorageDirectory().absoluteFile
                .toString() + "/output_video.mp4",Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + "/output_audio.mp4","/output.mp4")
        }
        binding.btnShowTriangle.setOnClickListener {
            startActivity(Intent(this,MyGLActivity::class.java))
        }
    }
}