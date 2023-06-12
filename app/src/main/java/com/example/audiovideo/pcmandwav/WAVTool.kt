package com.example.audiovideo.pcmandwav

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.Locale

class WAVTool(context: Context) {
    private val AUDIO_FREQUENCY = 44100
    private val RECORD_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
    private val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private val PLAY_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_STEREO
    private val RECORD_AUDIO_BUFFER_TIMES = 1
    private val PLAY_AUDIO_BUFFER_TIMES = 1
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var recordThread: AudioRecordThread
    private lateinit var tmpWavFile: File
    var playState = WindState.IDLE // 当前状
    private val TAG: String = "PCMTOOL"
    private val TMP_FOLDER_NAME = "PCM_TMP"
    private var cacheWAVFolder: String
    private var onStateListener: OnState? = null
    interface OnState {
        fun onStateChanged(currentState: WindState)
    }

    enum class WindState {
        ERROR, IDLE, RECORDING, STOP_RECORD, PLAYING, STOP_PLAY
    }
    init {
        cacheWAVFolder = context.externalCacheDir?.absolutePath +
                File.separator +
                TMP_FOLDER_NAME
        val folder = File(cacheWAVFolder)
        if (!folder.exists()) {
            val f = folder.mkdirs()
            Log.d(
                TAG, String.format(
                    Locale.CHINA, "PCM目录:%s -> %b", cacheWAVFolder, f
                )
            )
        } else {
            for (f in folder.listFiles()) {
                val d = f.delete()
                Log.d(TAG, String.format(Locale.CHINA, "删除PCM文件:%s %b", f.name, d))
            }
            Log.d(
                TAG,
                String.format(Locale.CHINA, "PCM目录:%s", cacheWAVFolder)
            )
        }
    }
    @Synchronized
    fun startRecord() {
        if (playState != WindState.IDLE) {
            Log.w(
                TAG,
                "无法开始录制，当前状态为 $playState"
            )
            return
        }
        try {
            tmpWavFile = File.createTempFile(
                "recording",
                ".wav",
                File(cacheWAVFolder)
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        recordThread = AudioRecordThread()
        recordThread.start()
    }
    @Synchronized
    fun stopRecord() {
        if (playState != WindState.RECORDING) {
            return
        }
        playState = WindState.STOP_RECORD
        notifyState(playState)
    }
    @Synchronized
    fun startPlay() {
        if (!isIdle()) {
            return
        }
        AudioTrackPlayThread(tmpWavFile).start()
    }
    @Synchronized
    fun stopPlay() {
        if (playState != WindState.PLAYING) {
            return
        }
        playState = WindState.STOP_PLAY
        notifyState(playState)
    }
    @Synchronized
    fun isIdle(): Boolean {
        return WindState.IDLE == playState
    }
    @Synchronized
    private fun notifyState(currentState: WindState) {
        if (null != onStateListener) {
            mainHandler.post { onStateListener!!.onStateChanged(currentState) }
        }
    }
    @Throws(IOException::class)
    private fun writeWavFileHeader(
        out: FileOutputStream, totalAudioLen: Long, longSampleRate: Long,
        channels: Int
    ) {
        val header =
            generateWavFileHeader(totalAudioLen, longSampleRate, channels)
        out.write(header, 0, header.size)
    }
    /**
     * 任何一种文件在头部添加相应的头文件才能够确定的表示这种文件的格式，
     * wave是RIFF文件结构，每一部分为一个chunk，其中有RIFF WAVE chunk，
     * FMT Chunk，Fact chunk,Data chunk,其中Fact chunk是可以选择的
     *
     * @param pcmAudioByteCount 不包括header的音频数据总长度
     * @param longSampleRate    采样率,也就是录制时使用的频率
     * @param channels          audioRecord的频道数量
     */
    private fun generateWavFileHeader(
        pcmAudioByteCount: Long,
        longSampleRate: Long,
        channels: Int
    ): ByteArray {
        val totalDataLen = pcmAudioByteCount + 36 // 不包含前8个字节的WAV文件总长度
        val byteRate = longSampleRate * 2 * channels
        val header = ByteArray(44)
        header[0] = 'R'.toByte() // RIFF
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte() //数据大小
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()  //WAVE
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        //FMT Chunk
        header[12] = 'f'.toByte()  // 'fmt '
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()  //过渡字节
        //数据大小
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        //编码方式 10H为PCM编码格式
        header[20] = 1 // format = 1
        header[21] = 0
        //通道数
        header[22] = channels.toByte()
        header[23] = 0
        //采样率，每个通道的播放速度
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (2 * channels).toByte()
        header[33] = 0
        //每个样本的数据位数
        header[34] = 16
        header[35] = 0
        //Data chunk
        header[36] = 'd'.toByte()  //data
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (pcmAudioByteCount and 0xff).toByte()
        header[41] = (pcmAudioByteCount shr 8 and 0xff).toByte()
        header[42] = (pcmAudioByteCount shr 16 and 0xff).toByte()
        header[43] = (pcmAudioByteCount shr 24 and 0xff).toByte()
        return header
    }
    inner class AudioRecordThread: Thread() {
        var aRecord: AudioRecord? = null
        var bufferSize = 10240
        init {
            priority = MAX_PRIORITY
            bufferSize = AudioTrack.getMinBufferSize(
                AUDIO_FREQUENCY,
                RECORD_CHANNEL_CONFIG,
                AUDIO_ENCODING
            ) * RECORD_AUDIO_BUFFER_TIMES
            Log.d(
                TAG,
                "record buffer size = $bufferSize"
            )
            aRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                AUDIO_FREQUENCY,
                RECORD_CHANNEL_CONFIG,
                AUDIO_ENCODING,
                bufferSize
            )
        }
        override fun run() {
            super.run()
            playState = WindState.RECORDING
            notifyState(playState)
            try{
                val wavFos = FileOutputStream(tmpWavFile)
                writeWavFileHeader(
                    wavFos,
                    bufferSize.toLong(),
                    AUDIO_FREQUENCY.toLong(),
                    aRecord!!.channelCount
                )
                aRecord!!.startRecording()
                val byteBuffer = ByteArray(bufferSize)
                while(playState == WindState.RECORDING&&!isInterrupted){
                    val end = aRecord!!.read(byteBuffer,0,byteBuffer.size)
                    wavFos.write(byteBuffer,0,end)
                    wavFos.flush()
                }
                aRecord!!.stop()
                wavFos.close()
                // 修改header
                val wavRaf =
                    RandomAccessFile(tmpWavFile, "rw")
                val header: ByteArray = generateWavFileHeader(
                    tmpWavFile.length(),
                    AUDIO_FREQUENCY.toLong(),
                    aRecord!!.channelCount
                )
                Log.d(
                    TAG,
                    "header: " + getHexString(header)
                )
                wavRaf.seek(0)
                wavRaf.write(header)
                wavRaf.close()
                Log.d(
                    TAG,
                    "tmpWavFile.length: " + tmpWavFile.length()
                )
                Log.i(
                    TAG,
                    "audio tmp PCM file len: " + tmpWavFile.length()
                )
            }catch (e:Exception){
                Log.e(
                    TAG,
                    "AudioRecordThread:",
                    e
                )
                notifyState(WindState.ERROR)
            }
            playState = WindState.IDLE
            notifyState(playState)
            Log.d(TAG, "录制结束")
        }
    }
    private fun getHexString(bytes: ByteArray): String? {
        val sb = StringBuilder()
        for (b: Byte in bytes) {
            sb.append(Integer.toHexString(b.toInt())).append(",")
        }
        return sb.toString()
    }
    inner class AudioTrackPlayThread(file:File?):Thread(){
        var track: AudioTrack? = null
        var bufferSize = 10240
        var audioFile: File? = file
        init{
            priority = MAX_PRIORITY
            val bufferSize: Int = AudioTrack.getMinBufferSize(
                AUDIO_FREQUENCY,
                PLAY_CHANNEL_CONFIG,
                AUDIO_ENCODING
            ) * PLAY_AUDIO_BUFFER_TIMES
            track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                AUDIO_FREQUENCY,
                PLAY_CHANNEL_CONFIG,
                AUDIO_ENCODING,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
        }

        override fun run() {
            super.run()
            playState = WindState.PLAYING
            notifyState(playState)
            try{
                val fis = FileInputStream(audioFile)
                track!!.play()
                val aByteBuffer = ByteArray(bufferSize)
                while (playState == WindState.PLAYING&&fis.read(aByteBuffer) >= 0){
                    track!!.write(aByteBuffer,0,aByteBuffer.size)
                }
                track!!.stop()
                track!!.release()
            }catch (e:java.lang.Exception){
                Log.e(
                    TAG,
                    "AudioTrackPlayThread:",
                    e
                )
                notifyState(WindState.ERROR)
            }
            playState = WindState.STOP_PLAY
            notifyState(playState)
            playState = WindState.IDLE
            notifyState(playState)
        }
    }
}