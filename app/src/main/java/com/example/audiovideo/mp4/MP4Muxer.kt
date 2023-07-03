package com.example.audiovideo.mp4

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer

object MP4Muxer {
    private var mediaExtractor: MediaExtractor?  = null
    private var mediaMuxer: MediaMuxer?  = null
    fun muxerMP4(){
        try{
            val file = File(Environment.getExternalStorageDirectory().absoluteFile
                .toString() + "/mux_video.mp4")
            file.createNewFile()
            mediaExtractor = getMediaExtractor(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + "/mux_video.mp4"
            )
            val videoIndex: Int = getIndex(mediaExtractor!!, "video/")
            //切换道视频信号的信道
            mediaExtractor?.selectTrack(videoIndex)
            val trackFormat = mediaExtractor?.getTrackFormat(videoIndex)
            mediaMuxer = MediaMuxer(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + "/mux_output.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
            val videoSampleTime: Int
            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            videoSampleTime = trackFormat!!.getInteger(MediaFormat.KEY_FRAME_RATE)
            val trackIndex = mediaMuxer?.addTrack(trackFormat)
            val bufferInfo = MediaCodec.BufferInfo()
            mediaMuxer?.start()
            bufferInfo.presentationTimeUs = 0
            while (true) {
                val readSampleSize = mediaExtractor?.readSampleData(byteBuffer, 0)
                if (readSampleSize!! < 0) {
                    break
                }
                mediaExtractor?.advance()
                bufferInfo.size = readSampleSize
                bufferInfo.offset = 0
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                bufferInfo.presentationTimeUs += 1000 * 1000 / videoSampleTime.toLong()
                mediaMuxer?.writeSampleData(trackIndex!!, byteBuffer, bufferInfo)
            }
            mediaMuxer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }finally {
            mediaExtractor?.release()
            mediaMuxer?.release()
        }
    }
    @Throws(IOException::class)
    private fun getMediaExtractor(source: String): MediaExtractor {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(source)
        return mediaExtractor
    }
    private fun getIndex(mediaExtractor: MediaExtractor, channal: String): Int {
        var index = -1
        val trackCount = mediaExtractor.trackCount
        for (i in 0 until trackCount) {
            val trackFormat = mediaExtractor.getTrackFormat(i)
            if (trackFormat.getString(MediaFormat.KEY_MIME)!!.startsWith(channal)) {
                index = i
            }
        }
        return index
    }
}