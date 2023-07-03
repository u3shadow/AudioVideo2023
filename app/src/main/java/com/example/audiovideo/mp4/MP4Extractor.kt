package com.example.audiovideo.mp4

import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.nio.ByteBuffer


object MP4Extractor {
    var videoOutputStream: FileOutputStream? = null
    var audioOutputStream: FileOutputStream? = null
    lateinit var mediaExtractor:MediaExtractor
    fun extract() {
        try {
            mediaExtractor = MediaExtractor()
            initFIle()
            val (audioTrackIndex, videoTrackIndex) = initTrackIndex()
            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            //切换到视频信道
            extract(mediaExtractor, videoTrackIndex, byteBuffer, videoOutputStream)
            //切换到音频信道
            extract(mediaExtractor, audioTrackIndex, byteBuffer, audioOutputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaExtractor.release()
            try {
                videoOutputStream!!.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initTrackIndex(): Pair<Int, Int> {
        val trackCount = mediaExtractor.trackCount
        var audioTrackIndex = -1
        var videoTrackIndex = -1
        for (i in 0 until trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(i)
            val mineType = trackFormat.getString(MediaFormat.KEY_MIME)
            //视频信道
            if (mineType!!.startsWith("video/")) {
                videoTrackIndex = i
            }
            //音频信道
            if (mineType.startsWith("audio/")) {
                audioTrackIndex = i
            }
        }
        return Pair(audioTrackIndex, videoTrackIndex)
    }

    private fun initFIle() {
        val videoFile = File(
            Environment.getExternalStorageDirectory().absoluteFile,
            "output_video.mp4"
        )
        val audioFile = File(
            Environment.getExternalStorageDirectory().absoluteFile,
            "output_audio"
        )
        if (!videoFile.exists()){
            videoFile.createNewFile()
        }else{
           videoFile.delete()
            videoFile.createNewFile()
        }
        if (!audioFile.exists()){
            audioFile.createNewFile()
        }else{
            audioFile.delete()
            audioFile.createNewFile()
        }
        videoOutputStream = FileOutputStream(videoFile)
        audioOutputStream = FileOutputStream(audioFile)
        mediaExtractor.setDataSource(
            Environment.getExternalStorageDirectory().absoluteFile
                .toString() + "/input.mp4"
        )
    }

    private fun extract(
        mediaExtractor: MediaExtractor,
        videoTrackIndex: Int,
        byteBuffer: ByteBuffer,
        outputStream: FileOutputStream?
    ) {
        mediaExtractor.selectTrack(videoTrackIndex)
        while (true) {
            val readSampleCount = mediaExtractor.readSampleData(byteBuffer, 0)
            if (readSampleCount < 0) {
                break
            }
            //保存视频信道信息
            val buffer = ByteArray(readSampleCount)
            byteBuffer[buffer]
            outputStream?.write(buffer)
            byteBuffer.clear()
            mediaExtractor.advance()
        }
    }

}