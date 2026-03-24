# 코드베이스 리팩토링 분석 보고서

> 분석일: 2026-02-25 (v3.0 전체 재분석)
> 분석 대상: example-android 프로젝트
> 전체 평가: **9.0/10** — 완료 17건 / 잔여 23건 (P0: 4, P1: 5, P2: 8, P3: 6+1)

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [아키텍처 현황](#2-아키텍처-현황)
3. [최신 트렌드 준수 현황](#3-최신-트렌드-준수-현황)
4. [완료된 개선 사항](#4-완료된-개선-사항)
5. [리팩토링 필요 항목](#5-리팩토링-필요-항목)
6. [미적용 최신 트렌드](#6-미적용-최신-트렌드)
7. [개선 로드맵](#7-개선-로드맵)

---

## 1. 프로젝트 개요

### 기술 스택

| 카테고리      | 기술                  | 버전            |
| ------------- | --------------------- | --------------- |
| Language      | Kotlin                | 2.0.21          |
| Build         | Android Gradle Plugin | 8.13.2          |
| UI            | Jetpack Compose       | BOM 2024.12.01  |
| Navigation    | Navigation Compose    | 2.8.5           |
| DI            | Hilt                  | 2.51.1          |
| Network       | Retrofit / OkHttp     | 2.11.0 / 4.12.0 |
| Storage       | DataStore             | 1.1.1           |
| Serialization | Kotlin Serialization  | 1.7.3           |
| Build Plugin  | KSP                   | 2.0.21-1.0.27   |

### 프로젝트 규모

- **Kotlin 파일**: 90개
- **테스트 파일**: 18개 (단위 10개 + 통합 8개)
- **아키텍처**: Clean Architecture (Domain/Data/UI 3계층)

---

## 2. 아키텍처 현황

```
┌─────────────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                           │
├─────────────────────────────────────────────────────────────────┤
│  Activities           Screens              Components           │
│  - AuthActivity       - LogInScreen        - ExampleButton      │
│  - MainActivity       - SignUpScreen       - ExampleInputBox    │
│  - SplashActivity     - MainScreen         - SocialLoginButtons │
└───────────────────────────┬─────────────────────────────────────┘
                            │ ViewModel
┌───────────────────────────┴─────────────────────────────────────┐
│              ViewModel Layer (State/Event)                      │
├─────────────────────────────────────────────────────────────────┤
│  BaseViewModel<S, E>                                            │
│  - StateFlow (UI State)                                         │
│  - SharedFlow (One-time Events)                                 │
└───────────────────────────┬─────────────────────────────────────┘
                            │ UseCase
┌───────────────────────────┴─────────────────────────────────────┐
│               Domain Layer (Business Logic)                     │
├─────────────────────────────────────────────────────────────────┤
│  UseCases              Models              Services             │
│  - LogInUseCase        - User              - PasswordHasher     │
│  - SignUpUseCase       - AppError          - AuthManager        │
│  - LogoutUseCase       - ValidationResult  - SessionManager     │
└───────────────────────────┬─────────────────────────────────────┘
                            │ Repository
┌───────────────────────────┴─────────────────────────────────────┐
│                 Data Layer (Data Access)                        │
├─────────────────────────────────────────────────────────────────┤
│  Repository        Remote                 Local                 │
│  - AuthRepo        - AuthApi              - UserCache           │
│                    - SafeApiCall          - CookieDataStore     │
│                    - RetryInterceptor     - DiskCache           │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 최신 트렌드 준수 현황

### 잘 적용된 부분

| 항목                          | 위치                  | 설명                                      |
| ----------------------------- | --------------------- | ----------------------------------------- |
| **Kotlin 2.0 (K2)**           | `libs.versions.toml`  | 최신 K2 컴파일러 사용                     |
| **Type-Safe Navigation**      | `Route.kt`            | `@Serializable` 기반 타입 안전 네비게이션 |
| **Material 3 / Material You** | `Theme.kt`            | Dynamic Color 지원 (Android 12+)          |
| **Gradle Version Catalog**    | `libs.versions.toml`  | 중앙 집중식 의존성 관리                   |
| **Flow 최신 패턴**            | `FlowExtensions.kt`   | UiState sealed class, WhileSubscribed     |
| **Compose Lifecycle**         | ViewModels            | `collectAsStateWithLifecycle()` 사용      |
| **Hilt DI**                   | `di/`                 | 모듈별 DI 분리, HiltViewModel             |
| **Proto DataStore**           | `datastore/`          | 타입 안전 영구 저장소                     |
| **테스트 인프라**             | `test/`               | Turbine, MockK, Truth                     |
| **Predictive Back**           | `AndroidManifest.xml` | enableOnBackInvokedCallback 적용          |
| **Baseline Profiles**         | `baselineprofile/`    | 앱 시작 성능 최적화                       |
| **Dispatcher 주입**           | `DispatcherModule.kt` | @IoDispatcher qualifier 정의              |

---

## 4. 완료된 개선 사항

### 4.1 OAuth 에러 UI 표시 ✅

**위치**: `AuthActivity.kt`

**구현 내용**:

- MutableSharedFlow 기반 에러 이벤트 관리
- Scaffold + SnackbarHost로 에러 메시지 표시
- OAuth 콜백 실패 시 사용자에게 Snackbar로 안내

```kotlin
private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)

LaunchedEffect(Unit) {
    errorEvent.collect { message ->
        snackbarHostState.showSnackbar(message)
    }
}
```

---

### 4.2 SafeApiCall 에러 처리 강화 ✅

**위치**: `SafeApiCall.kt`, `SessionManager.kt`

**구현 내용**:

- SessionManager 싱글톤으로 세션 이벤트 브로드캐스트
- 401 Unauthorized → SessionExpired 이벤트 발생
- 403 Forbidden → AccessDenied 이벤트 발생
- 429 Too Many Requests → RateLimited 이벤트 + Retry-After 헤더 파싱

```kotlin
when (statusCode) {
    HTTP_UNAUTHORIZED -> {
        sessionManager.notifySessionExpired()
        return AppError.Auth.SessionExpired
    }
    HTTP_FORBIDDEN -> {
        sessionManager.notifyAccessDenied()
        return AppError.Auth.Forbidden
    }
    HTTP_TOO_MANY_REQUESTS -> {
        val retryAfterSeconds = parseRetryAfterHeader(response)
        sessionManager.notifyRateLimited(retryAfterSeconds)
        return AppError.Network.RateLimited(retryAfterSeconds)
    }
}
```

---

### 4.3 Compose 리컴포지션 최적화 ✅

**적용 위치**:

- `ScreenHeader.kt`: TextStyle 상수를 컴포저블 외부로 추출
- `ExampleDividerWithText.kt`: TextStyle remember 메모라이제이션
- `ExampleLoadingOverlay.kt`: Color 상수화, 람다/InteractionSource 메모라이제이션

```kotlin
// 컴포저블 외부 불변 상수 - 리컴포지션 시 재생성 방지
private val BrandHeaderStyle = TextStyle(
    fontSize = 28.sp,
    fontWeight = FontWeight.Bold,
    color = Brand
)

private val OverlayBackgroundColor = Color.Black.copy(alpha = 0.3f)
```

---

### 4.4 Navigation Predictive Back 지원 ✅

**위치**: `AndroidManifest.xml`, `AuthActivity.kt`

**구현 내용**:

- `enableOnBackInvokedCallback="true"` 설정
- NavHost 전환 애니메이션 적용

---

### 4.5 Baseline Profiles ✅

**위치**: `baselineprofile/` 모듈

**구현 내용**:

- BaselineProfileGenerator 구현
- 주요 사용자 시나리오 (로그인, 회원가입) 포함
- 앱 시작 시간 20-30% 개선 기대

---

### 4.6 ViewModel Dispatcher 주입 ✅

**위치**: `DispatcherModule.kt`, `BaseViewModel.kt`

**구현 내용**:

- @IoDispatcher, @DefaultDispatcher qualifier 정의
- BaseViewModel에서 디스패처 주입 지원
- 테스트 시 TestDispatcher 주입 가능

---

### 4.7 RetryInterceptor Coroutine delay ✅

**위치**: `RetryInterceptor.kt`, `NetworkModule.kt`

**구현 내용**:

- Thread.sleep → Coroutine delay로 변경
- IoDispatcher 주입으로 테스트 시 TestDispatcher 사용 가능
- runBlocking + delay 조합으로 스레드 효율적 사용

```kotlin
private fun suspendDelay(delayMillis: Long) {
    val context = dispatcher ?: return runBlocking { delay(delayMillis) }
    runBlocking(context) { delay(delayMillis) }
}
```

---

### 4.8 API URL BuildConfig 분리 ✅

**위치**: `build.gradle.kts`, `AppConfig.kt`, `NetworkModule.kt`

**구현 내용**:

- `buildConfigField`로 debug/release 환경별 BASE_URL 분리
- `AppConfig.baseUrl`을 통해 NetworkModule에서 참조
- 하드코딩 제거

---

### 4.9 인증서 피닝 적용 ✅

**위치**: `SSLPinningConfig.kt`, `NetworkModule.kt`

**구현 내용**:

- `SSLPinningConfig.createCertificatePinner()`로 인증서 피닝 설정
- OkHttpClient에 적용

---

### 4.10 JWT 토큰 인증 마이그레이션 ✅

**위치**: `AuthInterceptor.kt`, `TokenManager.kt`

**구현 내용**:

- 쿠키 기반 세션 → JWT Bearer 토큰 인증으로 전환
- EncryptedSharedPreferences(AES-256-GCM)로 토큰 암호화 저장
- PersistentCookieJar 제거

---

### 4.11 Modifier.Node API 마이그레이션 ✅

**위치**: `ModifierExtensions.kt`

**구현 내용**:

- `Modifier.composed` 제거, `Modifier.Node` 기반으로 전환
- `RoundedBackgroundNode`, `ConstrainedHeightNode` 구현
- 메모리 효율성 및 리컴포지션 성능 향상

---

### 4.12 minSdk 26 상향 ✅

**위치**: `build.gradle.kts`

**구현 내용**:

- minSdk 24 → 26 (Android 8.0+)
- Java 8+ Time API 네이티브 지원
- desugaring 불필요

---

### 4.13 미사용 코드 정리 ✅

**구현 내용**:

- 미사용 파일 삭제: `AuthScreenLayout.kt`, `FlowExtensions.kt`, `UiError.kt`, `TestTags.kt`
- 미사용 함수/컴포저블 제거: `BrandHeader`, `SocialLoginIconRow`, `ExampleOutlinedButton`, `ifNotNull`, `shimmerEffect`, `expandedTouchTarget`, `CollectAsEventSimple`, `CollectAsEventOnMain` 외 다수
- 미사용 qualifier 제거: `@MainDispatcher`, `@DefaultDispatcher`

---

### 4.14 ViewModel 검증 에러 버그 수정 ✅

**위치**: `LogInViewModel.kt`, `SignUpViewModel.kt`

**구현 내용**:

- `emailError = emailError ?: emailErr` → `emailError = emailErr` 수정
- 기존 에러가 남아있어 새 검증 결과가 무시되는 버그 해결

---

### 4.15 AuthActivity 딥링크 에러 타이밍 수정 ✅

**위치**: `AuthActivity.kt`

**구현 내용**:

- `setContent()` 이전에 `showError()` 호출 시 구독자 없이 이벤트 드롭되는 문제 해결
- `pendingError` 패턴으로 에러를 저장 후 `LaunchedEffect`에서 표시

---

### 4.16 테스트 어설션 수정 ✅

**위치**: `LogInViewModelTest.kt`, `SignUpViewModelTest.kt`

**구현 내용**:

- `it.error` → `it.emailError`/`it.passwordError` (per-field 에러로 전환된 검증 로직에 맞게)
- `clearError` 테스트를 서버 에러 시나리오로 변경

---

### 4.17 하드코딩 문자열 리소스화 ✅

**위치**: `MainTab.kt`, `MainScreen.kt`, `strings_common.xml`

**구현 내용**:

- `MainTab.label: String` → `MainTab.labelResId: @StringRes Int` 변경
- `"First Tab"`, `"Second Tab"` → `stringResource(R.string.tab_first/tab_second)`
- 한국어 번역 추가

---

## 5. 리팩토링 필요 항목

### P0 — 긴급 (아키텍처/보안 결함)

#### 5.1 Domain → Data 계층 의존성 위반 `Architecture`

**위치**: `AuthRepository.kt`, `LogInUseCase.kt`, `SignUpUseCase.kt`

**현재 문제**:

- Domain 계층의 Repository 인터페이스가 Data 계층 DTO(`LoginRequest`, `SignUpRequest`)를 직접 참조
- Clean Architecture 원칙 위반: Domain은 외부 계층에 의존하면 안 됨

```kotlin
// domain/repository/AuthRepository.kt — DTO 직접 참조
suspend fun logIn(request: LoginRequest): Result<User>
suspend fun signUp(request: SignUpRequest): Result<User>
```

**개선안**:

- Repository 인터페이스를 원시 타입/도메인 모델로 변경
- DTO 변환을 Data 계층(Repository 구현체)에서 수행

```kotlin
// Domain 계층 — DTO 의존 제거
suspend fun logIn(email: String, hashedPassword: String): Result<User>
suspend fun signUp(email: String, hashedPassword: String, name: String): Result<User>
```

---

#### 5.2 DeepLinkHandler 하드코딩 한국어 문자열 `i18n`

**위치**: `DeepLinkHandler.kt`

**현재 문제**:

```kotlin
// Context 없이 하드코딩된 한국어 에러 메시지
"인증 코드가 없습니다"
"알 수 없는 딥링크입니다"
```

**개선안**:

- `DeepLinkResult`에 에러 코드를 전달하고, UI 계층에서 `getString()`으로 변환
- 또는 DeepLinkHandler에 Context를 주입하여 string resource 사용

---

#### 5.3 AppLogger 릴리스 빌드 보안 `Security`

**위치**: `AppLogger.kt`

**현재 문제**:

- 릴리스 빌드에서도 `Log.d()`, `Log.e()` 등 호출
- 민감한 정보(토큰, 에러 상세) 노출 가능

**개선안**:

```kotlin
object AppLogger {
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
        // 릴리스: Crashlytics/Sentry에만 전송
    }
}
```

---

#### 5.4 SocialProvider / LoginProvider 열거형 중복 `Architecture`

**위치**: `SocialProvider.kt`, `LoginProvider.kt`

**현재 문제**:

- `SocialProvider`(UI 계층)와 `LoginProvider`(Data 계층)가 동일한 값을 표현
- 매핑 로직이 여러 곳에 분산

**개선안**:

- `LoginProvider`를 Domain 계층 단일 모델로 통합
- UI/Data 계층에서 공통 모델 참조

---

### P1 — 높음 (코드 품질/유지보수성)

#### 5.5 AuthInterceptor 단일 책임 위반 `Architecture`

**위치**: `AuthInterceptor.kt`

**현재 문제**:

- 토큰 주입 + 토큰 갱신(refresh) + 동기화(synchronized) 3가지 책임을 한 클래스에서 처리

**개선안**:

- `TokenAttachInterceptor`: 요청에 토큰 첨부
- `TokenRefreshAuthenticator`: OkHttp `Authenticator` 인터페이스로 401 갱신 처리
- synchronized 블록 → `Mutex` 사용

---

#### 5.6 FormData 내 비즈니스 로직 `Architecture`

**위치**: `LogInFormData.kt`, `SignUpFormData.kt`

**현재 문제**:

- `data class`에 `validateEmail()`, `validatePassword()` 등 비즈니스 로직 포함
- Data class는 데이터 보관 목적이므로 검증 로직 분리 필요

**개선안**:

- `AuthValidator` 클래스로 검증 로직 추출
- FormData는 순수 데이터 홀더로 유지

---

#### 5.7 validateAll()이 첫 번째 에러만 반환 `UX`

**위치**: `LogInFormData.kt`, `SignUpFormData.kt`

**현재 문제**:

```kotlin
fun validateAll(): ValidationResult {
    validateEmail().let { if (it is Failure) return it }  // 여기서 중단
    validatePassword().let { if (it is Failure) return it }
    return ValidationResult.Success
}
```

- 사용자가 여러 필드 에러를 동시에 볼 수 없음

**참고**: 현재 ViewModel에서 per-field 검증을 별도로 수행하므로 `validateAll()`은 실질적으로 미사용 상태. 제거하거나 전체 검증 결과를 반환하도록 변경 필요.

---

#### 5.8 MeUseCase 인증 상태 갱신 누락 `Logic`

**위치**: `MeUseCase.kt`

**현재 문제**:

- `me()` API 성공 시 캐시만 업데이트하고, `AuthManager`의 인증 상태(Flow)를 갱신하지 않음
- 토큰 만료 등으로 `me()` 실패 시에도 인증 상태가 유지됨

**개선안**:

- `me()` 성공 시 `authManager.setAuthenticated(user)` 호출
- 401 실패 시 `authManager.setUnauthenticated()` 호출

---

#### 5.9 접근성 미흡 `Accessibility`

**위치**: UI 컴포넌트 전반

**현재 문제**:

- 일부 `Icon`에 `contentDescription = null` (스크린 리더 무시됨)
- 터치 타겟 사이즈 48dp 미만인 요소 존재
- Semantics 속성 미지정

**개선안**:

- 의미 있는 아이콘에 `contentDescription` 추가
- `Modifier.minimumInteractiveComponentSize()` 적용
- 폼 필드에 `Modifier.semantics { }` 활용

---

### P2 — 중간 (코드 개선)

#### 5.10 ViewModel 검증 패턴 중복 `DRY`

**위치**: `LogInViewModel.kt`, `SignUpViewModel.kt`

**현재 문제**:

```kotlin
// 동일 패턴이 두 ViewModel에 반복
fun updateEmail(email: String) {
    updateState { copy(formData = formData.copy(email = email), emailError = null) }
}
fun validateEmail() {
    val result = uiState.value.formData.validateEmail()
    if (result is Failure) updateState { copy(emailError = result.error) }
}
```

**개선안**:

- `BaseAuthViewModel`로 공통 검증 패턴 추출 또는
- 위임 클래스(`AuthFormHandler`)로 분리

---

#### 5.11 미사용 CachePolicy 변형 `Dead Code`

**위치**: `CachePolicy.kt`

**현재 문제**:

- `CachePolicy.CacheOnly`, `CachePolicy.NetworkOnly` 등 선언만 있고 사용처 없음

**개선안**: 미사용 변형 제거하고 실제 사용 중인 것만 유지

---

#### 5.12 RequestCoalescingInterceptor `!!` 사용 `Safety`

**위치**: `RequestCoalescingInterceptor.kt`

**현재 문제**:

```kotlin
val cached = inFlightRequests[cacheKey]!!  // NPE 위험
```

**개선안**: `?.let { }` 또는 `requireNotNull()` 사용

---

#### 5.13 UserCache 동시성 경쟁 조건 `Concurrency`

**위치**: `UserCache.kt`

**현재 문제**:

- 일반 `HashMap` 사용, 동시 접근 시 데이터 손상 가능

**개선안**: `ConcurrentHashMap` 또는 `Mutex`로 동시성 보호

---

#### 5.14 SignUpScreen buildAnnotatedString 메모이제이션 `Performance`

**위치**: `SignUpScreen.kt`

**현재 문제**:

- `buildAnnotatedString { }` 블록이 리컴포지션마다 재생성

**개선안**: `remember { }` 로 래핑

---

#### 5.15 하드코딩된 dp 값 `Consistency`

**위치**: UI 컴포넌트 전반

**현재 문제**:

- `8.dp`, `16.dp`, `24.dp` 등 매직 넘버 산재
- `Dimensions.kt`에 이미 정의된 상수 미활용

**개선안**: `ExampleDimensions` 상수 일관 사용

---

#### 5.16 SignUpScreen 버튼 alpha 중복 `Redundancy`

**위치**: `SignUpScreen.kt`

**현재 문제**:

- `ExampleButton`이 이미 `enabled` 파라미터로 시각적 상태 처리하는데, 외부에서 `alpha(0.6f)`를 중복 적용

**개선안**: 외부 alpha 제거, `ExampleButton.enabled`에 위임

---

#### 5.17 플레이스홀더 테스트 파일 `Testing`

**위치**: `test/` 내 일부 파일

**현재 문제**:

- `// TODO: 테스트 작성` 주석만 있는 파일 존재

**개선안**: 실제 테스트 작성 또는 파일 제거

---

### P3 — 낮음 (장기 개선)

#### 5.18 테스트 커버리지 부족 `Testing`

**위치**: 전체 프로젝트

**현재 문제**:

- 15개 이상 클래스에 대응 테스트 없음
  - `AuthInterceptor`, `SafeApiCall`, `RetryInterceptor`
  - `TokenManager`, `NetworkMonitor`, `DeepLinkHandler`
  - `PasswordHasher`, `AppLogger`, `UserCache`
  - 등

**개선안**: 핵심 인프라 클래스부터 단위 테스트 추가

---

#### 5.19 캐시 로직 중복 `DRY`

**위치**: `MemoryCache.kt`, `SingleValueCache.kt`, `DiskCache.kt`

**현재 문제**:

- TTL 관리, 만료 체크 로직이 3개 캐시 구현에 각각 존재

**개선안**: 공통 `CacheEntry<T>` + `BaseTtlCache` 추상 클래스로 통합

---

#### 5.20 LogoutUseCase 에러 무시 `Error Handling`

**위치**: `LogoutUseCase.kt`

**현재 문제**:

- 서버 로그아웃 API 실패 시에도 로컬 세션만 삭제하고 사용자에게 알리지 않음

**개선안**: 서버 실패 시에도 로컬 클린업은 수행하되, 결과를 `Result`로 반환하여 UI에서 선택적 처리

---

#### 5.21 User 모델 검증 부재 `Validation`

**위치**: `User.kt`

**현재 문제**:

- 서버 응답을 그대로 신뢰, 빈 문자열/잘못된 이메일 형식 등 미검증

**개선안**: `User.validate()` 또는 생성 시 `require()` 블록 추가

---

#### 5.22 collectAsEvent 네이밍 일관성 `Convention`

**위치**: `FlowCollector.kt`

**현재 문제**:

- `collectAsEvent`라는 이름이 Compose의 `collectAsState` 패턴과 유사하지만 다른 동작 (LaunchedEffect 기반 일회성 소비)

**개선안**: `LaunchedEventCollector` 또는 `ObserveAsEvents`로 이름 변경하여 의도 명확화

---

#### 5.23 테마 미관리 색상 `Consistency`

**위치**: UI 컴포넌트 내 직접 색상 참조

**현재 문제**:

- `Color.Black`, `Color.White`, `Color.Gray` 등 MaterialTheme 외부 색상 직접 사용
- 다크 모드 전환 시 적절히 변경되지 않을 수 있음

**개선안**: `MaterialTheme.colorScheme` 또는 `ExtendedColors`로 통합

---

## 6. 미적용 최신 트렌드

### 6.1 멀티모듈 아키텍처 `P3`

**현재**: 단일 앱 모듈
**권장**: Feature 기반 모듈 분리

```
example-android/
├── app/                    # 앱 진입점
├── core/
│   ├── common/            # 공통 유틸리티
│   ├── network/           # 네트워크 레이어
│   ├── data/              # 데이터 레이어
│   └── ui/                # 공통 UI 컴포넌트
├── feature/
│   ├── auth/              # 인증 기능
│   └── main/              # 메인 화면
└── baselineprofile/       # 성능 프로파일
```

---

## 7. 개선 로드맵

### 우선순위별 정리

```
완료 ✅ (17건)
├── OAuth 에러 UI 표시 구현
├── SafeApiCall 에러 처리 강화
├── Compose 리컴포지션 최적화
├── Navigation Predictive Back 지원
├── Baseline Profiles 구현
├── ViewModel Dispatcher 주입
├── RetryInterceptor Coroutine delay 적용
├── API URL BuildConfig 분리
├── 인증서 피닝 적용 (SSLPinningConfig)
├── JWT 토큰 인증 마이그레이션 (CookieJar 제거)
├── Modifier.Node API 마이그레이션
├── minSdk 26 상향
├── 미사용 코드 정리
├── ViewModel 검증 에러 버그 수정
├── AuthActivity 딥링크 에러 타이밍 수정
├── 테스트 어설션 수정
└── 하드코딩 문자열 리소스화

P0 — 긴급 (4건)
├── 5.1 Domain → Data 계층 의존성 위반
├── 5.2 DeepLinkHandler 하드코딩 한국어 문자열
├── 5.3 AppLogger 릴리스 빌드 보안
└── 5.4 SocialProvider / LoginProvider 열거형 중복

P1 — 높음 (5건)
├── 5.5 AuthInterceptor 단일 책임 위반
├── 5.6 FormData 내 비즈니스 로직
├── 5.7 validateAll() 첫 번째 에러만 반환 / 미사용
├── 5.8 MeUseCase 인증 상태 갱신 누락
└── 5.9 접근성 미흡 (contentDescription, 터치 타겟)

P2 — 중간 (8건)
├── 5.10 ViewModel 검증 패턴 중복
├── 5.11 미사용 CachePolicy 변형
├── 5.12 RequestCoalescingInterceptor !! 사용
├── 5.13 UserCache 동시성 경쟁 조건
├── 5.14 SignUpScreen buildAnnotatedString 메모이제이션
├── 5.15 하드코딩된 dp 값
├── 5.16 SignUpScreen 버튼 alpha 중복
└── 5.17 플레이스홀더 테스트 파일

P3 — 낮음 (6건 + 멀티모듈)
├── 5.18 테스트 커버리지 부족 (15개+ 클래스)
├── 5.19 캐시 로직 중복
├── 5.20 LogoutUseCase 에러 무시
├── 5.21 User 모델 검증 부재
├── 5.22 collectAsEvent 네이밍 일관성
├── 5.23 테마 미관리 색상
└── 6.1 멀티모듈 아키텍처
```

### 예상 효과

| 개선 항목                | 예상 효과                        | 상태    |
| ------------------------ | -------------------------------- | ------- |
| Baseline Profiles        | 앱 시작 시간 20-30% 단축         | ✅ 완료 |
| Compose 최적화           | UI 렌더링 성능 향상              | ✅ 완료 |
| 에러 처리 강화           | 사용자 경험 개선, 안정성 향상    | ✅ 완료 |
| Predictive Back          | Android 14+ 사용자 경험 개선     | ✅ 완료 |
| Dispatcher 주입          | 테스트 용이성 향상               | ✅ 완료 |
| RetryInterceptor         | 스레드 효율성 향상               | ✅ 완료 |
| API URL BuildConfig      | 환경별 URL 분리                  | ✅ 완료 |
| 인증서 피닝              | MITM 공격 방지                   | ✅ 완료 |
| JWT 마이그레이션         | 보안 강화, 상태비저장 인증       | ✅ 완료 |
| Modifier.Node            | 메모리/리컴포지션 성능 향상      | ✅ 완료 |
| minSdk 26                | 레거시 코드 제거                 | ✅ 완료 |
| 미사용 코드 정리         | 코드베이스 간소화                | ✅ 완료 |
| 검증 버그 수정           | 인라인 에러 정상 표시            | ✅ 완료 |
| 문자열 리소스화          | 국제화(i18n) 완성                | ✅ 완료 |
| Domain 계층 의존성 수정  | Clean Architecture 원칙 준수     | 🔴 P0  |
| DeepLinkHandler i18n     | 다국어 에러 메시지               | 🔴 P0  |
| AppLogger 릴리스 보안    | 민감 정보 노출 방지              | 🔴 P0  |
| Provider 열거형 통합     | 중복 제거, 유지보수 용이         | 🔴 P0  |
| AuthInterceptor SRP      | 책임 분리, 테스트 용이           | 🟠 P1  |
| 접근성 개선              | 장애인 사용자 지원               | 🟠 P1  |
| ViewModel 중복 제거      | DRY 원칙 준수                    | 🟡 P2  |
| UserCache 동시성         | 동시 접근 안정성                 | 🟡 P2  |
| 테스트 커버리지          | 코드 신뢰성 향상                 | 🔵 P3  |
| 멀티모듈 아키텍처        | 빌드 속도/확장성 향상            | 🔵 P3  |

---

## 변경 이력

| 날짜       | 버전 | 변경 내용                                       |
| ---------- | ---- | ----------------------------------------------- |
| 2026-01-26 | 1.0  | 최초 분석 문서 작성                             |
| 2026-01-26 | 1.1  | SafeApiCall, OAuth UI, Compose 최적화 완료 반영 |
| 2026-01-26 | 1.2  | RetryInterceptor Coroutine delay 적용 완료      |
| 2026-02-25 | 2.0  | 대규모 업데이트: 미사용 코드 정리, 검증 버그 수정, 딥링크 에러 타이밍 수정, 테스트 수정, 문자열 리소스화. 이미 완료된 P1/P2 항목(API URL, 인증서 피닝, JWT, Modifier.Node, minSdk) 반영 |
| 2026-02-25 | 3.0  | 전체 코드 재분석: P0 4건, P1 5건, P2 8건, P3 6건 총 23개 리팩토링 항목 추가. 이전 기존 항목(패스워드 해싱, 캐시 갱신, Circuit Breaker 등)은 새 분석에 통합 |

---

## 참고 자료

- [Android Developer - Performance](https://developer.android.com/topic/performance)
- [Jetpack Compose Performance](https://developer.android.com/jetpack/compose/performance)
- [Baseline Profiles](https://developer.android.com/topic/performance/baselineprofiles)
- [Predictive Back Gesture](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)
- [Modifier.Node](https://developer.android.com/reference/kotlin/androidx/compose/ui/node/ModifierNodeElement)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-top-10/)
