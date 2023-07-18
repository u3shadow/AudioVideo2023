package com.example.audiovideo.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Triangle {

    // 顶点坐标
    private val vertexCoords = floatArrayOf(
        0.0f,  0.5f, 0.0f,   // top
        -0.5f, -0.5f, 0.0f,   // bottom left
        0.5f, -0.5f, 0.0f    // bottom right
    )

    private var vertexBuffer: FloatBuffer

    // 顶点着色器
    private val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "}"

    // 片段着色器
    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"

    private var mProgram: Int = 0

    init {
        // 将顶点坐标初始化到ByteBuffer
        val bb = ByteBuffer.allocateDirect(vertexCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())

        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertexCoords)
        vertexBuffer.position(0)

        // 加载和编译顶点着色器和片段着色器
        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // 创建OpenGL程序并连接顶点着色器和片段着色器
        mProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(mProgram)

        // 获取顶点着色器中的位置句柄
        val positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")

        // 启用顶点属性数组
        GLES20.glEnableVertexAttribArray(positionHandle)

        // 准备三角形坐标数据
        GLES20.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        // 获取片段着色器中的颜色句柄
        val colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        // 设置三角形的颜色（红色）
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        // 获取顶点着色器中的MVP矩阵句柄
        val mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")

        // 应用MVP矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

        // 禁用顶点属性数组
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        // 每个顶点的坐标数
        const val COORDS_PER_VERTEX = 3

        // 顶点之间的偏移量
        const val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

        // 三角形的颜色（红色）
        val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)

        // 顶点个数
        const val vertexCount = 3
    }
}

