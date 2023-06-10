package com.example.prac03

import android.opengl.GLSurfaceView
import android.content.Context

/*
    생성자 (init):
    setEGLContextClientVersion(3)로 OpenGL ES 3.0을 사용하도록 설정.
    MainGLRenderer를 생성하여 mainRenderer에 할당.
    setRenderer(mainRenderer)로 MainGLRenderer를 렌더러로 설정.
    renderMode를 GLSurfaceView.RENDERMODE_CONTINUOUSLY로 설정하여 지속적으로 화면을 갱신하도록 한다.
*/
/*
    이 클래스는 OpenGL ES 3.0을 사용해 렌더링을 처리하는 MainGLRenderer를 설정하고, 화면을 지속적으로 갱신하는 GLSurfaceView를 구현한다.
*/
class MainGLSurfaceView(context: Context): GLSurfaceView(context) {
    private val mainRenderer: MainGLRenderer

    init {
        setEGLContextClientVersion(3)
        mainRenderer = MainGLRenderer(context)
        setRenderer(mainRenderer)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        //renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}