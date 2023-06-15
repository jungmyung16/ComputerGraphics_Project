package com.example.prac03

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/*
    정점 데이터 (Vertex Data):

    drawOrder: 큐브를 그리기 위한 삼각형의 인덱스를 정의합니다.
    vertexCoords: 큐브의 8개의 정점에 대한 좌표를 저장합니다.
    vertexNormals: 각 정점에 대응하는 노멀 벡터(광원의 반사 방향) 데이터를 저장합니다.
    vertexUVs: 각 면에 대한 텍스처 좌표를 정의하여 텍스처 매핑을 수행합니다.
    버퍼 초기화:

    vertexBuffer, normalBuffer, uvBuffer: 각각 정점, 노멀, UV 좌표를 저장하는 FloatBuffer입니다. 이를 통해 GPU에 데이터를 전달합니다.
    재질 및 조명 설정:

    matAmbient, matSpecular, matShininess: 재질의 특성(환경광, 반사광, 광택도)을 정의합니다.
    lightDir, lightAmbient, lightDiffuse, lightSpecular: 조명의 방향, 환경광, 확산광, 반사광 색상을 설정합니다.
    셰이더 로딩 및 초기화:

    loadShader(): 외부 GLSL 파일로부터 셰이더를 로드합니다. vertex와 fragment 셰이더를 각각 로드하여 프로그램을 생성하고 링크합니다.
    glGetUniformLocation(): 셰이더에서 사용할 변수들(mvpMatrix, lightDir, matAmbi, 등)의 위치를 가져옵니다.
    텍스처 로딩: 텍스처 ID를 생성하고, GLUtils.texImage2D()를 사용하여 이미지를 로드한 후, 텍스처의 필터링 방식(선형)을 설정합니다.
    행렬 계산:

    mvpMatrix와 worldMat을 셰이더에 전달하여 물체의 변환을 수행합니다.
    그리기:

    draw() 메서드에서는 텍스처와 셰이더 프로그램을 활성화하고, glDrawArrays()를 통해 큐브의 모든 면을 그립니다. 이때 각 면에 텍스처와 조명 효과가 적용됩니다.
    핵심 흐름:
    초기화: 텍스처와 셰이더 프로그램을 초기화하고, 큐브의 정점, 노멀, UV 데이터를 GPU에 전달합니다.
    셰이더 설정: 각종 변수(조명, 재질 등)를 셰이더 프로그램에 전달하여 렌더링 시 사용할 수 있도록 설정합니다.
    그리기: draw() 메서드를 호출하여 텍스처가 적용된 큐브를 화면에 렌더링합니다.
    세부 사항:
    GLUtils.texImage2D()를 사용하여 crate.bmp 파일을 텍스처로 로드합니다. (이 파일은 리소스 폴더에 있어야 합니다.)
    mvpMatrix와 worldMat를 셰이더에 전달하여 큐브가 화면에서 적절히 위치하도록 합니다.
    glDrawArrays()를 사용하여 큐브의 면을 그립니다. GL_TRIANGLES로 삼각형 형태로 그려집니다.
*/
/*
    3D 텍스처가 적용된 큐브를 그리기 위한 OpenGL ES 3.0 기반의 코드입니다. 셰이더, 텍스처, 조명, 재질 등의 요소를 결합하여 큐브를 화면에 렌더링
*/

class MyLitTexCube(val myContext: Context) {
    private val drawOrder = intArrayOf(
        0, 3, 2, 0, 2, 1,
        2, 3, 7, 2, 7, 6,
        1, 2, 6, 1, 6, 5,
        4, 0, 1, 4, 1, 5,
        3, 0, 4, 3, 4, 7,
        5, 6, 7, 5, 7, 4
    )
    private val vertexCoords = FloatArray(108).apply {
        val vertex = arrayOf(
            floatArrayOf( -0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, -0.5f, -0.5f),
            floatArrayOf(0.5f, 0.5f, -0.5f),
            floatArrayOf(-0.5f, 0.5f, 0.5f),
            floatArrayOf(-0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, -0.5f, 0.5f),
            floatArrayOf(0.5f, 0.5f, 0.5f)
        )
        var index = 0
        for(i in 0 .. 35){
            this[index++] = vertex[drawOrder[i]][0]
            this[index++] = vertex[drawOrder[i]][1]
            this[index++] = vertex[drawOrder[i]][2]
        }
    }
    private val vertexNormals = FloatArray(108).apply {
        val normals = arrayOf(
            floatArrayOf(-0.57735f,  0.57735f, -0.57735f),
            floatArrayOf(-0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f, -0.57735f, -0.57735f),
            floatArrayOf(0.57735f,  0.57735f, -0.57735f),
            floatArrayOf(-0.57735f,  0.57735f,  0.57735f),
            floatArrayOf(-0.57735f, -0.57735f,  0.57735f),
            floatArrayOf(0.57735f, -0.57735f,  0.57735f),
            floatArrayOf(0.57735f,  0.57735f,  0.57735f)
        )
        var index = 0
        for(i in 0 .. 35){
            this[index++] = normals[drawOrder[i]][0]
            this[index++] = normals[drawOrder[i]][1]
            this[index++] = normals[drawOrder[i]][2]
        }
    }
    private val vertexUVs = FloatArray(72).apply {
        val UVs = arrayOf(
            floatArrayOf(0.0f, 0.0f),
            floatArrayOf(0.0f, 1.0f),
            floatArrayOf(1.0f, 1.0f),
            floatArrayOf(0.0f, 0.0f),
            floatArrayOf(1.0f, 1.0f),
            floatArrayOf(1.0f, 0.0f)
        )
        var index = 0
        for(i in 0 .. 5){
            for(j in 0 .. 5){
                this[index++] = UVs[j][0]
                this[index++] = UVs[j][1]
            }
        }
    }
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
    private var uvBuffer: FloatBuffer =
        ByteBuffer.allocateDirect(vertexUVs.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertexUVs)
                position(0)
            }
        }

    private val matAmbient = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matShininess = 10.0f

    private var mProgram: Int = -1
    private var mEyePosHandle = -1
    private var mLightDirHandle: Int = -1
    private var mLightAmbiHandle: Int = -1
    private var mLightDiffHandle: Int = -1
    private var mLightSpecHandle: Int = -1
    private var mMatAmbiHandle: Int = -1
    private var mMatSpecHandle: Int = -1
    private var mMatShHandle: Int = -1

    private var mvpMatrixHandle: Int = -1
    private var mWorldMatHandle = -1
    private var textureID = IntArray(1)

    private val vertexCount: Int = vertexCoords.size / COORDS_PER_VERTEX
    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "cube_light_tex_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "cube_light_tex_frag.glsl", myContext)

        mProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        GLES30.glUseProgram(mProgram)
        GLES30.glEnableVertexAttribArray(9)
        GLES30.glVertexAttribPointer(
            9,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(10)
        GLES30.glVertexAttribPointer(
            10,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            normalBuffer
        )
        GLES30.glEnableVertexAttribArray(11)
        GLES30.glVertexAttribPointer(
            11,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            uvBuffer
        )

        mvpMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
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
        mWorldMatHandle = GLES30.glGetUniformLocation(mProgram, "worldMat")
        GLES30.glGenTextures(1, textureID, 0)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, loadBitmap("crate.bmp", myContext), 0)
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
    }
    fun draw(mvpMatrix: FloatArray, worldMat: FloatArray) {
        GLES30.glUseProgram(mProgram)

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandle, 1, false, worldMat, 0)

        GLES30.glUniform3fv(mLightDirHandle, 1, lightDir, 0)

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureID[0])
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
    }
}