package com.example.audiovideo.openglpic

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

    import android.graphics.Bitmap
    import android.opengl.GLES20
    import android.opengl.GLUtils
    import android.opengl.Matrix
    import android.util.Log
    import java.nio.ByteBuffer
    import java.nio.ByteOrder
    import java.nio.FloatBuffer


    class MyGLRenderer(private val mBitmap: Bitmap) : GLSurfaceView.Renderer {
        private var mPositionHandle // 顶点
                = 0
        private var mColorHandle // 颜色
                = 0
        private var mTextureHandle // 纹理
                = 0
        private var mMvpMatrixHandle // 变换矩阵
                = 0
        private var mProgram // Program
                = 0
        private var mGLUniformTexture // 图片纹理
                = 0
        private var mGLTextureId = -1 // 纹理ID
        private var mCubeBuffer: FloatBuffer? = null
        private var mTextureBuffer: FloatBuffer? = null
        private var mColorBuffer: FloatBuffer? = null
        private var mMatrixBuffer: FloatBuffer? = null
        private val mViewMatrix = FloatArray(16)
        private val mProjectMatrix = FloatArray(16)
        private val mMVPMatrix = FloatArray(16)

        // 加载Handle
        private fun makeHandle() {
            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "position")
            mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
            mTextureHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
            mMvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "transform")
            mGLUniformTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
            Log.e(
                TAG,
                "makeHandle: mPositionHandle=" + mPositionHandle + " mColorHandle=" + mColorHandle + " mTextureHandle="
                        + mTextureHandle + " mMvpMatrixHandle=" + mMvpMatrixHandle + " mGLUniformTexture=" + mGLUniformTexture
            )
        }

        // 加载Buffer
        private fun loadBuffer() {
            mCubeBuffer = initBuffer(CUBE)
            mTextureBuffer = initBuffer(TEXTURE_NO_ROTATION)
            mColorBuffer = initBuffer(COLORS)
            mMatrixBuffer = initBuffer(mMVPMatrix)
        }

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

            // 创建program
            val vertexShader =
                loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
            val fragmentShader =
                loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

            //创建一个空的OpenGLES程序
            mProgram = GLES20.glCreateProgram()
            Log.e(
                TAG,
                "onSurfaceCreated: vertexShader=$vertexShader  fragmentShader=$fragmentShader  mProgram=$mProgram"
            )
            //将顶点着色器加入到程序
            GLES20.glAttachShader(mProgram, vertexShader)
            //将片元着色器加入到程序中
            GLES20.glAttachShader(mProgram, fragmentShader)
            //连接到着色器程序
            GLES20.glLinkProgram(mProgram)

            // 获取program的链接情况
            val link = IntArray(1)
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, link, 0)
            if (link[0] <= 0) {
                Log.e("Load Program", "Linking Failed" + GLES20.glGetProgramInfoLog(mProgram))
            }
            GLES20.glUseProgram(mProgram)
            makeHandle()
            val textures = IntArray(1)
            // 加载纹理
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR.toFloat()
            )
            //纹理也有坐标系，称UV坐标，或者ST坐标
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT.toFloat()
            ) // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT.toFloat()
            ) // T轴的拉伸方式为重复
            //        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.width, mBitmap.height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null
            )
            //        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D,textures[0],0);
            if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.e(TAG, "glCheckFramebufferStatus error")
            } else {
                mGLTextureId = textures[0]
                GLES20.glUniform1i(mGLUniformTexture, 1) // 设置第一层纹理
            }
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height) // 设置窗口大小
            Log.e(TAG, "onSurfaceChanged: width=$width height=$height")
            val w = mBitmap.width
            val h = mBitmap.height
            val sWH = w / h.toFloat()
            val sWidthHeight = width / height.toFloat()
            if (width > height) {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -sWidthHeight * sWH,
                        sWidthHeight * sWH,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -sWidthHeight / sWH,
                        sWidthHeight / sWH,
                        -1f,
                        1f,
                        3f,
                        7f
                    )
                }
            } else {
                if (sWH > sWidthHeight) {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -1 / sWidthHeight * sWH,
                        1 / sWidthHeight * sWH,
                        3f,
                        7f
                    )
                } else {
                    Matrix.orthoM(
                        mProjectMatrix,
                        0,
                        -1f,
                        1f,
                        -sWH / sWidthHeight,
                        sWH / sWidthHeight,
                        3f,
                        7f
                    )
                }
            }
            //设置相机位置
            Matrix.setLookAtM(mViewMatrix, 0, 0F, 0F, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
            //计算变换矩阵
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
            loadBuffer()
        }

        override fun onDrawFrame(gl: GL10) {

            // 顶点
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mCubeBuffer)
            GLES20.glEnableVertexAttribArray(mPositionHandle)

            // 顶点着色器的纹理坐标
            GLES20.glVertexAttribPointer(mTextureHandle, 2, GLES20.GL_FLOAT, false, 8, mTextureBuffer)
            GLES20.glEnableVertexAttribArray(mTextureHandle)
            // 传入的图片纹理
            if (mGLTextureId != -1) {
                Log.e(TAG, "onDrawFrame: mGLTextureId=$mGLTextureId")
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 1)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mGLTextureId)
                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mBitmap)
            }

            // 变换矩阵
            GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mMatrixBuffer)
            //获取片元着色器的vColor成员的句柄

            //设置绘制三角形的颜色
            GLES20.glEnableVertexAttribArray(mColorHandle)
            GLES20.glVertexAttribPointer(
                mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                4, mColorBuffer
            )

            // 绘制顶点 ，方式有顶点法和索引法
            // GLES20.GL_TRIANGLE_STRIP即每相邻三个顶点组成一个三角形，为一系列相接三角形构成
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4) // 顶点法，按照传入渲染管线的顶点顺序及采用的绘制方式将顶点组成图元进行绘制
            GLES20.glDisableVertexAttribArray(mPositionHandle)
            GLES20.glDisableVertexAttribArray(mTextureHandle)
            GLES20.glDisableVertexAttribArray(mColorHandle)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        /**
         * 加载shader
         *
         * @param type       片元、顶点
         * @param shaderCode Code
         * @return int
         */
        private fun loadShader(type: Int, shaderCode: String): Int {
            //根据type创建顶点着色器或者片元着色器
            val shader = GLES20.glCreateShader(type)
            //将资源加入到着色器中，并编译
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        companion object {
            private val TAG = ""

            // 原始的矩形区域的顶点坐标，因为后面使用了顶点法绘制顶点，所以不用定义绘制顶点的索引。无论窗口的大小为多少，在OpenGL二维坐标系中都是为下面表示的矩形区域
            private val CUBE = floatArrayOf( // 窗口中心为OpenGL二维坐标系的原点（0,0）
                -1.0f, -1.0f, 0.0f,  // v1
                1.0f, -1.0f, 0.0f,  // v2
                -1.0f, 1.0f, 0.0f,  // v3
                1.0f, 1.0f, 0.0f
            )

            // 纹理也有坐标系，称UV坐标，或者ST坐标。UV坐标定义为左上角（0，0），右下角（1，1），一张图片无论大小为多少，在UV坐标系中都是图片左上角为（0，0），右下角（1，1）
            // 纹理坐标，每个坐标的纹理采样对应上面顶点坐标。
            // 纹理为0 ~ 2，会有四分屏
            private val TEXTURE_NO_ROTATION = floatArrayOf(
                0.0f, 1.0f,  // v1
                1.0f, 1.0f,  // v2
                0.0f, 0.0f,  // v3
                1.0f, 0.0f
            )

            //设置颜色
            private val COLORS = floatArrayOf(
                0.0f, 1.0f, 0.0f, 1.0f,
                1.0f, 0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f, 1.0f
            )

            // 顶点shader
            private const val VERTEX_SHADER = "" +
                    "precision mediump float;\n" +
                    "attribute vec4 position;\n" +  // 顶点着色器的顶点坐标,由外部程序传入
                    "attribute vec2 inputTextureCoordinate;\n" +  // 传入的纹理坐标
                    "attribute vec4 aColor;\n" +
                    "varying vec4 mColor;\n" +  // 传入的纹理坐标
                    "uniform mat4 transform;" +  // 变换矩阵
                    "varying vec2 textureCoordinate;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = transform*position;\n" +
                    "    mColor = aColor;\n" +
                    "    textureCoordinate = inputTextureCoordinate;\n" +  // 最终顶点位置
                    "}"

            // 光栅化后产生了多少个片段，就会插值计算出多少个varying变量，同时渲染管线就会调用多少次片段着色器
            private const val FRAGMENT_SHADER = "precision mediump float;\n" +
                    "varying vec2 textureCoordinate;\n" +  // 最终顶点位置，上面顶点着色器的varying变量会传递到这里
                    "uniform sampler2D vTexture;\n" +  // 外部传入的图片纹理 即代表整张图片的数据
                    "varying vec4 mColor;\n" +  // 传入的纹理坐标
                    "void main()\n" +
                    "{" +
                    "gl_FragColor =mix ( texture2D(vTexture,vec2(1.0-textureCoordinate.x,textureCoordinate.y)) , mColor,0.2);" +  // 增加1.0 - ，为了使图像反转
                    "}"

            // 初始化buffer
            private fun initBuffer(buffers: FloatArray): FloatBuffer {
                // 先初始化buffer,数组的长度*4,因为一个float占4个字节
                val mbb: ByteBuffer = ByteBuffer.allocateDirect(buffers.size * 4)
                // 数组排列用nativeOrder
                mbb.order(ByteOrder.nativeOrder())
                val floatBuffer: FloatBuffer = mbb.asFloatBuffer()
                floatBuffer.put(buffers)
                floatBuffer.flip()
                return floatBuffer
            }
        }

    }