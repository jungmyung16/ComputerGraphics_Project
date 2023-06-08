package com.example.prac03

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.prac03.databinding.ActivityMainBinding

/*
    뷰 바인딩:

    ActivityMainBinding으로 레이아웃 파일과 연결한다. binding을 통해 뷰에 접근한다.
    GLSurfaceView 초기화:

    binding.surfaceView.setEGLContextClientVersion(3)로 OpenGL ES 3.0을 사용한다.
    binding.surfaceView.setRenderer(MainGLRenderer(this))로 OpenGL 렌더러를 설정한다.
    binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY로 렌더링 모드를 지속적으로 업데이트한다.
    버튼 클릭 이벤트 처리:

    버튼 클릭 시 cameraRotate()나 cameraMove() 함수로 카메라를 회전하거나 이동시킨다.
    surfaceView.requestRender()로 화면을 다시 그린다.
    액션바 숨기기:

    supportActionBar?.hide()로 액션바를 숨긴다.
*/

/*
    OpenGL ES를 활용해 3D 그래픽을 그린다. 
    버튼 클릭으로 카메라 위치나 회전을 조작한다. 
    MainGLRenderer는 그래픽을 그린다.
*/

class MainActivity : AppCompatActivity() {

    //private lateinit var mainSurfaceView: MainGLSurfaceView

    val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //mainSurfaceView = MainGLSurfaceView(this)
        //setContentView(mainSurfaceView)
        supportActionBar?.hide()
        initSurfaceView()
        setContentView(binding.root)

        binding.eyeLeft.setOnClickListener {
            cameraRotate(0.174f)
            binding.surfaceView.requestRender()
        }
        binding.eyeRight.setOnClickListener {
            cameraRotate(-0.174f)
            binding.surfaceView.requestRender()
        }
        binding.eyeForward.setOnClickListener {
            cameraMove(0.5f)
            binding.surfaceView.requestRender()
        }
        binding.eyeBackward.setOnClickListener {
            cameraMove(-0.5f)
            binding.surfaceView.requestRender()
        }
    }
    fun initSurfaceView(){
        binding.surfaceView.setEGLContextClientVersion(3)
        binding.surfaceView.setRenderer(MainGLRenderer(this))
        binding.surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}