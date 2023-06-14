package com.example.prac03

import android.content.Context
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/*
    정점 데이터:

    vertexCoords: 7개의 꼭짓점 좌표를 정의하여 육각형의 형태를 만든다.
    vertexNormals: 각 정점에 해당하는 노멀 벡터를 정의한다.
    drawOrder: 육각형의 각 삼각형을 그리기 위한 인덱스를 정의한다.
    버퍼 초기화:

    vertexBuffer: 정점 좌표 데이터를 저장하는 버퍼.
    normalBuffer: 정점 노멀 벡터를 저장하는 버퍼.
    indexBuffer: 삼각형의 그리기 순서를 정의한 인덱스를 저장하는 버퍼.
    셰이더 프로그램:

    vertexShader와 fragmentShader를 통해 셰이더를 로드하고 프로그램을 연결한다.
    셰이더에서 사용할 변수로 색상, 빛의 방향, 재질 정보, 카메라 위치 등을 설정한다.
    조명과 재질 설정:

    mColorHandle: 색상(블루 색상)을 설정한다.
    mLightDirHandle: 광원의 방향을 설정한다.
    mMatAmbiHandle, mMatSpecHandle, mMatShHandle: 재질의 반사 특성(ambient, specular, shininess)을 설정한다.
    행렬 연산:

    mvpMatrix와 worldMat을 셰이더로 전달해 물체의 변환(회전, 이동 등)을 처리한다.
    그리기:

    GLES30.glDrawElements()를 사용하여 drawOrder에 정의된 인덱스를 바탕으로 헥사곤을 그린다. 이 메서드는 각 삼각형을 그릴 때 인덱스를 이용해 최적화된 방식으로 그린다.
    핵심 동작:
    초기화: 셰이더를 로드하고 프로그램을 생성한다. OpenGL의 glEnableVertexAttribArray()와 glVertexAttribPointer()를 사용해 정점과 노멀을 연결한다.
    셰이더 변수 설정: glUniform3fv()와 glUniform4fv()를 통해 조명, 색상, 재질 정보를 셰이더로 전달한다.
    헥사곤 그리기: glDrawElements()로 헥사곤의 면을 그린다.
*/
/*
    OpenGL ES 3.0을 사용하여 육각형을 그리기 위해 필요한 모든 설정을 처리하는 코드다. 헥사곤의 정점과 노멀을 정의하고, 셰이더를 통해 색상과 조명, 재질 속성을 적용하여 3D 공간에서 렌더링한다.
*/

class MyLitHexa(myContext: Context) {
    private val vertexCoords = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        1.0f, -1.0f, 0.0f,
        0.5f, -1.0f, -0.866f,
        -0.5f, -1.0f, -0.866f,
        -1.0f, -1.0f, 0.0f,
        -0.5f, -1.0f, 0.866f,
        0.5f, -1.0f, 0.866f
    )
    private val vertexNormals = floatArrayOf(
        0.0f, 1.0f, 0.0f,
        1.0f, 0.0f, 0.0f,
        0.5f, 0.0f, -0.866f,
        -0.5f, 0.0f, -0.866f,
        -1.0f, 0.0f, 0.0f,
        -0.5f, 0.0f, 0.866f,
        0.5f, 0.0f, 0.866f
    )
    private val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3,
        0, 3, 4, 0, 4, 5,
        0, 5, 6, 0, 6, 1
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
    private val indexBuffer: ShortBuffer =
        ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }
    private val color = floatArrayOf(0.0f, 0.0f, 1.0f, 1.0f)
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

    private val vertexStride: Int = COORDS_PER_VERTEX * 4
    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "hexa_light_color_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "hexa_light_color_frag.glsl", myContext)

        mProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        GLES30.glUseProgram(mProgram)
        GLES30.glEnableVertexAttribArray(3)
        GLES30.glVertexAttribPointer(
            3,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(4)
        GLES30.glVertexAttribPointer(
            4,
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

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, drawOrder.size, GLES30.GL_UNSIGNED_SHORT, indexBuffer)
    }
}
