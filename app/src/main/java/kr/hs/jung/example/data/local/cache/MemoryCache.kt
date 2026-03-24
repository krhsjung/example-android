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

}
