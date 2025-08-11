# OpenGL ES 3.0 3D Rendering Engine for Android

## Overview
이 프로젝트는 Android 플랫폼에서 OpenGL ES 3.0을 활용하여 실시간 3D 그래픽스 렌더링을 구현한 것이다. Phong 조명 모델과 텍스처 매핑을 통해 다양한 3D 객체들을 렌더링하며, Arcball 카메라 시스템을 통한 직관적인 뷰 조작을 제공한다.

## Implemented Features & Techniques

### 렌더링 기법
- **Phong Shading Model**
  - Ambient, Diffuse, Specular 조명 컴포넌트 구현
  - Per-vertex normal을 활용한 조명 계산
  - 동적 조명 방향 변경 (시간 기반 회전)
  
- **Texture Mapping**
  - 2D 텍스처 로딩 및 UV 좌표 매핑
  - Mipmap 생성 및 Linear/Linear-Mipmap-Linear 필터링
  - 다중 텍스처 객체 지원 (logo.bmp, crate.bmp)

- **Material Properties**
  - Ambient, Specular 반사 계수
  - Shininess 값을 통한 하이라이트 제어
  - 객체별 독립적인 재질 속성 설정

### 핵심 알고리즘
- **Quaternion-based Rotation (Arcball)**
  - 짐벌락 없는 3D 회전 구현
  - 스크린 좌표를 구 표면으로 투영
  - 쿼터니언 곱셈을 통한 회전 누적
  
- **Matrix Transformations**
  - Model-View-Projection (MVP) 행렬 계산
  - World 좌표계 변환
  - Perspective projection (FOV: 90°)

### 카메라 시스템
- **Arcball Camera**
  - 터치/마우스 드래그를 통한 직관적인 회전
  - 쿼터니언 기반 회전으로 안정적인 카메라 움직임
  
- **FPS-style Camera Controls**
  - 전진/후진 이동 (카메라 방향 벡터 기반)
  - 좌우 회전 (Y축 기준)
  - 이동 범위 제한 (-10 ~ 10 units)

### 3D 객체 렌더링
- **Textured Ground Plane**
  - 20x20 units 크기의 바닥면
  - 반복 텍스처 매핑 (UV 좌표 0-20)
  
- **Hexagonal Pyramid**
  - 7개 정점으로 구성된 육각 피라미드
  - 인덱스 버퍼를 활용한 효율적인 렌더링
  
- **Textured Cube**
  - 8개 정점, 36개 인덱스 구성
  - 각 면에 독립적인 UV 매핑

## Screenshots
**[렌더링 결과 스크린샷을 이곳에 추가하세요]**

## Tech Stack & Dependencies

### 프로그래밍 언어
- **Kotlin** - Android 애플리케이션 개발
- **GLSL ES 3.0** - 셰이더 프로그래밍

### 그래픽스 API
- **OpenGL ES 3.0** - 3D 그래픽스 렌더링

### Android 컴포넌트
- **GLSurfaceView** - OpenGL 렌더링 서페이스
- **View Binding** - UI 컴포넌트 바인딩

### 빌드 시스템
- **Gradle 7.5** - 빌드 자동화
- **Android Gradle Plugin 7.4.2**
- **Kotlin 1.8.0**

### 최소 요구사항
- **Android SDK**
  - compileSdk: 33
  - minSdk: 24
  - targetSdk: 33
- **OpenGL ES 3.0** 지원 디바이스

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/Prac03/
│   │   │   ├── MainActivity.kt           # 메인 액티비티 및 UI 컨트롤
│   │   │   ├── MainGLRenderer.kt         # OpenGL 렌더러 및 씬 관리
│   │   │   ├── MainGLSurfaceView.kt      # GLSurfaceView 구현
│   │   │   ├── MyArcball.kt              # Arcball 카메라 구현
│   │   │   ├── MyLitTexGround.kt         # 텍스처 바닥 렌더링
│   │   │   ├── MyLitGround.kt            # 기본 바닥 렌더링
│   │   │   ├── MyLitHexa.kt              # 육각 피라미드 렌더링
│   │   │   ├── MyLitCube.kt              # 기본 큐브 렌더링
│   │   │   └── MyLitTexCube.kt           # 텍스처 큐브 렌더링
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml     # 메인 레이아웃
│   │   │   │   └── activity_main.xml (landscape)
│   │   │   └── values/
│   │   │       ├── colors.xml
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   ├── assets/                       # 셰이더 및 텍스처 파일
│   │   │   ├── *.glsl                    # GLSL 셰이더 파일들
│   │   │   ├── logo.bmp                  # 바닥 텍스처
│   │   │   └── crate.bmp                 # 큐브 텍스처
│   │   └── AndroidManifest.xml
│   ├── androidTest/
│   │   └── java/com/example/prac03/
│   │       └── ExampleInstrumentedTest.kt
│   └── test/
│       └── java/com/example/prac03/
│           └── ExampleUnitTest.kt
├── build.gradle                           # 앱 레벨 빌드 설정
└── proguard-rules.pro                    # ProGuard 규칙
```

## 스크린샷
|<img src="https://github.com/user-attachments/assets/70958c5f-bc2b-440b-a66f-cde727cdaf65" alt="Image" width="300" />|<img src="https://github.com/user-attachments/assets/ea9f1dae-b7b1-4f77-9021-e69550a5f425" alt="Image" width="300" />|
|:-------------:|:---------:|
|<img src="https://github.com/user-attachments/assets/6c8d5c11-95ed-47c4-b9b7-de63f4f08454" alt="Image" width="300" />|<img src="https://github.com/user-attachments/assets/de665446-383c-4ee5-9776-a63e362382e9" alt="Image" width="300" />|

## Getting Started

### Prerequisites
- Android Studio Arctic Fox 이상
- Android SDK 33
- OpenGL ES 3.0을 지원하는 Android 디바이스 또는 에뮬레이터
- JDK 1.8 이상

### Build Instructions

1. **프로젝트 클론**
   ```bash
   git clone [레포지토리 주소]
   cd Prac03
   ```

2. **Android Studio에서 프로젝트 열기**
   - Android Studio 실행
   - File → Open → 프로젝트 디렉토리 선택

3. **셰이더 및 텍스처 파일 확인**
   - `app/src/main/assets/` 디렉토리에 다음 파일들이 있는지 확인:
     - GLSL 셰이더 파일들 (*.glsl)
     - 텍스처 이미지 (logo.bmp, crate.bmp)

4. **빌드 및 실행**
   - Build → Make Project (Ctrl+F9)
   - Run → Run 'app' (Shift+F10)
   - 타겟 디바이스 선택 후 실행

### APK 빌드
```bash
./gradlew assembleRelease
```
빌드된 APK는 `app/build/outputs/apk/release/` 디렉토리에 생성된다.

## Controls

### 카메라 조작
- **◀ 버튼**: 카메라 좌회전 (10°)
- **▶ 버튼**: 카메라 우회전 (10°)
- **▲ 버튼**: 카메라 전진 (0.5 units)
- **▼ 버튼**: 카메라 후진 (0.5 units)

### 터치 컨트롤
- **드래그**: Arcball 카메라 회전
- **터치 시작**: 회전 시작점 설정
- **터치 이동**: 실시간 회전 적용

### 자동 애니메이션
- 큐브와 육각 피라미드는 Y축을 중심으로 자동 회전
- 조명 방향이 시간에 따라 원형 경로로 이동

## Technical Details

### 셰이더 프로그램
프로젝트는 다음과 같은 셰이더 프로그램을 사용한다:
- `cube_light_tex_vert.glsl` / `cube_light_tex_frag.glsl` - 텍스처 큐브용
- `cube_light_color_vert.glsl` / `cube_light_color_frag.glsl` - 컬러 큐브용
- `hexa_light_color_vert.glsl` / `hexa_light_color_frag.glsl` - 육각 피라미드용
- `ground_light_tex_vert.glsl` / `ground_light_tex_frag.glsl` - 텍스처 바닥용

### Vertex Attributes
- **Location 0-2**: Ground (position, UV, normal)
- **Location 3-4**: Hexagon (position, normal)
- **Location 6-7**: Cube (position, normal)
- **Location 9-11**: Textured Cube (position, normal, UV)

### Uniform Variables
- `uMVPMatrix`: Model-View-Projection 변환 행렬
- `worldMat`: World 변환 행렬
- `eyePos`: 카메라 위치
- `lightDir`: 조명 방향
- `lightAmbi/Diff/Spec`: 조명 색상 컴포넌트
- `matAmbi/Spec/Sh`: 재질 속성




