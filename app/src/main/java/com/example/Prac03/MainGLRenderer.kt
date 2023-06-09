package com.example.prac03

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.system.SystemCleaner
import android.util.Log
import android.view.MotionEvent
import kr.ac.hallym.prac03.MyArcball
import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/*
    변수 설정:

    eyePos, eyeAt, cameraVec로 카메라의 위치, 방향, 벡터를 설정한다.
    lightDir, lightAmbient, lightDiffuse, lightSpecular로 조명의 방향과 특성을 정의한다.
    초기화 (onSurfaceCreated):

    OpenGL 초기 설정(배경 색, 깊이 테스트 등).
    그래픽 오브젝트들(mGround, mHexa, mCube, mArcball) 초기화.
    화면 크기 변경 시 (onSurfaceChanged):

    화면 비율을 계산하고, 원근 투영 행렬(projectionMatrix)과 카메라 뷰 행렬(viewMatrix)을 설정한다.
    프레임 그리기 (onDrawFrame):

    화면을 지운 후 카메라의 위치와 방향을 계산하고, 모델 뷰 행렬을 업데이트하여 객체들을 그린다.
    mGround, mHexa, mCube 오브젝트들을 회전시키며 그린다.
    조명 방향을 시간에 따라 변경한다.
    Matrix.multiplyMM를 통해 여러 변환 행렬을 결합하여 3D 객체를 회전 및 이동시킨다.
    터치 이벤트 처리 (onTouchEvent):

    사용자가 화면을 터치하거나 움직일 때 Arcball을 통해 회전 처리를 한다.
    유틸리티 함수:

    loadShader: 쉐이더 파일을 읽어와 컴파일한다.
    loadBitmap: 이미지를 로드하여 비트맵으로 반환한다.
    cameraRotate: 카메라 벡터를 회전시킨다.
    cameraMove: 카메라를 주어진 거리만큼 이동시킨다.
*/
/*
    이 코드는 OpenGL을 사용하여 3D 씬을 그리고, 카메라를 회전 및 이동시킬 수 있는 기능을 제공한다. 모델은 mGround, mHexa, mCube와 같은 오브젝트로 구성되며, Arcball을 사용해 사용자가 터치로 화면을 회전시킬 수 있다.
*/
const val COORDS_PER_VERTEX = 3

var eyePos = floatArrayOf(0.0f, 3.0f, 3.0f)
var eyeAt = floatArrayOf(0.0f, 0.0f, 0.0f)
var cameraVec = floatArrayOf(0.0f, -0.7071f, -0.7071f)

val lightDir = floatArrayOf(0.0f, 1.0f, 1.0f)
val lightAmbient = floatArrayOf(0.1f, 0.1f, 0.1f)
val lightDiffuse = floatArrayOf(1.0f, 1.0f, 1.0f)
val lightSpecular = floatArrayOf(1.0f, 1.0f, 1.0f)

class MainGLRenderer(val context: Context): GLSurfaceView.Renderer {
    private lateinit var mGround: MyLitTexGround
    private lateinit var mHexa: MyLitHexa
    //private lateinit var mCube: MyLitCube
    private lateinit var mCube: MyLitTexCube
    private lateinit var mArcball: MyArcball

    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)
    private var vpMatrix = FloatArray(16)
    private var mvpMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f
    )

    private var startTime = SystemClock.uptimeMillis()
    private var rotYAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1.0f)

        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setIdentityM(viewMatrix, 0)
        Matrix.setIdentityM(projectionMatrix, 0)
        Matrix.setIdentityM(vpMatrix, 0)

        mGround = MyLitTexGround(context)
        mHexa = MyLitHexa(context)
        //mCube = MyLitCube(context)
        mCube = MyLitTexCube(context)
        mArcball = MyArcball()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        mArcball.resize(width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, ratio, 0.001f, 1000f)

        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2],
                0f, 1f, 0f)

        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        eyeAt[0] = eyePos[0] + cameraVec[0]
        eyeAt[1] = eyePos[1] + cameraVec[1]
        eyeAt[2] = eyePos[2] + cameraVec[2]
        Matrix.setLookAtM(viewMatrix, 0, eyePos[0], eyePos[1], eyePos[2], eyeAt[0], eyeAt[1], eyeAt[2],
            0f, 1f, 0f)
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        //Matrix.setIdentityM(modelMatrix, 0)
        //Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mArcball.rotationMatrix, 0)
        //mGround.draw(mvpMatrix, modelMatrix)
        mGround.draw(mvpMatrix, mArcball.rotationMatrix)

        val endTime = SystemClock.uptimeMillis()
        val angle = 0.1f * (endTime - startTime).toFloat()
        startTime = endTime
        rotYAngle += angle
        var rotYMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotYMatrix, 0, rotYAngle, 0f, 1f, 0f)

        var rotMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
        Matrix.rotateM(rotMatrix, 0, 45f, 0f, 0f, 1f)
        Matrix.multiplyMM(rotMatrix, 0, rotYMatrix, 0, rotMatrix, 0)

        lightDir[0] = sin(rotYAngle*0.01f)
        lightDir[2] = cos(rotYAngle*0.01f)

        for(z in -5..0 step 2) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, 3f, 0f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, 3f, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -3f, 0f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mHexa.draw(mvpMatrix, modelMatrix)

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, -3f, 1.5f, z.toFloat())
            Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, rotMatrix, 0)
            Matrix.multiplyMM(modelMatrix, 0, mArcball.rotationMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mCube.draw(mvpMatrix, modelMatrix)
        }
    }
    fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x.toInt()
        val y = event.y.toInt()

        when(event.action) {
            MotionEvent.ACTION_DOWN -> mArcball.start(x, y)
            MotionEvent.ACTION_MOVE -> mArcball.end(x, y)
        }
        return true
    }
}

fun loadShader(type: Int, filename: String, myContext: Context): Int{
    return GLES30.glCreateShader(type).also{ shader ->

        val inputStream = myContext.assets.open(filename)
        val inputBuffer = ByteArray(inputStream.available())
        inputStream.read(inputBuffer)
        val shaderCode = String(inputBuffer)

        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compiled = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer()
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled)
        if(compiled.get(0)==0){
            GLES30.glGetShaderiv(shader, GLES30.GL_INFO_LOG_LENGTH, compiled)
            if(compiled.get(0)>1){
                Log.e("Shader", "$type shader: " + GLES30.glGetShaderInfoLog(shader))
            }
            GLES30.glDeleteShader(shader)
            Log.e("Shader", "$type shader compile error.")
        }
    }
}
fun loadBitmap(filename: String, myContext: Context): Bitmap {
    val manager = myContext.assets
    val inputStream = BufferedInputStream(manager.open(filename))
    val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
    return bitmap!!
}

fun cameraRotate(theta: Float) {
    val sinTheta = sin(theta)
    val cosTheta = cos(theta)
    val newVecZ = cosTheta * cameraVec[2] - sinTheta * cameraVec[0]
    val newVecX = sinTheta * cameraVec[2] + cosTheta * cameraVec[0]
    cameraVec[0] = newVecX
    cameraVec[2] = newVecZ
}
fun cameraMove(distance: Float) {
    val newPosX = eyePos[0] + distance * cameraVec[0]
    val newPosZ = eyePos[2] + distance * cameraVec[2]
    if(newPosX > -10 && newPosX < 10 && newPosZ > -10 && newPosZ < 10) {
        eyePos[0] = newPosX
        eyePos[2] = newPosZ
    }
}