package kr.hs.jung.example.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kr.hs.jung.example.data.local.datastore.CookieProto
import kr.hs.jung.example.data.local.datastore.EncryptedCookieStorage
import kr.hs.jung.example.util.logger.AppLogger
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 쿠키를 EncryptedSharedPreferences에 암호화 저장하는 CookieJar
 *
 * 앱 프로세스가 종료되어도 쿠키가 유지됩니다.
 * AES-256-GCM 암호화로 쿠키를 안전하게 보호합니다.
 *
 * 전략:
 * - 읽기: 메모리 캐시에서 즉시 반환 (동기)
 * - 쓰기: 메모리 캐시 즉시 갱신 + 암호화 저장소 비동기 저장
 * - 초기화: 앱 시작 시 암호화 저장소에서 메모리로 로드
 */
@Singleton
class PersistentCookieJar @Inject constructor(
    private val encryptedStorage: EncryptedCookieStorage
) : CookieJar {

    // 메모리 캐시 (스레드 안전)
    private val memoryCookies = mutableListOf<CookieProto>()

    // 비동기 디스크 저장을 위한 코루틴 스코프
    private val diskScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // 초기화 여부
    @Volatile
    private var isInitialized = false

    companion object {
        private const val TAG = "PersistentCookieJar"
    }

    /**
     * 암호화 저장소에서 메모리로 쿠키를 로드합니다.
     * 앱 시작 시 호출되어야 합니다.
     */
    fun initialize() {
        if (isInitialized) return

        synchronized(memoryCookies) {
            if (isInitialized) return

            try {
                val stored = encryptedStorage.load()
                memoryCookies.clear()
                memoryCookies.addAll(stored)
                isInitialized = true
                AppLogger.d(TAG, "Loaded ${memoryCookies.size} cookies from encrypted storage")
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to load cookies: ${e.message}", e)
                isInitialized = true
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // 메모리 캐시 동기 갱신
        synchronized(memoryCookies) {
            cookies.forEach { newCookie ->
                memoryCookies.removeAll {
                    it.name == newCookie.name && it.domain == newCookie.domain
                }
                memoryCookies.add(newCookie.toProto())
            }
        }

        // 암호화 저장소 비동기 저장
        diskScope.launch {
            try {
                encryptedStorage.save(memoryCookies.toList())
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to save cookies: ${e.message}", e)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (!isInitialized) {
            AppLogger.d(TAG, "CookieJar not initialized, returning empty")
            return emptyList()
        }

        val now = System.currentTimeMillis()

        return synchronized(memoryCookies) {
            memoryCookies
                .mapNotNull { proto -> proto.toCookie() }
                .filter { cookie ->
                    cookie.expiresAt > now && cookie.matches(url)
                }
        }
    }

    /**
     * 모든 쿠키를 제거합니다.
     */
    fun clear() {
        synchronized(memoryCookies) {
            memoryCookies.clear()
        }
        encryptedStorage.clear()
        AppLogger.d(TAG, "Cookies cleared")
    }

    private fun Cookie.toProto(): CookieProto = CookieProto(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresAt = expiresAt,
        secure = secure,
        httpOnly = httpOnly
    )

    private fun CookieProto.toCookie(): Cookie = Cookie.Builder()
        .name(name)
        .value(value)
        .domain(domain)
        .path(path)
        .expiresAt(expiresAt)
        .apply {
            if (secure) secure()
            if (httpOnly) httpOnly()
        }
        .build()
}
