# Example Android

Jetpack Compose 기반 Android 앱 프로젝트입니다. Clean Architecture와 MVVM 패턴을 적용하여 인증(로그인/회원가입/OAuth) 기능을 구현합니다.

---

## 기술 스택

| 영역         | 기술                                                     |
| ------------ | -------------------------------------------------------- |
| UI           | Jetpack Compose, Material 3                              |
| Architecture | Clean Architecture, MVVM                                 |
| DI           | Hilt (Dagger)                                            |
| Networking   | Retrofit 2, OkHttp 4, kotlinx.serialization              |
| Async        | Kotlin Coroutines, Flow                                  |
| Storage      | DataStore, EncryptedSharedPreferences                    |
| Security     | SSL Pinning (CertificatePinner), AES-256-GCM 쿠키 암호화 |
| Navigation   | Jetpack Navigation Compose                               |
| Test         | JUnit 4, MockK, Truth, Turbine, Compose UI Test          |
| Build        | Kotlin DSL, Version Catalog, Baseline Profiles           |

---

## 빌드 환경

- **minSdk**: 26 (Android 8.0)
- **targetSdk / compileSdk**: 36
- **JVM Target**: 11
- **AGP**: 8.13.2
- **Kotlin**: 2.0.21

---

## 빌드 타입

| 항목    | Debug                   | Release    |
| ------- | ----------------------- | ---------- |
| API URL | development             | production |
| 로깅    | BODY (민감 정보 마스킹) | NONE       |
| Minify  | 비활성화                | R8 활성화  |

```bash
# Debug 빌드
./gradlew assembleDebug

# Release 빌드
./gradlew assembleRelease

# 컴파일 확인
./gradlew compileDebugKotlin
```

---

## 프로젝트 구조

```
app/src/main/java/kr/hs/jung/example/
├── ExampleApplication.kt              # Hilt 초기화, CookieJar 초기화, 생명주기 관리
│
├── domain/                            # Domain Layer (플랫폼 독립)
│   ├── model/                         # 도메인 모델
│   │   ├── AppError.kt               #   sealed class 에러 계층 (Network/Auth/Validation/Business)
│   │   ├── User.kt                   #   사용자 모델
│   │   ├── AuthFormData.kt           #   인증 폼 데이터 + AuthValidator
│   │   └── ValidationResult.kt       #   검증 결과
│   ├── repository/                    # Repository 인터페이스
│   │   └── AuthRepository.kt
│   ├── usecase/auth/                  # UseCase
│   │   ├── BaseAuthUseCase.kt
│   │   ├── LogInUseCase.kt
│   │   ├── SignUpUseCase.kt
│   │   ├── LogoutUseCase.kt
│   │   ├── MeUseCase.kt
│   │   └── ExchangeOAuthCodeUseCase.kt
│   ├── event/                         # 세션 이벤트
│   │   ├── SessionEvent.kt
│   │   └── SessionEventBus.kt
│   ├── state/                         # 상태 인터페이스 (DIP)
│   │   └── AuthStateHolder.kt
│   └── service/
│       └── PasswordHasher.kt
│
├── data/                              # Data Layer
│   ├── remote/                        # 네트워크
│   │   ├── api/AuthApi.kt            #   Retrofit 인터페이스
│   │   ├── dto/                       #   DTO (AuthDto, ServerErrorResponseDto)
│   │   ├── SafeApiCall.kt            #   에러 매핑 + 세션 처리
│   │   ├── RetryInterceptor.kt       #   지수 백오프 + Jitter 재시도
│   │   ├── SanitizingLoggingInterceptor.kt  # 민감 정보 마스킹 로깅
│   │   ├── SSLPinningConfig.kt       #   SSL 인증서 피닝 설정
│   │   ├── PersistentCookieJar.kt    #   암호화 쿠키 저장 (메모리 + 디스크)
│   │   ├── OAuthHelper.kt            #   OAuth (Chrome Custom Tabs)
│   │   └── NetworkConstants.kt       #   네트워크 상수
│   ├── local/                         # 로컬 저장소
│   │   ├── cache/                     #   2-tier 캐시 (Memory LRU + Disk DataStore)
│   │   │   ├── Cache.kt              #     캐시 인터페이스
│   │   │   ├── MemoryCache.kt        #     TTL + LRU 메모리 캐시
│   │   │   ├── DiskCache.kt          #     DataStore 디스크 캐시
│   │   │   └── UserCache.kt          #     사용자 정보 2-tier 캐시
│   │   └── datastore/
│   │       ├── CookieDataStore.kt    #     쿠키 데이터 모델 (CookieProto/CookieStore)
│   │       └── EncryptedCookieStorage.kt  # AES-256-GCM 암호화 쿠키 저장소
│   └── repository/
│       └── AuthRepositoryImpl.kt      #   Repository 구현체 (캐시 전략 포함)
│
├── ui/                                # UI Layer
│   ├── feature/                       # 화면별 Feature
│   │   ├── splash/SplashActivity.kt  #   스플래시 (세션 확인 → 라우팅)
│   │   ├── auth/                      #   인증 화면
│   │   │   ├── AuthActivity.kt       #     인증 Activity (NavHost)
│   │   │   ├── login/                 #     로그인 (Screen + ViewModel)
│   │   │   └── signup/                #     회원가입 (Screen + ViewModel)
│   │   └── main/                      #   메인 화면
│   │       ├── MainActivity.kt
│   │       ├── MainScreen.kt
│   │       └── MainViewModel.kt
│   ├── common/                        # 공통 UI
│   │   ├── BaseViewModel.kt          #   Base<State, Event> 패턴
│   │   ├── ErrorMessageResolver.kt   #   AppError → 다국어 문자열
│   │   ├── UiError.kt / UiEvent.kt
│   │   └── state/AuthState.kt
│   ├── component/                     # 재사용 컴포넌트
│   │   ├── auth/SocialLoginButtons.kt
│   │   ├── button/ExampleButton.kt
│   │   ├── checkbox/ExampleCheckbox.kt
│   │   ├── dialog/                    #   ErrorAlert, LoadingOverlay
│   │   ├── divider/ExampleDividerWithText.kt
│   │   ├── input/ExampleInputBox.kt
│   │   └── layout/                    #   AuthScreenLayout, ScreenHeader
│   ├── navigation/                    # Route, MainTab
│   ├── theme/                         # Color, Type, Dimensions, Theme
│   └── modifier/ModifierExtensions.kt
│
├── di/                                # Hilt DI 모듈
│   ├── NetworkModule.kt              #   OkHttp, Retrofit, JSON
│   ├── CacheModule.kt               #   캐시 의존성
│   ├── RepositoryModule.kt          #   Repository 바인딩
│   ├── DispatcherModule.kt          #   Coroutine Dispatcher
│   └── StateModule.kt               #   상태 바인딩
│
└── util/                              # 유틸리티
    ├── config/AppConfig.kt           #   빌드 환경 설정
    ├── logger/AppLogger.kt           #   통합 로깅
    ├── deeplink/                      #   딥링크 처리
    └── extension/FlowExtensions.kt   #   Flow 확장 함수
```

---

## 다국어 지원

영어 (기본)와 한국어를 지원합니다.

| 리소스 파일                | 내용                                  |
| -------------------------- | ------------------------------------- |
| `strings_auth.xml`         | 로그인/회원가입 UI 문자열             |
| `strings_common.xml`       | 공통 문자열 (버튼, 라벨)              |
| `strings_error.xml`        | 에러 메시지 (Network/Auth/Validation) |
| `strings_server_error.xml` | 서버 에러 코드별 메시지               |

---

## 테스트

```bash
# Unit Test
./gradlew testDebugUnitTest

# Android Instrumented Test
./gradlew connectedDebugAndroidTest
```

| 유형         | 파일 수 | 주요 대상                                     |
| ------------ | ------- | --------------------------------------------- |
| Unit Test    | 12      | ViewModel, Repository, UseCase, BaseViewModel |
| Android Test | 8       | Compose UI 컴포넌트, Robot Pattern            |

---

## 보안

- **쿠키 암호화**: `EncryptedSharedPreferences` (AES-256-GCM) 기반 쿠키 저장
- **SSL Pinning**: `CertificatePinner` 구조 구현 (해시 추가 시 활성화)
- **로그 마스킹**: 네트워크 로그에서 password, token 등 민감 필드 자동 마스킹
- **비밀번호 해싱**: 클라이언트 SHA-512 → 서버 Argon2 이중 해싱 (평문 전송 방지)
- **Release 보안**: R8 난독화, 리소스 축소, 로깅 비활성화

---

## Todo

### 보안

- [ ] SSL Pinning 프로덕션 해시 추가 (`SSLPinningConfig.pinnedKeyHashes`)

### 기능

- [ ] 메인 화면 탭 네비게이션 콘텐츠 구현
- [ ] 토큰 갱신 (Refresh Token) 로직 구현

### 네트워크

- [ ] 네트워크 상태 모니터링 (ConnectivityManager)
- [ ] 중복 요청 방지 (Request coalescing)

### 테스트

- [ ] TestFixtures 확장 (API Mock 데이터, 실패 시나리오)
- [ ] Unit/UI 테스트 추가

### 인프라

- [ ] Crashlytics / Analytics 연동
- [ ] 앱 업데이트 체크 (In-App Update API)
