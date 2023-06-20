package com.example.audiovideo

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.audiovideo.camerapreview.PreviewActivity
import com.example.audiovideo.databinding.ActivityMainListBinding
import com.example.audiovideo.image.BitmapActivity
import com.example.audiovideo.pcmandwav.PCMActivity
import com.example.audiovideo.pcmandwav.WAVActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainListBinding.inflate(layoutInflater)
        val view  = binding.root
        setContentView(view)
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
    }
}