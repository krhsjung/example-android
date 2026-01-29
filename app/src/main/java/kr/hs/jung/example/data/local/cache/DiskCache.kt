package kr.hs.jung.example.data.local.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.hs.jung.example.util.logger.AppLogger
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration

/**
 * 디스크 캐시 엔트리
 *
 * DataStore에 저장되는 캐시 데이터 형식입니다.
 *
 * @param data JSON으로 직렬화된 데이터
 * @param expiresAt 만료 시간 (밀리초 타임스탬프)
 */
@Serializable
data class DiskCacheEntry(
    val data: String,
    val expiresAt: Long
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiresAt
}

/**
 * 디스크 캐시 저장소
 *
 * 여러 캐시 엔트리를 키-값 형태로 저장합니다.
 *
 * @param entries 캐시 엔트리 맵
 */
@Serializable
data class DiskCacheStore(
    val entries: Map<String, DiskCacheEntry> = emptyMap()
)

/**
 * DiskCacheStore를 위한 DataStore Serializer
 */
class DiskCacheStoreSerializer(
    private val json: Json
) : Serializer<DiskCacheStore> {

    override val defaultValue: DiskCacheStore = DiskCacheStore()

    override suspend fun readFrom(input: InputStream): DiskCacheStore {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = input.readBytes()
                if (bytes.isEmpty()) {
                    defaultValue
                } else {
                    json.decodeFromString(DiskCacheStore.serializer(), bytes.decodeToString())
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to read disk cache: ${e.message}", e)
                defaultValue
            }
        }
    }

    override suspend fun writeTo(t: DiskCacheStore, output: OutputStream) {
        withContext(Dispatchers.IO) {
            try {
                output.write(json.encodeToString(DiskCacheStore.serializer(), t).encodeToByteArray())
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to write disk cache: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "DiskCacheStoreSerializer"
    }
}

/**
 * DataStore 확장 프로퍼티
 */
val Context.diskCacheDataStore: DataStore<DiskCacheStore> by dataStore(
    fileName = "disk_cache.pb",
    serializer = DiskCacheStoreSerializer(Json { ignoreUnknownKeys = true })
)

/**
 * 디스크 캐시 구현
 *
 * DataStore를 사용하여 데이터를 영구 저장하는 캐시입니다.
 * 앱 재시작 후에도 캐시가 유지됩니다.
 *
 * 특징:
 * - 영구 저장 (앱 재시작 후에도 유지)
 * - TTL 기반 만료
 * - JSON 직렬화
 *
 * @param V 캐시 값 타입
 * @param dataStore DataStore 인스턴스
 * @param serializer 값 직렬화기
 * @param json JSON 인스턴스
 * @param policy 캐시 정책
 */
class DiskCache<V : Any>(
    private val dataStore: DataStore<DiskCacheStore>,
    private val serializer: KSerializer<V>,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val policy: CachePolicy = CachePolicy.LongLived
) : Cache<String, V> {

    override suspend fun get(key: String): V? {
        val store = dataStore.data.first()
        val entry = store.entries[key] ?: return null

        if (entry.isExpired()) {
            remove(key)
            return null
        }

        return try {
            json.decodeFromString(serializer, entry.data)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to deserialize cache entry: ${e.message}", e)
            remove(key)
            null
        }
    }

    override suspend fun put(key: String, value: V, ttl: Duration?) {
        val effectiveTtl = ttl ?: policy.defaultTtl
        val serializedData = json.encodeToString(serializer, value)
        val entry = DiskCacheEntry(
            data = serializedData,
            expiresAt = System.currentTimeMillis() + effectiveTtl.inWholeMilliseconds
        )

        dataStore.updateData { store ->
            store.copy(entries = store.entries + (key to entry))
        }
    }

    override suspend fun remove(key: String) {
        dataStore.updateData { store ->
            store.copy(entries = store.entries - key)
        }
    }

    override suspend fun clear() {
        dataStore.updateData { DiskCacheStore() }
    }

    override suspend fun contains(key: String): Boolean {
        val store = dataStore.data.first()
        val entry = store.entries[key] ?: return false

        if (entry.isExpired()) {
            remove(key)
            return false
        }

        return true
    }

    override suspend fun evictExpired() {
        dataStore.updateData { store ->
            val validEntries = store.entries.filterValues { !it.isExpired() }
            store.copy(entries = validEntries)
        }
    }

    /**
     * 캐시에서 값을 가져오거나, 없으면 loader를 실행합니다.
     */
    suspend fun getOrLoad(
        key: String,
        ttl: Duration? = null,
        loader: suspend () -> V?
    ): V? {
        get(key)?.let { return it }

        val value = loader() ?: return null
        put(key, value, ttl)
        return value
    }

    companion object {
        private const val TAG = "DiskCache"
    }
}

/**
 * 단일 값 디스크 캐시
 *
 * 하나의 값만 영구 저장하는 간단한 캐시입니다.
 *
 * @param V 캐시 값 타입
 * @param dataStore DataStore 인스턴스
 * @param key 캐시 키
 * @param serializer 값 직렬화기
 * @param json JSON 인스턴스
 * @param defaultTtl 기본 TTL
 */
class SingleValueDiskCache<V : Any>(
    private val dataStore: DataStore<DiskCacheStore>,
    private val key: String,
    private val serializer: KSerializer<V>,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val defaultTtl: Duration = CachePolicy.LongLived.defaultTtl
) {
    suspend fun get(): V? {
        val store = dataStore.data.first()
        val entry = store.entries[key] ?: return null

        if (entry.isExpired()) {
            clear()
            return null
        }

        return try {
            json.decodeFromString(serializer, entry.data)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to deserialize cache entry: ${e.message}", e)
            clear()
            null
        }
    }

    suspend fun set(value: V, ttl: Duration? = null) {
        val effectiveTtl = ttl ?: defaultTtl
        val serializedData = json.encodeToString(serializer, value)
        val entry = DiskCacheEntry(
            data = serializedData,
            expiresAt = System.currentTimeMillis() + effectiveTtl.inWholeMilliseconds
        )

        dataStore.updateData { store ->
            store.copy(entries = store.entries + (key to entry))
        }
    }

    suspend fun clear() {
        dataStore.updateData { store ->
            store.copy(entries = store.entries - key)
        }
    }

    suspend fun isValid(): Boolean {
        val store = dataStore.data.first()
        val entry = store.entries[key] ?: return false
        return !entry.isExpired()
    }

    suspend fun getOrLoad(
        ttl: Duration? = null,
        loader: suspend () -> V?
    ): V? {
        get()?.let { return it }

        val value = loader() ?: return null
        set(value, ttl)
        return value
    }

    companion object {
        private const val TAG = "SingleValueDiskCache"
    }
}
