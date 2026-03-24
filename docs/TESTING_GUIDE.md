# Testing Guide

이 문서는 example-android 프로젝트의 테스트 작성 및 실행 가이드입니다.

---

## 목차

1. [테스트 환경 설정](#1-테스트-환경-설정)
2. [테스트 실행 방법](#2-테스트-실행-방법)
3. [테스트 구조](#3-테스트-구조)
4. [테스트 작성 가이드](#4-테스트-작성-가이드)
5. [테스트 유틸리티](#5-테스트-유틸리티)
6. [예제 코드](#6-예제-코드)
7. [Best Practices](#7-best-practices)

---

## 1. 테스트 환경 설정

### 의존성

테스트에 사용되는 라이브러리들:

```kotlin
// build.gradle.kts
dependencies {
    // Unit Test
    testImplementation(libs.junit)                    // JUnit 4
    testImplementation(libs.kotlinx.coroutines.test)  // Coroutine 테스트
    testImplementation(libs.mockk)                    // Mocking
    testImplementation(libs.truth)                    // Assertion
    testImplementation(libs.turbine)                  // Flow 테스트
    testImplementation(libs.arch.core.testing)        // InstantTaskExecutor

    // Android Instrumented Test
    androidTestImplementation(libs.hilt.android.testing)  // Hilt 테스트
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)  // Compose UI 테스트
}
```

### 라이브러리 설명

| 라이브러리 | 용도 |
|-----------|------|
| **JUnit 4** | 기본 테스트 프레임워크 |
| **MockK** | Kotlin 친화적인 Mocking 라이브러리 |
| **Truth** | Google의 가독성 높은 Assertion 라이브러리 |
| **Turbine** | Flow 테스트를 위한 라이브러리 |
| **Coroutines Test** | 코루틴 테스트 지원 (TestDispatcher) |

---

## 2. 테스트 실행 방법

### Android Studio에서 실행

1. **단일 테스트 실행**: 테스트 메서드 옆의 ▶️ 버튼 클릭
2. **클래스 전체 실행**: 클래스 이름 옆의 ▶️ 버튼 클릭
3. **패키지 전체 실행**: 패키지 우클릭 → Run Tests

### 커맨드라인에서 실행

```bash
# 모든 Unit Test 실행
./gradlew test

# 특정 모듈의 Unit Test 실행
./gradlew :app:testDebugUnitTest

# 특정 클래스만 실행
./gradlew :app:testDebugUnitTest --tests "kr.hs.jung.example.ui.auth.login.LogInViewModelTest"

# 특정 메서드만 실행
./gradlew :app:testDebugUnitTest --tests "*.LogInViewModelTest.updateEmail*"

# 테스트 리포트 생성
./gradlew test --continue
# 리포트 위치: app/build/reports/tests/
```

### Instrumented Test 실행

```bash
# 에뮬레이터/기기에서 실행
./gradlew connectedAndroidTest
```

---

## 3. 테스트 구조

### 디렉토리 구조

```
app/src/
├── main/java/kr/hs/jung/example/
│   ├── ui/auth/login/LogInViewModel.kt
│   ├── domain/usecase/auth/LogInUseCase.kt
│   └── data/repository/AuthRepositoryImpl.kt
│
├── test/java/kr/hs/jung/example/          # Unit Tests
│   ├── util/
│   │   ├── MainDispatcherRule.kt          # Coroutine 테스트 Rule
│   │   └── TestFixtures.kt                # 테스트 데이터 팩토리
│   ├── ui/auth/login/
│   │   └── LogInViewModelTest.kt
│   ├── domain/usecase/auth/
│   │   └── LogInUseCaseTest.kt
│   └── data/repository/
│       └── AuthRepositoryImplTest.kt
│
└── androidTest/java/kr/hs/jung/example/   # Instrumented Tests
    └── ui/auth/login/
        └── LogInScreenTest.kt
```

### 테스트 네이밍 컨벤션

```kotlin
// 메서드 이름: `행동_조건_예상결과` 또는 backtick 사용
@Test
fun `updateEmail updates email in state`() { }

@Test
fun `logIn with empty email shows validation error`() { }

@Test
fun `logIn success emits Success event`() { }
```

---

## 4. 테스트 작성 가이드

### 4.1 ViewModel 테스트

ViewModel 테스트는 UI 로직과 상태 변화를 검증합니다.

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LogInViewModelTest {

    // Dispatchers.Main을 TestDispatcher로 교체
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var logInUseCase: LogInUseCase
    private lateinit var viewModel: LogInViewModel

    @Before
    fun setup() {
        // Mock 객체 생성
        logInUseCase = mockk()
        viewModel = LogInViewModel(logInUseCase)
    }

    @Test
    fun `updateEmail updates email in state`() {
        // When: 이메일 업데이트
        viewModel.updateEmail("test@test.com")

        // Then: 상태 확인
        assertThat(viewModel.uiState.value.email).isEqualTo("test@test.com")
    }

    @Test
    fun `logIn success emits Success event`() = runTest {
        // Given: UseCase 성공 설정
        coEvery { logInUseCase(any(), any()) } returns Result.success(testUser)

        viewModel.updateEmail("test@test.com")
        viewModel.updatePassword("ValidPass123!")

        // When & Then: 이벤트 수집 및 검증
        viewModel.event.test {
            viewModel.logIn()
            assertThat(awaitItem()).isEqualTo(LogInEvent.Success)
        }
    }
}
```

**검증 포인트:**
- 상태 업데이트 (`uiState`)
- 이벤트 발생 (`event`)
- 입력 검증 로직
- 로딩 상태 변화
- UseCase 호출 여부

### 4.2 UseCase 테스트

UseCase 테스트는 비즈니스 로직을 검증합니다.

```kotlin
class LogInUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var authManager: AuthManager
    private lateinit var useCase: LogInUseCase

    @Before
    fun setup() {
        authRepository = mockk()
        authManager = mockk(relaxed = true)  // relaxed: 기본 동작 자동 설정
        useCase = LogInUseCase(authRepository, authManager)
    }

    @Test
    fun `invoke saves user to AuthManager on success`() = runTest {
        // Given
        val testUser = TestFixtures.createUser()
        coEvery { authRepository.login(any()) } returns Result.success(testUser)

        // When
        val result = useCase("test@test.com", "password")

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { authManager.setUser(testUser) }  // 호출 검증
    }
}
```

**검증 포인트:**
- 입력 데이터 전처리 (trim 등)
- Repository 호출 여부
- 성공 시 후처리 (AuthManager 저장 등)
- 실패 시 동작

### 4.3 Repository 테스트

Repository 테스트는 API 응답 처리를 검증합니다.

```kotlin
class AuthRepositoryImplTest {

    private lateinit var authApi: AuthApi
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        authApi = mockk()
        repository = AuthRepositoryImpl(authApi, mockk(relaxed = true), SafeApiCall())
    }

    @Test
    fun `login returns mapped domain model on success`() = runTest {
        // Given: API 성공 응답
        val userDto = UserDto(id = "1", name = "Test", email = "test@test.com")
        coEvery { authApi.login(any()) } returns Response.success(userDto)

        // When
        val result = repository.login(LoginRequestDto("test@test.com", "pass"))

        // Then: DTO → Domain 변환 확인
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()?.email).isEqualTo("test@test.com")
    }

    @Test
    fun `login returns failure on 401 response`() = runTest {
        // Given: 401 응답
        coEvery { authApi.login(any()) } returns Response.error(
            401, "Unauthorized".toResponseBody(null)
        )

        // When
        val result = repository.login(LoginRequestDto("test@test.com", "wrong"))

        // Then
        assertThat(result.isFailure).isTrue()
    }
}
```

**검증 포인트:**
- 성공 응답 → Domain 모델 변환
- HTTP 에러 코드별 처리 (401, 404, 500 등)
- 네트워크 에러 처리

---

## 5. 테스트 유틸리티

### MainDispatcherRule

Coroutine 테스트를 위한 JUnit Rule입니다. `Dispatchers.Main`을 `TestDispatcher`로 교체합니다.

```kotlin
// util/MainDispatcherRule.kt
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

// 사용법
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
}
```

### TestFixtures

테스트용 객체 생성 팩토리입니다.

```kotlin
// util/TestFixtures.kt
object TestFixtures {
    fun createUser(
        id: String = "test-user-id",
        name: String = "Test User",
        email: String = "test@test.com"
    ) = User(id = id, name = name, email = email)
}

// 사용법
val testUser = TestFixtures.createUser(email = "custom@test.com")
```

---

## 6. 예제 코드

### MockK 사용법

```kotlin
// Mock 생성
val mockRepository = mockk<AuthRepository>()

// relaxed mock (기본 동작 자동 설정)
val mockManager = mockk<AuthManager>(relaxed = true)

// 동작 설정 (suspend 함수)
coEvery { mockRepository.login(any()) } returns Result.success(user)

// 동작 설정 (일반 함수)
every { mockManager.currentUser } returns flowOf(user)

// 호출 검증 (suspend 함수)
coVerify { mockRepository.login(any()) }

// 호출 검증 (일반 함수)
verify { mockManager.setUser(user) }

// 호출 횟수 검증
coVerify(exactly = 0) { mockRepository.logout() }
coVerify(exactly = 1) { mockRepository.login(any()) }

// 인자 캡처
val slot = slot<LoginRequestDto>()
coEvery { mockRepository.login(capture(slot)) } returns Result.success(user)
// ... 호출 후
assertThat(slot.captured.email).isEqualTo("test@test.com")
```

### Truth Assertion 사용법

```kotlin
// 기본 assertion
assertThat(result).isTrue()
assertThat(result).isFalse()
assertThat(value).isNull()
assertThat(value).isNotNull()

// 값 비교
assertThat(value).isEqualTo(expected)
assertThat(list).containsExactly(item1, item2)
assertThat(list).isEmpty()

// 타입 검증
assertThat(error).isInstanceOf(UiError.Resource::class.java)

// 문자열
assertThat(text).contains("expected")
assertThat(text).startsWith("prefix")
```

### Turbine (Flow 테스트) 사용법

```kotlin
@Test
fun `event flow test`() = runTest {
    viewModel.event.test {
        // 이벤트 발생 트리거
        viewModel.doSomething()

        // 이벤트 수신 및 검증
        val event = awaitItem()
        assertThat(event).isEqualTo(ExpectedEvent)

        // 추가 이벤트 검증
        val event2 = awaitItem()
        assertThat(event2).isEqualTo(AnotherEvent)

        // 이벤트가 없어야 할 때
        expectNoEvents()

        // 에러 예상
        awaitError()

        // 완료 예상
        awaitComplete()

        // 남은 이벤트 취소
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 7. Best Practices

### Given-When-Then 패턴

```kotlin
@Test
fun `login success saves user`() = runTest {
    // Given: 테스트 환경 설정
    val testUser = TestFixtures.createUser()
    coEvery { repository.login(any()) } returns Result.success(testUser)

    // When: 테스트 대상 실행
    val result = useCase("test@test.com", "password")

    // Then: 결과 검증
    assertThat(result.isSuccess).isTrue()
    coVerify { authManager.setUser(testUser) }
}
```

### 테스트 격리

```kotlin
@Before
fun setup() {
    // 매 테스트마다 새로운 Mock 객체 생성
    mockRepository = mockk()
    viewModel = MyViewModel(mockRepository)
}

@After
fun tearDown() {
    // 필요시 정리 작업
    clearAllMocks()
}
```

### 에지 케이스 테스트

```kotlin
// 빈 입력
@Test
fun `empty email shows error`() { }

// 경계값
@Test
fun `password with exactly 8 characters is valid`() { }

// null 처리
@Test
fun `null response is handled gracefully`() { }

// 네트워크 에러
@Test
fun `network timeout shows error message`() { }
```

### 테스트 커버리지 목표

| 레이어 | 권장 커버리지 |
|--------|-------------|
| ViewModel | 80%+ |
| UseCase | 90%+ |
| Repository | 70%+ |
| Domain Model | 95%+ |

---

## 추가 리소스

- [MockK 공식 문서](https://mockk.io/)
- [Truth 공식 문서](https://truth.dev/)
- [Turbine GitHub](https://github.com/cashapp/turbine)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Android Testing Guide](https://developer.android.com/training/testing)
