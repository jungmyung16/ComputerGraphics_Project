package kr.ac.hallym.prac03

import java.lang.Math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
    변수들:

    width와 height는 화면 크기.
    lastPos는 마지막 터치나 마우스 위치.
    scalarQ와 vectorQ는 회전을 나타내는 쿼터니언 값.
    rotationMatrix는 최종 회전 행렬.
    resize(w: Int, h: Int):

    화면 크기(width, height)를 설정.
    project(xi: Int, yi: Int):

    2D 화면 좌표 (xi, yi)를 3D 공간상의 단위 벡터로 변환한다.
    start(xi: Int, yi: Int):

    터치나 마우스 드래그를 시작할 때 호출되며, 초기 위치를 설정.
    end(xi: Int, yi: Int):

    드래그가 끝날 때 호출된다. 현재 위치를 이전 위치와 비교하여 회전 각도와 회전 축을 계산하고, 이를 쿼터니언으로 변환하여 회전 행렬을 업데이트한다.
    normalize(v: DoubleArray):

    벡터를 정규화하여 크기를 1로 만든다.
    dotProduct(v1: DoubleArray, v2: DoubleArray):

    두 벡터의 내적을 계산한다.
    crossProduct(v1: DoubleArray, v2: DoubleArray):

    두 벡터의 외적을 계산하여 회전 축을 찾는다.
*/
/*
    Arcball 기법을 사용하여 마우스나 터치 입력에 따라 3D 객체를 회전시키는 기능을 제공한다. 주로 쿼터니언을 이용한 회전과 이를 변환하여 회전 행렬을 적용하는 방식이다.
*/
class MyArcball {

    private var width = 0
    private var height = 0
    private var lastPos = doubleArrayOf(0.0, 0.0, 0.0)

    // A quaternion
    private var scalarQ = 1.0
    private var vectorQ = doubleArrayOf(0.0, 0.0, 0.0)

    var rotationMatrix = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)

    fun resize(w: Int, h: Int) {
        width = w
        height = h
    }

    private fun project(xi: Int, yi: Int): DoubleArray {
        val pos = DoubleArray(3)
        pos[0] = (2 * xi - width).toDouble() / width.toDouble()
        pos[1] = (height - 2 * yi).toDouble() / height.toDouble()
        val length = sqrt(pos[0] * pos[0] + pos[1] * pos[1])
        pos[2] = cos(PI * 0.5 * (when { length<1.0 -> length else -> 1.0}))
        return normalize(pos)
    }

    fun start(xi: Int, yi: Int) {
        lastPos = project(xi, yi)
    }

    fun end(xi: Int, yi: Int) {
        val currPos: DoubleArray = project(xi, yi)

        val diff = DoubleArray(3)
        diff[0] = currPos[0] - lastPos[0]
        diff[1] = currPos[1] - lastPos[1]
        diff[2] = currPos[2] - lastPos[2]

        if (diff[0] == 0.0 && diff[1] == 0.0 && diff[2] == 0.0)
            return

        val angle = PI * 0.5 * sqrt(diff[0]*diff[0] + diff[1]*diff[1] + diff[2]*diff[2])
        var axis: DoubleArray = crossProduct(currPos, lastPos)
        axis = normalize(axis)

        // Create a quaternion
        val s2 = cos(angle * 0.5)
        val s = sin(angle * 0.5)
        val v2= doubleArrayOf(s*axis[0], s*axis[1], s*axis[2])

        // Update a quaternion - multiplying quaternions
        val s1 = scalarQ
        val v1 = vectorQ
        val s3 = dotProduct(v1, v2)
        val v3 = crossProduct(v1, v2)
        scalarQ = s1 * s2 - s3
        vectorQ[0] = s1*v2[0] + s2*v1[0] + v3[0]
        vectorQ[1] = s1*v2[1] + s2*v1[1] + v3[1]
        vectorQ[2] = s1*v2[2] + s2*v1[2] + v3[2]

        val det = 1.0 / sqrt(scalarQ*scalarQ + vectorQ[0]*vectorQ[0] + vectorQ[1]*vectorQ[1] + vectorQ[2]*vectorQ[2])
        scalarQ *= det
        vectorQ[0] *= det
        vectorQ[1] *= det
        vectorQ[2] *= det

        // rotation matrix with quaternions
        // P' = quat * P * quat^-1
        // M = {{ 1-2b^2-2c^2,  2ab-2sc,        2ac+2sb     },
        //      { 2ab+2sc,      1-2a^2-2c^2,    2bc-2sa     },
        //      { 2ac-2sb,      2bc+2sa,        1-2a^2-2b^2 }};
        rotationMatrix[0] = 1.0f - 2.0f*(vectorQ[1]*vectorQ[1] + vectorQ[2]*vectorQ[2]).toFloat()
        rotationMatrix[1] = 2.0f*(vectorQ[0]*vectorQ[1] - scalarQ*vectorQ[2]).toFloat()
        rotationMatrix[2] = 2.0f*(vectorQ[0]*vectorQ[2] + scalarQ*vectorQ[1]).toFloat()
        //rotationMatrix[3] = 0.0f

        rotationMatrix[4] = 2.0f*(vectorQ[0]*vectorQ[1] + scalarQ*vectorQ[2]).toFloat()
        rotationMatrix[5] = 1.0f - 2.0f*(vectorQ[0]*vectorQ[0] + vectorQ[2]*vectorQ[2]).toFloat()
        rotationMatrix[6] = 2.0f*(vectorQ[1]*vectorQ[2] - scalarQ*vectorQ[0]).toFloat()
        //rotationMatrix[7] = 0.0f

        rotationMatrix[8] = 2.0f*(vectorQ[0]*vectorQ[2] - scalarQ*vectorQ[1]).toFloat()
        rotationMatrix[9] = 2.0f*(vectorQ[1]*vectorQ[2] + scalarQ*vectorQ[0]).toFloat()
        rotationMatrix[10] = 1.0f - 2.0f*(vectorQ[0]*vectorQ[0] + vectorQ[1]*vectorQ[1]).toFloat()
        //rotationMatrix[11] = 0.0f

        //rotationMatrix[12] = 0.0f
        //rotationMatrix[13] = 0.0f
        //rotationMatrix[14] = 0.0f
        //rotationMatrix[15] = 1.0f

        lastPos = currPos
    }

    private fun normalize(v: DoubleArray): DoubleArray {
        val length = 1.0 / sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2])
        val vn = DoubleArray(3)
        vn[0] = v[0] * length
        vn[1] = v[1] * length
        vn[2] = v[2] * length
        return vn
    }

    private fun dotProduct(v1: DoubleArray, v2: DoubleArray): Double {
        return v1[0]*v2[0] + v1[1]*v2[1] + v1[2]*v2[2]
    }

    private fun crossProduct(v1: DoubleArray, v2: DoubleArray): DoubleArray {
        val v = DoubleArray(3)
        v[0] = v1[1]*v2[2] - v1[2]*v2[1]
        v[1] = v1[2]*v2[0] - v1[0]*v2[2]
        v[2] = v1[0]*v2[1] - v1[1]*v2[0]
        return v
    }
}