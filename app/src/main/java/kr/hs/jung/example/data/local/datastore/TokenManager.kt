package kr.hs.jung.example.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import kr.hs.jung.example.util.logger.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT 토큰 암호화 저장소
 *
 * EncryptedSharedPreferences(AES-256-GCM)를 사용하여
 * accessToken과 refreshToken을 안전하게 저장합니다.
 *
 * 특징:
 * - MasterKey(AES-256-GCM)로 자동 암호화/복호화
 * - 키와 값 모두 암호화
 * - 동기식 읽기/쓰기 (SharedPreferences 기반)
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
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
            AppLogger.e(TAG, "Failed to create EncryptedSharedPreferences, recreating", e)
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
     * 액세스 토큰 조회
     *
     * @return 저장된 액세스 토큰, 없으면 null
     */
    fun getAccessToken(): String? {
        return try {
            prefs.getString(KEY_ACCESS_TOKEN, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get access token: ${e.message}", e)
            null
        }
    }

    /**
     * 리프레시 토큰 조회
     *
     * @return 저장된 리프레시 토큰, 없으면 null
     */
    fun getRefreshToken(): String? {
        return try {
            prefs.getString(KEY_REFRESH_TOKEN, null)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to get refresh token: ${e.message}", e)
            null
        }
    }

    /**
     * 토큰 저장
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        try {
            prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply()
            AppLogger.d(TAG, "Tokens saved successfully")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to save tokens: ${e.message}", e)
        }
    }

    /**
     * 저장된 모든 토큰 삭제
     */
    fun clearTokens() {
        try {
            prefs.edit().clear().apply()
            AppLogger.d(TAG, "Tokens cleared")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to clear tokens: ${e.message}", e)
        }
    }

    /**
     * 토큰 보유 여부 확인
     *
     * @return 액세스 토큰이 존재하면 true
     */
    fun hasTokens(): Boolean {
        return getAccessToken() != null
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_FILE_NAME = "encrypted_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
