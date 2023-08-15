package com.example.audiovideo.aac

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import androidx.annotation.NonNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class EncodeAACTool{
    private val MIME_TYPE = "audio/mp4a-latm"
    private val KEY_CHANNEL_COUNT = 2
    private val KEY_SAMPLE_RATE = 44100
    private val bitRate = 96000
    private var mWorker: Worker? = null
    private val TAG = "AudioEncoder"
    fun encode(path: String?, name: String?): Boolean {
        if (mWorker == null) {
            mWorker = Worker()
        }
        if (name != null) {
            mWorker!!.setPath(path, name)
        }
        mWorker!!.start()
        return true
    }
    fun stop() {
        if (mWorker != null) {
            mWorker = null
        }
    }
    inner class  Worker : Thread() {
        private val mFrameSize = 2048
        private lateinit var mBuffer: ByteArray
        private var mEncoder: MediaCodec? = null
        private var path: String? = null
        private  var name1: String = ""
        var mBufferInfo: MediaCodec.BufferInfo? = null
        private lateinit var inputBufferArray: Array<ByteBuffer>
        private lateinit var outputBufferArray: Array<ByteBuffer>
        var fileInputStream: FileInputStream? = null
        var fileOutputStream: FileOutputStream? = null
        override fun run() {
            if (!prepare()) {
                Log.d(TAG, "音频编码器初始化失败")
            }
            try {
                fileInputStream = FileInputStream(
                    File(
                        Environment.getExternalStorageDirectory()
                            .absolutePath + "/record.pcm"
                    )
                )
                fileOutputStream = FileOutputStream(File(path + name1))
                while (fileInputStream!!.read(mBuffer, 0, mFrameSize) != -1) encode(mBuffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            release()
        }
        fun setPath(path: String?, name: String) {
            this.path = path
            this.name1 = "/$name.aac"
        }
        /**
         * 释放资源
         */
        private fun release() {
            if (mEncoder != null) {
                mEncoder!!.stop()
                mEncoder!!.release()
            }
        }
        /**
         * @return true配置成功，false配置失败
         */
        private fun prepare(): Boolean {
            try {
                mBufferInfo = MediaCodec.BufferInfo()
                val mediaFormat: MediaFormat? = getMediaFormat()
                if (mediaFormat != null) {
                    initEncoder(mediaFormat)
                }
                mBufferInfo = MediaCodec.BufferInfo()
            } catch (e: IOException) {
                e.printStackTrace()
                return false
            }
            mBuffer = ByteArray(mFrameSize)
            return true
        }
        @Throws(IOException::class)
        private fun initEncoder(mediaFormat: MediaFormat): MediaCodec? {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
            mEncoder!!.configure(
                mediaFormat, null, null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            mEncoder!!.start()
            inputBufferArray = mEncoder!!.getInputBuffers()
            outputBufferArray = mEncoder!!.getOutputBuffers()
            return mEncoder
        }
        @NonNull
        private fun getMediaFormat(): MediaFormat? {
            val mediaFormat = MediaFormat.createAudioFormat(
                MIME_TYPE,
                KEY_SAMPLE_RATE, KEY_CHANNEL_COUNT
            )
            mediaFormat.setString(MediaFormat.KEY_MIME, MIME_TYPE)
            mediaFormat.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2)
            mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
            mediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
            return mediaFormat
        }
        private fun encode(data: ByteArray) {
            val inputIndex = mEncoder!!.dequeueInputBuffer(-1)
            if (inputIndex >= 0) {
                val inputByteBuf = inputBufferArray[inputIndex]
                inputByteBuf.clear()
                inputByteBuf.put(data) //添加数据
                inputByteBuf.limit(data.size) //限制ByteBuffer的访问长度
                mEncoder!!.queueInputBuffer(inputIndex, 0, data.size, 0, 0) //把输入缓存塞回去给MediaCodec
            }
            var outputBufferIndex = mEncoder!!.dequeueOutputBuffer(mBufferInfo!!, 0)
            while (outputBufferIndex >= 0) {
                //获取缓存信息的长度
                val byteBufSize = mBufferInfo!!.size
                //添加ADTS头部后的长度
                val bytePacketSize = byteBufSize + 7
                val outPutBuf = outputBufferArray[outputBufferIndex]
                outPutBuf.position(mBufferInfo!!.offset)
                outPutBuf.limit(mBufferInfo!!.offset + mBufferInfo!!.size)
                val targetByte = ByteArray(bytePacketSize)
                //添加ADTS头部
                addADTStoPacket(targetByte, bytePacketSize)
                outPutBuf[targetByte, 7, byteBufSize]
                outPutBuf.position(mBufferInfo!!.offset)
                try {
                    fileOutputStream!!.write(targetByte)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                mEncoder!!.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = mEncoder!!.dequeueOutputBuffer(mBufferInfo!!, 0)
            }
        }
        /**
         * 给编码出的aac裸流添加adts头字段
         */
        private fun addADTStoPacket(packet: ByteArray, packetLen: Int) {
            val profile = 2 //AAC LC
            val freqIdx = 4 //44.1KHz
            val chanCfg = 2 //CPE
            packet[0] = 0xFF.toByte()
            packet[1] = 0xF9.toByte()
            packet[2] =
                ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
            packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
            packet[4] = (packetLen and 0x7FF shr 3).toByte()
            packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
            packet[6] = 0xFC.toByte()
        }
    }
}