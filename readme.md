# FE 폴더 구조 

````
runnity/                                  # Android 런닝 앱 전체 프로젝트 루트
├── data/                                 # 데이터 관리 계층 (API, Model, Repository 등)
│   ├── api/                              # 서버 통신 관련 (Retrofit 설정 및 인터페이스)
│   │   ├── RetrofitInstance.kt           # → Retrofit 객체 생성 및 기본 설정 (baseUrl, converter 등)
│   │   └── RunApiService.kt              # → 서버 API 명세 인터페이스 (@GET, @POST 등)
│   │
│   ├── model/                            # 서버와 주고받는 데이터 모델 (DTO)
│   │   └── User.kt, RunRecord.kt ...     # → 요청(Request), 응답(Response) 데이터 클래스들
│   │
│   ├── repository/                       # ViewModel과 API 중간 계층 (데이터 관리)
│   │   └── RunRepository.kt              # API 호출 및 데이터 처리 로직 관리
│   │
│   └── util/                             # 데이터 관련 유틸 (토큰, 네트워크 등)
│       └── TokenManager.kt               # → SharedPreferences로 JWT 토큰 저장/관리
│
├── service/                              # 백그라운드 서비스 계층 (실시간 추적, 알림 등)
│   ├── RunningService.kt                 # → Foreground Service로 러닝 중 거리·시간 추적
│   └── NotificationHelper.kt             # → 운동 중 알림(Notification) 생성 및 관리
│
├── socket/                               #  실시간 통신 관리 계층 (WebSocket, Socket.IO 등)
│   └── SocketManager.kt                  # → 서버 연결, 이벤트 수신/송신 로직 관리
│
├── theme/                                # 앱 전체 테마 및 스타일 정의
│   ├── Color.kt                          # → 앱 전반 색상 팔레트 정의
│   ├── Theme.kt                          # → Material3 테마 구성 (Light/Dark 모드 등)
│   └── Type.kt                           # → 폰트, 타이포그래피 스타일 정의
│
├── ui/                                   # UI 계층 (Compose 기반 화면 및 컴포넌트)
│   ├── components/                       # 재사용 가능한 UI 컴포넌트 모음
│   │   └── PrimaryButton.kt              # → 공용 버튼 컴포넌트
│   │
│   ├── navigation/                       # 화면 간 이동(Navigation) 관련
│   │   └── AppNavigation.kt              # → NavHost 정의 및 화면 전환 관리
│   │
│   └── screens/                          # 실제 앱 화면 단위 (View + ViewModel)
│       ├── login/                        # 로그인/회원가입 관련 화면
│       │   ├── LoginScreen.kt            # → 로그인 UI
│       │   └── LoginViewModel.kt         # → 로그인 로직 및 상태 관리
│       │
│       ├── running/                      # 개인 러닝/단체 러닝 관련 화면
│       │   └── RunningScreen.kt          # → 러닝 진행 UI (지도, 시간, 거리 등)
│       │
│       └── splash/                       # 앱 시작 시 로딩/인트로 화면
│           └── SplashScreen.kt           # → Splash UI 및 초기 세팅 처리
│
├── utils/                                # 앱 전역에서 공용으로 쓰이는 유틸 함수
│   ├── DateFormatter.kt                  # → 날짜·시간 포맷 변환
│   ├── PaceUtils.kt                      # → 페이스(분/km) 계산 로직
│   └── PermissionUtils.kt                # → 위치 권한 요청 및 상태 확인
│
├── GlobalApplication.kt                  # 앱 전역 초기화 설정 (Context, TokenManager 등)
└── MainActivity.kt                       # 앱 진입점 (AppNavigation 실행 및 화면 표시)
````
