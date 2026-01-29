package kr.hs.jung.example.data.local.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * 메모리 캐시 구현
 *
 * LRU (Least Recently Used) 정책을 사용하는 스레드 안전한 메모리 캐시입니다.
 * LinkedHashMap의 accessOrder를 활용하여 LRU를 구현합니다.
 *
 * 특징:
 * - 스레드 안전 (Mutex 사용)
 * - TTL 기반 만료
 * - LRU 기반 크기 제한
 * - 자동 만료 정리
 *
 * @param K 캐시 키 타입
 * @param V 캐시 값 타입
 * @param policy 캐시 정책
 */
class MemoryCache<K : Any, V : Any>(
    private val policy: CachePolicy = CachePolicy.Default
) : Cache<K, V> {

    private val mutex = Mutex()

    // accessOrder = true로 LRU 순서 유지
    private val cache = object : LinkedHashMap<K, CacheEntry<V>>(
        policy.maxSize,
        0.75f,
        true // accessOrder
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, CacheEntry<V>>?): Boolean {
            return size > policy.maxSize
        }
    }

    override suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key] ?: return@withLock null

        if (entry.isExpired()) {
            cache.remove(key)
            return@withLock null
        }

        entry.value
    }

    override suspend fun put(key: K, value: V, ttl: Duration?) = mutex.withLock {
        val effectiveTtl = ttl ?: policy.defaultTtl
        cache[key] = CacheEntry.create(value, effectiveTtl)
    }

    override suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
        Unit
    }

    override suspend fun clear() = mutex.withLock {
        cache.clear()
    }

    override suspend fun contains(key: K): Boolean = mutex.withLock {
        val entry = cache[key] ?: return@withLock false

        if (entry.isExpired()) {
            cache.remove(key)
            return@withLock false
        }

        true
    }

    override suspend fun evictExpired() = mutex.withLock {
        val iterator = cache.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().value.isExpired()) {
                iterator.remove()
            }
        }
    }

    /**
     * 현재 캐시 크기를 반환합니다.
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }

    /**
     * 캐시에서 값을 가져오거나, 없으면 loader를 실행하여 캐시에 저장합니다.
     *
     * @param key 캐시 키
     * @param ttl 캐시 TTL (null이면 기본값 사용)
     * @param loader 캐시 미스 시 실행할 로더
     * @return 캐시된 값 또는 로더에서 반환된 값
     */
    suspend fun getOrPut(
        key: K,
        ttl: Duration? = null,
        loader: suspend () -> V
    ): V {
        // 먼저 캐시에서 조회
        get(key)?.let { return it }

        // 캐시 미스: 로더 실행 후 캐시에 저장
        val value = loader()
        put(key, value, ttl)
        return value
    }

    /**
     * 캐시에서 값을 가져오거나, 없으면 loader를 실행합니다.
     * loader 결과가 null이면 캐시에 저장하지 않습니다.
     *
     * @param key 캐시 키
     * @param ttl 캐시 TTL (null이면 기본값 사용)
     * @param loader 캐시 미스 시 실행할 로더
     * @return 캐시된 값 또는 로더에서 반환된 값 (null 가능)
     */
    suspend fun getOrLoad(
        key: K,
        ttl: Duration? = null,
        loader: suspend () -> V?
    ): V? {
        // 먼저 캐시에서 조회
        get(key)?.let { return it }

        // 캐시 미스: 로더 실행
        val value = loader() ?: return null
        put(key, value, ttl)
        return value
    }
}

/**
 * 단일 값 메모리 캐시
 *
 * 하나의 값만 저장하는 간단한 캐시입니다.
 * User 정보 등 단일 엔티티 캐싱에 적합합니다.
 *
 * @param V 캐시 값 타입
 * @param defaultTtl 기본 TTL
 */
class SingleValueCache<V : Any>(
    private val defaultTtl: Duration = CachePolicy.Default.defaultTtl
) {
    private val mutex = Mutex()
    private var entry: CacheEntry<V>? = null

    /**
     * 캐시된 값을 가져옵니다.
     */
    suspend fun get(): V? = mutex.withLock {
        val current = entry ?: return@withLock null

        if (current.isExpired()) {
            entry = null
            return@withLock null
        }

        current.value
    }

    /**
     * 값을 캐시합니다.
     */
    suspend fun set(value: V, ttl: Duration? = null) = mutex.withLock {
        entry = CacheEntry.create(value, ttl ?: defaultTtl)
    }

    /**
     * 캐시를 제거합니다.
     */
    suspend fun clear() = mutex.withLock {
        entry = null
    }

    /**
     * 캐시가 존재하고 유효한지 확인합니다.
     */
    suspend fun isValid(): Boolean = mutex.withLock {
        val current = entry ?: return@withLock false
        !current.isExpired()
    }

    /**
     * 캐시된 값을 가져오거나, 없으면 loader를 실행합니다.
     */
    suspend fun getOrLoad(
        ttl: Duration? = null,
        loader: suspend () -> V?
    ): V? {
        get()?.let { return it }

        val value = loader() ?: return null
        set(value, ttl)
        return value
    }
}
