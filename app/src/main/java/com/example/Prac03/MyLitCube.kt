package com.example.prac03

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.opengles.GL

/*
    큐브의 기하학적 데이터:

    vertexCoords: 큐브의 각 꼭짓점에 대한 3D 좌표.
    vertexNormals: 각 꼭짓점에 대한 노멀 벡터 (조명 효과를 계산하기 위해 필요).
    drawOrder: 큐브의 삼각형을 그리기 위한 인덱스 배열 (각각의 면을 그릴 순서).
    버퍼 초기화:

    vertexBuffer: vertexCoords를 저장하는 FloatBuffer.
    normalBuffer: vertexNormals를 저장하는 FloatBuffer.
    indexBuffer: 큐브의 인덱스 배열을 저장하는 ShortBuffer.
    조명 및 재질 속성:

    color: 큐브의 색상 (빨간색).
    matAmbient, matSpecular, matShininess: 재질 속성 (반사도, 광택도 등).
    조명 관련 속성: lightDir, lightAmbient, lightDiffuse, lightSpecular 등.
    셰이더 프로그램:

    버텍스 셰이더 (cube_light_color_vert.glsl): 큐브의 각 꼭짓점 위치와 노멀 벡터를 처리.
    프래그먼트 셰이더 (cube_light_color_frag.glsl): 조명 모델을 적용하여 큐브의 색상을 계산.
    셰이더 프로그램 초기화:

    loadShader() 함수는 셰이더 코드 파일을 읽고 OpenGL에서 사용할 수 있는 셰이더 객체를 생성합니다.
    glCreateProgram()을 통해 셰이더 프로그램을 생성하고, 해당 프로그램을 활성화합니다.
    셰이더 유니폼 변수 설정:

    glGetUniformLocation()을 통해 셰이더에서 사용할 유니폼 변수의 위치를 가져옵니다.
    glUniform*() 함수들로 각 유니폼 변수에 값을 전달합니다. 예를 들어, lightDir (조명 방향), eyePos (카메라 위치) 등을 설정합니다.
    큐브 그리기:

    draw() 메서드에서 glDrawElements()를 호출하여 큐브를 화면에 그립니다.
    이 때, MVP (Model-View-Projection) 행렬과 월드 행렬을 유니폼 변수로 전달합니다.
    코드의 흐름:
    MyLitCube 객체가 초기화될 때, 셰이더를 로드하고 프로그램을 컴파일하여 GPU에 올립니다.
    draw() 메서드가 호출되면, 큐브의 좌표, 노멀, 색상, 조명 정보 등을 셰이더 프로그램에 전달하고, glDrawElements()를 통해 큐브를 그립니다.
    중요 변수들:
    MVP 행렬 (mvpMatrixHandle): 모델, 뷰, 프로젝션 행렬을 셰이더에 전달하여 큐브의 위치와 크기를 변환합니다.
    조명 속성: mLightDirHandle, mLightAmbiHandle, mLightDiffHandle 등으로 조명 방향, 색상, 그리고 재질의 반사 특성에 대한 정보를 셰이더에 전달합니다.
*/
/*
OpenGL ES 3.0을 사용해서 3D 큐브를 그린다. 큐브는 조명이 적용된 상태로 렌더링되고, 셰이더를 사용해서 색상, 노멀, 조명 같은 요소들을 처리한다. 간단히 말하면, 이 코드는 3D 공간에서 큐브를 그릴 때 빛이 어떻게 영향을 미치는지 보여준다다
*/

class MyLitCube(val myContext: Context) {
    private val vertexCoords = floatArrayOf(
        -0.5f, 0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f, 0.5f, -0.5f,
        -0.5f, 0.5f, 0.5f,
        -0.5f, -0.5f, 0.5f,
        0.5f, -0.5f, 0.5f,
        0.5f, 0.5f, 0.5f
    )
    private val vertexNormals = floatArrayOf(
        -0.57735f,  0.57735f, -0.57735f,
        -0.57735f, -0.57735f, -0.57735f,
        0.57735f, -0.57735f, -0.57735f,
        0.57735f,  0.57735f, -0.57735f,
        -0.57735f,  0.57735f,  0.57735f,
        -0.57735f, -0.57735f,  0.57735f,
        0.57735f, -0.57735f,  0.57735f,
        0.57735f,  0.57735f,  0.57735f
    )
    private val drawOrder = shortArrayOf(
        0, 3, 2, 0, 2, 1,
        2, 3, 7, 2, 7, 6,
        1, 2, 6, 1, 6, 5,
        4, 0, 1, 4, 1, 5,
        3, 0, 4, 3, 4, 7,
        5, 6, 7, 5, 7, 4
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
    private val color = floatArrayOf(1.0f, 0.0f, 0.0f, 1.0f)
    private val matAmbient = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)
    private val matShininess = 10.0f

    private var mProgram = -1

    private var mEyePosHandle = -1;
    private var mColorHandle = -1
    private var mLightDirHandle = -1
    private var mLightAmbiHandle = -1
    private var mLightDiffHandle = -1
    private var mLightSpecHandle = -1
    private var mMatAmbiHandle = -1
    private var mMatSpecHandle = -1
    private var mMatShHandle = -1

    private var mvpMatrixHandle = -1
    private var mWorldMatHandle = -1

    private val vertexStride: Int = COORDS_PER_VERTEX * 4

    init {
        val vertexShader: Int = loadShader(GLES30.GL_VERTEX_SHADER, "cube_light_color_vert.glsl", myContext)
        val fragmentShader: Int = loadShader(GLES30.GL_FRAGMENT_SHADER, "cube_light_color_frag.glsl", myContext)

        mProgram = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }
        GLES30.glUseProgram(mProgram)
        GLES30.glEnableVertexAttribArray(6)
        GLES30.glVertexAttribPointer(
            6,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )
        GLES30.glEnableVertexAttribArray(7)
        GLES30.glVertexAttribPointer(
            7,
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
        mWorldMatHandle = GLES30.glGetUniformLocation(mProgram, "worldMat")
    }
    fun draw(mvpMatrix: FloatArray, worldMat: FloatArray) {
        GLES30.glUseProgram(mProgram)
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(mWorldMatHandle, 1, false, worldMat, 0)

        GLES30.glUniform3fv(mLightDirHandle, 1, lightDir, 0)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, drawOrder.size, GLES30.GL_UNSIGNED_SHORT, indexBuffer)
    }
}