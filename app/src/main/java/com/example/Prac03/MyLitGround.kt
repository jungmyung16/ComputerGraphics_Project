package com.example.prac03


import android.content.Context
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/*
    바닥의 기하학적 데이터: 바닥을 이루는 6개의 꼭짓점 좌표와 각 점에 해당하는 노멀 벡터를 정의한다.
    셰이더 프로그램: vertexShader와 fragmentShader를 로드하고 컴파일해서 바닥을 그리기 위한 셰이더 프로그램을 만든다.
    조명: 광원의 방향, 색상(ambient, diffuse, specular)을 설정해서 바닥에 빛을 적용한다.
    행렬 연산: mvpMatrix와 worldMat 행렬을 사용해 바닥의 위치와 회전을 처리한다.

    GLES30.glCreateProgram()로 프로그램을 만들고, 두 개의 셰이더를 붙인 후 프로그램을 실행한다.
    glEnableVertexAttribArray()로 바닥의 좌표와 노멀을 바인딩하고, glUniform3fv()로 조명, 재질, 색상을 적용한다.
    glDrawArrays()로 바닥을 그린다.
*/
/*
    OpenGL ES 3.0을 사용해서 바닥을 렌더링하는 코드다. 기본적으로 바닥은 평면으로 설정되어 있고, 셰이더를 이용해 조명, 색상, 노멀 등을 처리
*/

class MyLitGround(val myContext: Context) {
    private val vertexCoords = floatArrayOf(
        -10.0f, -1.0f,-10.0f,
        -10.0f, -1.0f, 10.0f,
        10.0f, -1.0f, 10.0f,
        -10.0f, -1.0f,-10.0f,
        10.0f, -1.0f, 10.0f,
        10.0f, -1.0f,-10.0f
    )
    private val vertexNormals = floatArrayOf(
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.0f, 1.0f, 0.0f
    )
    private var vertexBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexCoords.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexCoords)
                position(0)
            }
        }
    private var normalBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexNormals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexNormals)
                position(0)
            }
        }

    private val color = floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)
    private val lightDir = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
    private val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

    private val matAmbient = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matShininess = 10.0f

    private var mProgram: Int = -1

    private var mEyePosHandle = -1;
    private var mColorHandle = -1
    private var mLightDirHandle: Int = -1
    private var mLightAmbiHandle: Int = -1
    private var mLightDiffHandle: Int = -1
    private var mLightSpecHandle: Int = -1
    private var mMatAmbiHandle: Int = -1
    private var mMatSpecHandle: Int = -1
    private var mMatShHandle: Int = -1

    private var mvpMatrixHandle: Int = -1
    private var mWorldMatHandel = -1

    private val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "ground_light_color_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "ground_light_color_frag.glsl", myContext)

        mProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        GLES30.glUseProgram(mProgram)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(
            0,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(
            2,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "fColor").also {
            GLES30.glUniform4fv(it, 1, color, 0)
        }
        mEyePosHandle = GLES30.glGetUniformLocation(mProgram, "eyePos").also {
            GLES30.glUniform3fv(it, 1, eyePos, 0)
        }
        mLightDirHandle = GLES30.glGetUniformLocation(mProgram, "lightDir").also {
            GLES30.glUniform3fv(it, 1, lightDir, 0)
        }
        mLightAmbiHandle = GLES30.glGetUniformLocation(mProgram, "lightAmbi").also {
            GLES30.glUniform3fv(it, 1, lightAmbient, 0)
        }
        mLightDiffHandle = GLES30.glGetUniformLocation(mProgram, "lightDiff").also {
            GLES30.glUniform3fv(it, 1, lightDiffuse, 0)
        }
        mLightSpecHandle = GLES30.glGetUniformLocation(mProgram, "lightSpec").also {
            GLES30.glUniform3fv(it, 1, lightSpecular, 0)
        }
        mMatAmbiHandle = GLES30.glGetUniformLocation(mProgram, "matAmbi").also {
            GLES30.glUniform3fv(it, 1, matAmbient, 0)
        }
        mMatSpecHandle = GLES30.glGetUniformLocation(mProgram, "matSpec").also {
            GLES30.glUniform3fv(it, 1, matSpecular, 0)
        }
        mMatShHandle = GLES30.glGetUniformLocation(mProgram, "matSh").also {
            GLES30.glUniform1f(it, matShininess)
        }
        mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        mWorldMatHandel = GLES30.glGetUniformLocation(mProgram, "worldMat")
    }
    fun draw(mvpMatrix: FloatArray, worldMat: FloatArray) {
        GLES30.glUseProgram(mProgram)
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandel, 1, false, worldMat, 0)

        GLES30.glUniform3fv(mLightDirHandle, 1, lightDir, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }
}