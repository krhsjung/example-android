# OkHttp CookieJar를 이용한 쿠키 기반 세션 관리

## 개요

OkHttp는 기본적으로 쿠키를 저장하거나 전송하지 않습니다. 서버가 `Set-Cookie` 헤더로 쿠키를 보내도, 다음 요청에 자동으로 포함되지 않습니다.

쿠키 기반 세션 관리를 위해서는 `CookieJar` 인터페이스를 구현하여 OkHttpClient에 설정해야 합니다.

## 왜 CookieJar가 필요한가?

### HTTP 쿠키의 동작 방식

1. 클라이언트가 로그인 요청을 보냄
2. 서버가 인증 후 `Set-Cookie: session_id=abc123` 헤더로 응답
3. 클라이언트는 이후 요청마다 `Cookie: session_id=abc123` 헤더를 포함해야 함
4. 서버는 쿠키로 세션을 식별하여 인증된 사용자로 처리

### OkHttp의 기본 동작

```kotlin
// CookieJar 없이 OkHttpClient 생성
val client = OkHttpClient.Builder().build()

// 로그인 요청 -> 서버가 Set-Cookie 응답
// 다음 요청 -> Cookie 헤더 없음 (세션 인식 불가!)
```

OkHttp는 브라우저와 달리 쿠키를 자동으로 관리하지 않습니다. `CookieJar`를 설정하지 않으면:
- 서버가 보낸 쿠키가 저장되지 않음
- 다음 요청에 쿠키가 포함되지 않음
- 매 요청마다 인증되지 않은 상태로 처리됨

## CookieJar 인터페이스

```kotlin
interface CookieJar {
    // 서버 응답에서 쿠키를 받아 저장
    fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>)

    // 요청 전에 해당 URL에 보낼 쿠키 반환
    fun loadForRequest(url: HttpUrl): List<Cookie>
}
```

## 구현 방식

### 1. 메모리 기반 (앱 종료 시 세션 소멸)

```kotlin
@Singleton
class MemoryCookieJar @Inject constructor() : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.getOrPut(url.host) { mutableListOf() }.apply {
            cookies.forEach { newCookie ->
                removeAll { it.name == newCookie.name }
                add(newCookie)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host]?.filter {
            it.expiresAt > System.currentTimeMillis()
        } ?: emptyList()
    }

    fun clear() {
        cookieStore.clear()
    }
}
```

**장점**: 구현이 간단함
**단점**: 앱 프로세스가 종료되면 세션이 사라짐

### 2. SharedPreferences 기반 (앱 종료 후에도 세션 유지)

```kotlin
@Singleton
class PersistentCookieJar @Inject constructor(
    @ApplicationContext context: Context
) : CookieJar {

    private val prefs = context.getSharedPreferences("cookies", Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val existingCookies = loadAllCookies().toMutableSet()
        cookies.forEach { newCookie ->
            existingCookies.removeAll { it.name == newCookie.name && it.domain == newCookie.domain }
            existingCookies.add(newCookie)
        }
        saveCookies(existingCookies)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return loadAllCookies().filter { cookie ->
            cookie.expiresAt > System.currentTimeMillis() && cookie.matches(url)
        }
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun saveCookies(cookies: Set<Cookie>) {
        // Cookie를 문자열로 직렬화하여 저장
    }

    private fun loadAllCookies(): Set<Cookie> {
        // 저장된 문자열을 Cookie로 역직렬화
    }
}
```

**장점**: 앱을 완전히 종료해도 세션이 유지됨
**단점**: 직렬화/역직렬화 로직이 필요함

## OkHttpClient에 적용

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        cookieJar: PersistentCookieJar
    ): OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)  // CookieJar 설정
        .build()
}
```

## 세션 클리어 (로그아웃)

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val cookieJar: PersistentCookieJar
) : AuthRepository {

    override fun clearSession() {
        cookieJar.clear()  // 저장된 모든 쿠키 삭제
    }
}
```

## 요청/응답 흐름

```
[로그인 요청]
POST /api/auth/login
Body: { email, password }

[서버 응답]
HTTP 200 OK
Set-Cookie: session_id=abc123; Path=/; HttpOnly
Body: { user info }

        ↓ CookieJar.saveFromResponse() 호출
        ↓ 쿠키 저장됨

[이후 API 요청]
GET /api/users/me
Cookie: session_id=abc123  ← CookieJar.loadForRequest()가 자동 추가

[서버 응답]
HTTP 200 OK
Body: { user info }
```

## 정리

| 항목 | CookieJar 없음 | CookieJar 있음 |
|------|---------------|---------------|
| 쿠키 저장 | X | O |
| 쿠키 자동 전송 | X | O |
| 세션 유지 | X | O |
| 로그인 상태 유지 | X | O |

쿠키 기반 세션 관리를 사용하는 서버와 통신할 때 `CookieJar`는 **필수**입니다.
