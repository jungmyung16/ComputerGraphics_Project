package com.example.prac03

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/*
    클래스 구성:
    버텍스 데이터:

    vertexCoords: 바닥의 각 꼭짓점 위치를 정의한 배열.
    vertexUVs: 각 꼭짓점에 해당하는 텍스처 좌표를 정의한 배열.
    vertexNormals: 각 꼭짓점의 법선 벡터를 정의한 배열(조명 효과를 위해 필요).
    셰이더 프로그램:

    vertexShader와 fragmentShader는 바닥을 그리기 위한 셰이더로, 파일 ground_light_tex_vert.glsl와 ground_light_tex_frag.glsl을 통해 불러와서 컴파일 및 링크하고 있어.
    mProgram은 셰이더 프로그램 객체로, 텍스처와 조명이 적용된 바닥을 그리기 위해 사용됨.
    조명 및 재질 설정:

    lightDir: 조명의 방향.
    lightAmbient, lightDiffuse, lightSpecular: 각각 환경광, 확산광, 반사광의 색상 값.
    matAmbient, matSpecular, matShininess: 물체의 재질 속성 (재질의 색상, 반사광, 광택도).
    텍스처:

    textureID: 텍스처를 위한 ID 배열. logo.bmp라는 텍스처 이미지를 바닥에 적용하고 있어.
    텍스처는 GL_TEXTURE_2D로 설정하고, GL_LINEAR_MIPMAP_LINEAR와 GL_LINEAR로 필터링 설정을 적용하고 있어.
    주요 메서드:
    init: OpenGL 셰이더를 로드하고, 필요한 버퍼(좌표, UV, 노멀 등)를 설정한 후 텍스처를 바인딩한다.
    draw: 바닥을 화면에 그리기 위해 호출되는 메서드로, 모델-뷰-프로젝션 행렬(mvpMatrix)과 월드 행렬(worldMat)을 전달받아 바닥을 렌더링한다. 텍스처를 바인딩하고, OpenGL의 glDrawArrays로 삼각형을 그린다.
*/

/*
    기본적으로 3D 바닥을 그리는 데 필요한 모든 작업을 수행하는데, 텍스처와 조명 효과를 함께 적용하여 사실적인 바닥을 렌더링한다. 조명과 재질 특성도 함께 적용되므로, 다양한 환경에서 바닥을 더 사실적으로 표현할 수 있음
*/

class MyLitTexGround(val myContext: Context) {
    private val vertexCoords = floatArrayOf(
        -10.0f, -1.0f,-10.0f,
        -10.0f, -1.0f, 10.0f,
        10.0f, -1.0f, 10.0f,
        -10.0f, -1.0f,-10.0f,
        10.0f, -1.0f, 10.0f,
        10.0f, -1.0f,-10.0f
    )
    private val vertexUVs = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 20.0f,
        20.0f, 20.0f,
        0.0f, 0.0f,
        20.0f, 20.0f,
        20.0f, 0.0f
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
    private var uvBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexUVs.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexUVs)
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
    private val lightDir = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
    private val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

    private val matAmbient = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matShininess = 10.0f

    private var mProgram: Int = -1

    private var mEyePosHandle = -1;
    private var mLightDirHandle: Int = -1
    private var mLightAmbiHandle: Int = -1
    private var mLightDiffHandle: Int = -1
    private var mLightSpecHandle: Int = -1
    private var mMatAmbiHandle: Int = -1
    private var mMatSpecHandle: Int = -1
    private var mMatShHandle: Int = -1

    private var mvpMatrixHandle: Int = -1
    private var mWorldMatHandel = -1

    private var textureID = IntArray(1)
    private val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "ground_light_tex_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "ground_light_tex_frag.glsl", myContext)

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
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(
            1,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            uvBuffer
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

        GLES30.glGenTextures(1,textureID, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR) //nearest = 계단식 -> 연산량이 줄어든다
        //GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_MIRRORED_REPEAT)
        //GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_MIRRORED_REPEAT)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, loadBitmap("logo.bmp", myContext), 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
    }
    fun draw(mvpMatrix: FloatArray, worldMat: FloatArray) {
        GLES30.glUseProgram(mProgram)
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandel, 1, false, worldMat, 0)

        GLES30.glUniform3fv(mLightDirHandle, 1, lightDir, 0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }
}