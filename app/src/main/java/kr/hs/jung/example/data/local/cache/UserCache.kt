package kr.hs.jung.example.data.local.cache

import android.content.Context
import androidx.datastore.core.DataStore
import kotlinx.serialization.Serializable
import kr.hs.jung.example.domain.model.LoginProvider
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * User 캐시용 직렬화 모델
 *
 * Domain 모델인 User를 직렬화하기 위한 DTO입니다.
 */
@Serializable
data class CachedUser(
    val id: String,
    val name: String,
    val email: String,
    val picture: String? = null,
    val provider: String? = null,
    val maxSessions: Int? = null
) {
    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
        picture = picture,
        provider = provider?.let { LoginProvider.entries.find { p -> p.value == it } },
        maxSessions = maxSessions
    )

    companion object {
        fun fromDomain(user: User): CachedUser = CachedUser(
            id = user.id,
            name = user.name,
            email = user.email,
            picture = user.picture,
            provider = user.provider?.value,
            maxSessions = user.maxSessions
        )
    }
}

/**
 * User 캐시 관리자
 *
 * 메모리 캐시와 디스크 캐시를 조합하여 사용자 정보를 캐싱합니다.
 *
 * 캐시 전략:
 * 1. 메모리 캐시 조회 (빠름, 5분 TTL)
 * 2. 메모리 미스 → 디스크 캐시 조회 (느림, 30분 TTL)
 * 3. 디스크 히트 → 메모리 캐시 갱신
 * 4. 디스크 미스 → 네트워크 요청
 * 5. 네트워크 성공 → 메모리 + 디스크 캐시 갱신
 *
 * @param context 앱 컨텍스트 (DataStore 접근용)
 */
@Singleton
class UserCache @Inject constructor(
    private val context: Context
) {
    // 메모리 캐시 (5분 TTL)
    private val memoryCache = SingleValueCache<User>(
        defaultTtl = MEMORY_TTL
    )

    // 디스크 캐시 (30분 TTL)
    private val diskCache: SingleValueDiskCache<CachedUser> by lazy {
        SingleValueDiskCache(
            dataStore = context.diskCacheDataStore,
            key = CACHE_KEY,
            serializer = CachedUser.serializer(),
            defaultTtl = DISK_TTL
        )
    }

    /**
     * 캐시에서 사용자 정보를 가져옵니다.
     *
     * @return 캐시된 사용자 정보 또는 null
     */
    suspend fun get(): User? {
        // 1. 메모리 캐시 조회
        memoryCache.get()?.let {
            AppLogger.d(TAG, "Memory cache hit")
            return it
        }

        // 2. 디스크 캐시 조회
        val cachedUser = diskCache.get()
        if (cachedUser != null) {
            AppLogger.d(TAG, "Disk cache hit")
            val user = cachedUser.toDomain()
            // 메모리 캐시 갱신
            memoryCache.set(user)
            return user
        }

        AppLogger.d(TAG, "Cache miss")
        return null
    }

    /**
     * 사용자 정보를 캐시에 저장합니다.
     *
     * @param user 저장할 사용자 정보
     */
    suspend fun set(user: User) {
        AppLogger.d(TAG, "Caching user: ${user.id}")
        // 메모리 캐시 저장
        memoryCache.set(user)
        // 디스크 캐시 저장
        diskCache.set(CachedUser.fromDomain(user))
    }

    /**
     * 모든 캐시를 제거합니다.
     */
    suspend fun clear() {
        AppLogger.d(TAG, "Clearing user cache")
        memoryCache.clear()
        diskCache.clear()
    }

    /**
     * 캐시가 유효한지 확인합니다.
     */
    suspend fun isValid(): Boolean {
        return memoryCache.isValid() || diskCache.isValid()
    }

    /**
     * 캐시에서 값을 가져오거나, 없으면 loader를 실행합니다.
     *
     * @param loader 캐시 미스 시 실행할 로더 (네트워크 요청 등)
     * @return 사용자 정보 또는 null
     */
    suspend fun getOrLoad(loader: suspend () -> User?): User? {
        // 캐시 조회
        get()?.let { return it }

        // 캐시 미스: 로더 실행
        val user = loader() ?: return null

        // 캐시 저장
        set(user)
        return user
    }

    /**
     * 캐시에서 값을 가져오거나, 없으면 loader를 실행합니다.
     * Result를 반환하는 loader를 지원합니다.
     *
     * @param loader 캐시 미스 시 실행할 로더
     * @return Result<User>
     */
    suspend fun getOrLoadResult(loader: suspend () -> Result<User>): Result<User> {
        // 캐시 조회
        get()?.let { return Result.success(it) }

        // 캐시 미스: 로더 실행
        return loader().onSuccess { user ->
            set(user)
        }
    }

    companion object {
        private const val TAG = "UserCache"
        private const val CACHE_KEY = "current_user"
        private val MEMORY_TTL: Duration = 5.minutes
        private val DISK_TTL: Duration = 30.minutes
    }
}
