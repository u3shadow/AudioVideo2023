package com.example.audiovideo.pcmandwav

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.audiovideo.R
import com.example.audiovideo.databinding.ActivityPcmactivityBinding
import com.example.audiovideo.databinding.ActivityWavactivityBinding

class WAVActivity : AppCompatActivity() {
    private lateinit var binding:ActivityWavactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWavactivityBinding.inflate(layoutInflater)
        val  view = binding.root
        setContentView(view)
        val wavTool = WAVTool(this)
        binding.btnRecord.setOnClickListener{
            wavTool.stopPlay()
         wavTool.startRecord()
        }
        binding.btnPlay.setOnClickListener{
            wavTool.stopRecord()
            wavTool.startPlay()
        }

    }
}