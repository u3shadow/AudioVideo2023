package com.example.audiovideo.mp4

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel





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
        val oriAudioFormat = mediaExtractor.getTrackFormat(videoTrackIndex)
        var maxBufferSize = 100 * 1000;
        if (oriAudioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            maxBufferSize = oriAudioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        }
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)
        val mediaCodec =
            MediaCodec.createDecoderByType(oriAudioFormat.getString(MediaFormat.KEY_MIME)!!)
        mediaCodec.configure(oriAudioFormat, null, null, 0)
        val writeChannel: FileChannel = outputStream!!.channel
        mediaCodec.start()
        val info = MediaCodec.BufferInfo()
        var outputBufferIndex = -1
        while (true) {
            val decodeInputIndex = mediaCodec.dequeueInputBuffer(0)
            if (decodeInputIndex >= 0) {
                val sampleTimeUs = mediaExtractor.sampleTime
                if (sampleTimeUs == (-1).toLong()) {
                    break
                } else if (sampleTimeUs > 6000000) {
                    break
                }
                info.size = mediaExtractor.readSampleData(buffer, 0)
                info.presentationTimeUs = sampleTimeUs
                info.flags = mediaExtractor.sampleFlags
                val content = ByteArray(buffer.remaining())
                buffer.get(content)
                val inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex)
                inputBuffer?.put(content)
                mediaCodec.queueInputBuffer(
                    decodeInputIndex,
                    0,
                    info.size,
                    info.presentationTimeUs,
                    info.flags
                )
                mediaExtractor.advance()
            }
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
            while (outputBufferIndex >= 0) {
                val decodeOutputBuffer = mediaCodec . getOutputBuffer (outputBufferIndex);
                writeChannel.write(decodeOutputBuffer);//pcm
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, 100_000);
            }
        }
        writeChannel.close()
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()
    }

}