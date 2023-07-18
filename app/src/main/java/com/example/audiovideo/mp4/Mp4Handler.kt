package com.example.audiovideo.mp4

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer

object Mp4Handler {

    //拆分视频信息
    fun muxerMediaVideo(source: String,fileName: String) {
        try {
            val mediaExtractor: MediaExtractor? = getMediaExtractor(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + source
            )
            val mediaMuxer = MediaMuxer(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
            val videoIndex: Int = getIndex(mediaExtractor!!, "video/")
            //切换道视频信号的信道
            mediaExtractor.selectTrack(videoIndex)
            val trackFormat = mediaExtractor.getTrackFormat(videoIndex)

            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            val videoSampleTime: Int = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            val trackIndex = mediaMuxer.addTrack(trackFormat)
            val bufferInfo = MediaCodec.BufferInfo()
            mediaMuxer.start()
            bufferInfo.presentationTimeUs = 0
            while (true) {
                val readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0)
                if (readSampleSize < 0) {
                    break
                }
                mediaExtractor.advance()
                bufferInfo.size = readSampleSize
                bufferInfo.offset = 0
                bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME
                bufferInfo.presentationTimeUs += 1000 * 1000 / videoSampleTime.toLong()
                mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo)
            }
            //release
            mediaMuxer.stop()
            mediaExtractor.release()
            mediaMuxer.release()
            Log.e("TAG", "finish")
        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    //拆分音频信息
    fun muxerMediaAudio(source: String,fileName:String) {
        try {
            val mediaExtractor: MediaExtractor? = getMediaExtractor(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + source
            )
            val mediaMuxer = MediaMuxer(
                Environment.getExternalStorageDirectory().absoluteFile
                    .toString() + fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
            val audioIndex1: Int = getIndex(mediaExtractor!!, "audio/")
            mediaExtractor.selectTrack(audioIndex1)
            val trackFormat = mediaExtractor.getTrackFormat(audioIndex1)
            val byteBuffer = ByteBuffer.allocate(500 * 1024)
            val trackIndex = mediaMuxer.addTrack(trackFormat)
            val bufferInfo = MediaCodec.BufferInfo()
            mediaMuxer.start()
            bufferInfo.presentationTimeUs = 0
            val stampTime: Long = getStampTime(mediaExtractor, byteBuffer)
            mediaExtractor.unselectTrack(audioIndex1)
            mediaExtractor.selectTrack(audioIndex1)
            while (true) {
                val readSampleSize = mediaExtractor.readSampleData(byteBuffer, 0)
                if (readSampleSize < 0) {
                    break
                }
                mediaExtractor.advance()
                bufferInfo.size = readSampleSize
                bufferInfo.offset = 0
                bufferInfo.flags = mediaExtractor.sampleFlags
                bufferInfo.presentationTimeUs += stampTime
                mediaMuxer.writeSampleData(trackIndex, byteBuffer, bufferInfo)
            }
            //release
            mediaMuxer.stop()
            mediaExtractor.release()
            mediaMuxer.release()
            Log.e("TAG", "finish")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    fun muxerAudioVideo(video:String,audio:String,fileName: String){
       val mediaMuxer = MediaMuxer(
            Environment.getExternalStorageDirectory().absoluteFile
                .toString() + fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
        )
        val extractor1 = MediaExtractor()
        extractor1.setDataSource(video)

        val extractor2 = MediaExtractor()
        extractor2.setDataSource(audio)

        var videoTrackIndex1 = -1
        for (i in 0 until extractor1.trackCount) {
            val format = extractor1.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                extractor1.selectTrack(i)
                videoTrackIndex1 = mediaMuxer.addTrack(format)
                break
            }
        }

        var videoTrackIndex2 = -1
        for (i in 0 until extractor2.trackCount) {
            val format = extractor2.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                extractor2.selectTrack(i)
                videoTrackIndex2 = mediaMuxer.addTrack(format)
                break
            }
        }

        mediaMuxer.start()

        val buffer = ByteBuffer.allocate(1024 * 1024)
        val info = MediaCodec.BufferInfo()

        while (true) {
            val sampleSize = extractor1.readSampleData(buffer, 0)
            if (sampleSize < 0) {
                break
            }
            info.offset = 0
            info.size = sampleSize
            info.presentationTimeUs = extractor1.sampleTime
            info.flags = extractor1.sampleFlags
            mediaMuxer.writeSampleData(videoTrackIndex1, buffer, info)
            extractor1.advance()
        }

        while (true) {
            val sampleSize = extractor2.readSampleData(buffer, 0)
            if (sampleSize < 0) {
                break
            }
            info.offset = 0
            info.size = sampleSize
            info.presentationTimeUs = extractor2.sampleTime
            info.flags = extractor2.sampleFlags
            mediaMuxer.writeSampleData(videoTrackIndex2, buffer, info)
            extractor2.advance()
        }

        mediaMuxer.stop()
        mediaMuxer.release()

    }
    @Throws(IOException::class)
    private fun getMediaExtractor(source: String): MediaExtractor? {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(source)
        return mediaExtractor
    }

    fun getStampTime(
        mediaExtractor: MediaExtractor,
        byteBuffer: ByteBuffer
    ): Long {
        var stampTime: Long = 0
        //获取帧之间的间隔时间
        run({
            mediaExtractor.readSampleData(byteBuffer, 0)
            if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC) {
                mediaExtractor.advance()
            }
            mediaExtractor.readSampleData(byteBuffer, 0)
            val secondTime: Long = mediaExtractor.getSampleTime()
            mediaExtractor.advance()
            mediaExtractor.readSampleData(byteBuffer, 0)
            val thirdTime: Long = mediaExtractor.getSampleTime()
            stampTime = Math.abs(thirdTime - secondTime)
            Log.e("audio111", stampTime.toString() + "")
        })
        return stampTime
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