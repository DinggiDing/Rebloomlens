
```
📦 root/
├── build-logic/                      # 공통 빌드 설정 모듈
│   └── convention/                  
│       ├── android-app.gradle.kts   
│       ├── android-plugin.gradle.kts
│       └── build.gradle.kts          
├── app/                              # 메인 앱 디렉토리
│   ├── core/                         
│   │    ├── MainActivity.kt          
│   │    ├── PluginManager.kt         # 플러그인 관리
│   │    ├── Plugin.kt               
│   │    ├── ConfigLoader.kt         
│   │    └── utils/                  
│   │         └── JsonParser.kt      
│   ├── build.gradle.kts            
│   └── src/                          # 앱 소스 코드
├── common/                           # 공통 인터페이스 모듈
│   ├── plugin_interfaces/           
│   │    └── Plugin.kt                
│   └── build.gradle.kts
│
├── 📁 plugins/                          # 플러그인 모듈 디렉토리
│   ├── likert_scale/                 # Likert Scale 플러그인 폴더
│   │    ├── src/                    
│   │    │    └── LikertScalePlugin.kt 
│   │    ├── config.json             
│   │    └── build.gradle.kts         
│   └── step_counter/                 # Step Counter 플러그인 폴더
│        ├── src/                     
│        │    └── StepCounterPlugin.kt 
│        ├── config.json              
│        └── build.gradle.kts
│   
├── settings.gradle.kts               # 전체 모듈 설정 파일
└── build.gradle.kts                  # 루트 빌드 설정 파일
```
