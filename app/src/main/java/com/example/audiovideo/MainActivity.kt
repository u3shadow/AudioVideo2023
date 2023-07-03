package com.example.audiovideo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.audiovideo.camerapreview.PreviewActivity
import com.example.audiovideo.databinding.ActivityMainListBinding
import com.example.audiovideo.image.BitmapActivity
import com.example.audiovideo.mp4.MP4Extractor
import com.example.audiovideo.mp4.MP4Muxer
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }else{
            val intent  = Intent()
            intent.action = ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(intent)
        }
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
            MP4Extractor.extract()
        }
        binding.btnMuxMp4.setOnClickListener {
           MP4Muxer.muxerMP4()
        }
    }
}