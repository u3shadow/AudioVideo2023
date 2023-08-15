package com.example.audiovideo.aac

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class AACDeCoder{
    private lateinit var inputBuffers: Array<ByteBuffer>
    private lateinit var outputBuffers: Array<ByteBuffer>
    private lateinit var fileOutputStream: FileOutputStream
    //用于分离出音频轨道
    private val mMediaExtractor: MediaExtractor
    private lateinit var mMediaDecode: MediaCodec
    private val targetFile: File

    //类型
    private val mime: String = "audio/mp4a-latm"

    private lateinit var bufferInfo: MediaCodec.BufferInfo
    private val pcmFile: File
    private val totalSize: Int = 0

    init {
        val root = Environment.getExternalStorageDirectory()
            .absolutePath
        targetFile = File(root, "myaac.aac")
        pcmFile = File(root, "解码的pcm.pcm")
        if (!pcmFile.exists()) {
            try {
                pcmFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            fileOutputStream = FileOutputStream(pcmFile.getAbsoluteFile())
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        mMediaExtractor = MediaExtractor()
        try {
            //设置资源
            mMediaExtractor.setDataSource(targetFile.getAbsolutePath())
            //获取含有音频的MediaFormat
            val mediaFormat: MediaFormat? = createMediaFormat()
            mMediaDecode = MediaCodec.createDecoderByType(mime)
            mMediaDecode.configure(mediaFormat, null, null, 0) //当解压的时候最后一个参数为0
            mMediaDecode.start() //开始，进入runnable状态
            //只有MediaCodec进入到Runnable状态后，才能过去缓存组
            inputBuffers = mMediaDecode.inputBuffers
            outputBuffers = mMediaDecode.outputBuffers
            bufferInfo = MediaCodec.BufferInfo()
        } catch (e: IOException) {
            Log.e("tag_ioException", e.message + "")
            e.printStackTrace()
        }
    }

    private fun createMediaFormat(): MediaFormat? {
        //获取文件的轨道数，做循环得到含有音频的mediaFormat
        for (i in 0 until mMediaExtractor.trackCount) {
            val mediaFormat = mMediaExtractor.getTrackFormat(i)
            //MediaFormat键值对应
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.contains("audio/")) {
                mMediaExtractor.selectTrack(i)
                return mediaFormat
            }
        }
        return null
    }
    fun decode(){
        var inputSawEos = false
        var outputSawEos = false
        val kTimes: Long = 5000 //循环时间
        while (!outputSawEos) {
            if (!inputSawEos) {
                //每5000毫秒查询一次
                val inputBufferIndex = mMediaDecode.dequeueInputBuffer(kTimes)
                //输入缓存index可用
                if (inputBufferIndex >= 0) {
                    //获取可用的输入缓存
                    val inputBuffer = inputBuffers[inputBufferIndex]
                    //从MediaExtractor读取数据到输入缓存中，返回读取长度
                    val bufferSize = mMediaExtractor.readSampleData(inputBuffer, 0)
                    if (bufferSize <= 0) { //已经读取完
                        //标志输入完毕
                        inputSawEos = true
                        //做标识
                        mMediaDecode.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            0,
                            kTimes,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                    } else {
                        val time = mMediaExtractor.sampleTime
                        //将输入缓存放入MediaCodec中
                        mMediaDecode.queueInputBuffer(inputBufferIndex, 0, bufferSize, time, 0)
                        //指向下一帧
                        mMediaExtractor.advance()
                    }
                }
            }
            //获取输出缓存，需要传入MediaCodec.BufferInfo 用于存储ByteBuffer信息
            val outputBufferIndex = mMediaDecode.dequeueOutputBuffer(bufferInfo, kTimes)
            if (outputBufferIndex >= 0) {
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                    mMediaDecode.releaseOutputBuffer(outputBufferIndex, false)
                    continue
                }
                //有输出数据
                if (bufferInfo.size > 0) {
                    //获取输出缓存
                    val outputBuffer = outputBuffers[outputBufferIndex]
                    //设置ByteBuffer的position位置
                    outputBuffer.position(bufferInfo.offset)
                    //设置ByteBuffer访问的结点
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                    val targetData = ByteArray(bufferInfo.size)
                    //将数据填充到数组中
                    outputBuffer[targetData]
                    try {
                        fileOutputStream.write(targetData)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                //释放输出缓存
                mMediaDecode.releaseOutputBuffer(outputBufferIndex, false)
                //判断缓存是否完结
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    outputSawEos = true
                }
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mMediaDecode.outputBuffers
            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val mediaFormat = mMediaDecode.outputFormat
            }
        }
        //释放资源
        //释放资源
        try {
            fileOutputStream.flush()
            fileOutputStream.close()
            mMediaDecode.stop()
            mMediaDecode.release()
            mMediaExtractor.release()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}