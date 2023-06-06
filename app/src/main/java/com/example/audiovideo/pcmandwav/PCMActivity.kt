package com.example.audiovideo.pcmandwav

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import androidx.core.app.ActivityCompat
import com.example.audiovideo.R
import com.example.audiovideo.databinding.ActivityMainListBinding
import com.example.audiovideo.databinding.ActivityPcmactivityBinding
import java.io.DataInput
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PCMActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPcmactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPcmactivityBinding.inflate(layoutInflater)
        val view  = binding.root
        setContentView(view)
        binding.btnRecord.setOnClickListener{
            record()
        }
        binding.btnPlay.setOnClickListener{
            play()
        }

    }
    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
        sampleRate,channelConfig,
        audioFormat,bufferSize)

    private var isRecording = true
    private lateinit var file:File
    private fun record(){
        val fileName = "record.pcm"
        file = File(this.filesDir, fileName)
        val outputStream = DataOutputStream(FileOutputStream(file))
       audioRecord.startRecording()
        val buffer = ByteArray(bufferSize)
        Thread{
            while (isRecording){
                val bytesRead = audioRecord.read(buffer,0,bufferSize)
                if (bytesRead > 0){
                    outputStream.write(buffer,0,bytesRead)
                }
            }
            outputStream.close()
        }.start()
    }
    private fun stopRecording(){
        isRecording = false
        audioRecord.stop()
        audioRecord.release()
    }
    private fun play(){
        stopRecording()
        val bufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM
        )
        try{
            val fileInputStream  = FileInputStream("record.pcm")
            val dataInputStream  = DataInputStream(fileInputStream)

            audioTrack.play()
            val buffer = ByteArray(bufferSize)
            var bytesRead: Int
            while (dataInputStream.available() > 0){
                bytesRead = dataInputStream.read(buffer,0,buffer.size)
                if (bytesRead != -1){
                    audioTrack.write(buffer,0,bytesRead)
                }
            }
            audioTrack.stop()
            audioTrack.release()
            dataInputStream.close()
            fileInputStream.close()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}