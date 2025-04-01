
```
📦 root/
├── build-logic/                      # 공통 빌드 설정 모듈
│   └── convention/                   # 공통 Gradle 플러그인 모음
│       ├── android-app.gradle.kts    # 앱 공통 설정 스크립트
│       ├── android-plugin.gradle.kts # 플러그인 공통 설정 스크립트
│       └── build.gradle.kts          # build-logic 모듈 설정
├── app/                              # 메인 앱 디렉토리
│   ├── core/                         # 코어 모듈
│   │    ├── MainActivity.kt          # 메인 액티비티
│   │    ├── PluginManager.kt         # 플러그인 관리
│   │    ├── Plugin.kt                # 플러그인 인터페이스
│   │    ├── ConfigLoader.kt          # 설정 로더
│   │    └── utils/                   # 유틸리티
│   │         └── JsonParser.kt       # JSON 파싱 유틸
│   ├── build.gradle.kts              # 앱 모듈 빌드 설정
│   └── src/                          # 앱 소스 코드
├── common/                           # 공통 인터페이스 모듈
│   ├── plugin_interfaces/            # 공통 플러그인 인터페이스
│   │    └── Plugin.kt                # Plugin 인터페이스 정의
│   └── build.gradle.kts              # 공통 모듈 빌드 설정
├── plugins/                          # 플러그인 모듈 디렉토리
│   ├── likert_scale/                 # Likert Scale 플러그인 폴더
│   │    ├── src/                     # 플러그인 소스 코드
│   │    │    └── LikertScalePlugin.kt # 플러그인 구현 파일
│   │    ├── config.json              # 플러그인 설정 JSON
│   │    └── build.gradle.kts         # 플러그인 모듈 빌드 설정
│   └── step_counter/                 # Step Counter 플러그인 폴더
│        ├── src/                     # 플러그인 소스 코드
│        │    └── StepCounterPlugin.kt # 플러그인 구현 파일
│        ├── config.json              # 플러그인 설정 JSON
│        └── build.gradle.kts         # 플러그인 모듈 빌드 설정
├── settings.gradle.kts               # 전체 모듈 설정 파일
└── build.gradle.kts                  # 루트 빌드 설정 파일
```
