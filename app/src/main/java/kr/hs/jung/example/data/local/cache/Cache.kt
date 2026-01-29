package kr.hs.jung.example.data.local.cache

import kotlin.time.Duration

/**
 * 캐시 인터페이스
 *
 * 데이터 캐싱을 위한 범용 인터페이스입니다.
 * 메모리 캐시와 디스크 캐시 모두 이 인터페이스를 구현합니다.
 *
 * @param K 캐시 키 타입
 * @param V 캐시 값 타입
 */
interface Cache<K, V> {
    /**
     * 캐시에서 값을 가져옵니다.
     *
     * @param key 캐시 키
     * @return 캐시된 값 또는 null (만료된 경우 포함)
     */
    suspend fun get(key: K): V?

    /**
     * 캐시에 값을 저장합니다.
     *
     * @param key 캐시 키
     * @param value 저장할 값
     * @param ttl 캐시 유효 기간 (null이면 기본 TTL 사용)
     */
    suspend fun put(key: K, value: V, ttl: Duration? = null)

    /**
     * 특정 키의 캐시를 제거합니다.
     *
     * @param key 제거할 캐시 키
     */
    suspend fun remove(key: K)

    /**
     * 모든 캐시를 제거합니다.
     */
    suspend fun clear()

    /**
     * 특정 키가 캐시에 존재하는지 확인합니다.
     *
     * @param key 확인할 캐시 키
     * @return 존재 여부 (만료된 경우 false)
     */
    suspend fun contains(key: K): Boolean

    /**
     * 만료된 캐시 항목을 정리합니다.
     */
    suspend fun evictExpired()
}

/**
 * 캐시 엔트리
 *
 * 캐시된 값과 만료 시간을 함께 저장합니다.
 *
 * @param V 값 타입
 * @param value 캐시된 값
 * @param expiresAt 만료 시간 (밀리초 타임스탬프)
 */
data class CacheEntry<V>(
    val value: V,
    val expiresAt: Long
) {
    /**
     * 캐시가 만료되었는지 확인합니다.
     */
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt

    companion object {
        /**
         * 캐시 엔트리를 생성합니다.
         *
         * @param value 캐시할 값
         * @param ttl 유효 기간
         */
        fun <V> create(value: V, ttl: Duration): CacheEntry<V> {
            return CacheEntry(
                value = value,
                expiresAt = System.currentTimeMillis() + ttl.inWholeMilliseconds
            )
        }
    }
}

/**
 * 캐시 정책
 *
 * 캐시의 기본 동작을 정의합니다.
 *
 * @param defaultTtl 기본 TTL
 * @param maxSize 최대 캐시 크기 (메모리 캐시용)
 */
data class CachePolicy(
    val defaultTtl: Duration,
    val maxSize: Int = 100
) {
    companion object {
        /**
         * 기본 캐시 정책 (5분 TTL, 100개 항목)
         */
        val Default = CachePolicy(
            defaultTtl = Duration.parse("5m"),
            maxSize = 100
        )

        /**
         * 짧은 TTL 정책 (1분)
         */
        val ShortLived = CachePolicy(
            defaultTtl = Duration.parse("1m"),
            maxSize = 50
        )

        /**
         * 긴 TTL 정책 (30분)
         */
        val LongLived = CachePolicy(
            defaultTtl = Duration.parse("30m"),
            maxSize = 200
        )

        /**
         * 세션 정책 (앱 재시작 전까지)
         */
        val Session = CachePolicy(
            defaultTtl = Duration.parse("24h"),
            maxSize = 50
        )
    }
}
