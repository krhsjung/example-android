package kr.hs.jung.example.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EncryptedSharedPreferences를 사용한 암호화 쿠키 저장소
 *
 * iOS의 Keychain/SecureCookieStorage에 대응하는 Android 구현입니다.
 * AES-256 GCM 암호화로 쿠키 데이터를 안전하게 저장합니다.
 *
 * 특징:
 * - MasterKey(AES-256-GCM)로 자동 암호화/복호화
 * - 키와 값 모두 암호화
 * - 동기식 읽기/쓰기 (SharedPreferences 기반)
 */
@Singleton
class EncryptedCookieStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to create EncryptedSharedPreferences, falling back to clear", e)
            // 암호화 생성 실패 시 기존 파일 삭제 후 재생성
            context.deleteSharedPreferences(PREFS_FILE_NAME)
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                PREFS_FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    /**
     * 저장된 쿠키 목록을 읽어옵니다.
     */
    fun load(): List<CookieProto> {
        return try {
            val jsonString = prefs.getString(KEY_COOKIES, null) ?: return emptyList()
            val store = json.decodeFromString(CookieStore.serializer(), jsonString)
            store.cookies
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to load cookies: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * 쿠키 목록을 암호화하여 저장합니다.
     */
    fun save(cookies: List<CookieProto>) {
        try {
            val store = CookieStore(cookies = cookies)
            val jsonString = json.encodeToString(CookieStore.serializer(), store)
            prefs.edit().putString(KEY_COOKIES, jsonString).apply()
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save cookies: ${e.message}", e)
        }
    }

    /**
     * 저장된 모든 쿠키를 삭제합니다.
     */
    fun clear() {
        try {
            prefs.edit().clear().apply()
            AppLogger.d(TAG, "Encrypted cookies cleared")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to clear cookies: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "EncryptedCookieStorage"
        private const val PREFS_FILE_NAME = "encrypted_cookies"
        private const val KEY_COOKIES = "cookie_store"
    }
}
