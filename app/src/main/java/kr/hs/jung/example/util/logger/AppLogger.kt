package kr.hs.jung.example.util.logger

import android.util.Log
import kr.hs.jung.example.BuildConfig

/**
 * 앱 전체에서 사용하는 통합 로깅 유틸리티
 *
 * - 일관된 태그 형식 사용 (TAG_PREFIX-컴포넌트명)
 * - Debug/Release 빌드에 따른 로그 레벨 제어
 * - 중앙화된 로그 관리로 추후 원격 로깅 통합 용이
 */
object AppLogger {

    private const val TAG_PREFIX = "Example"

    /**
     * Debug 로그 (Debug 빌드에서만 출력)
     */
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(formatTag(tag), message)
        }
    }

    /**
     * Info 로그 (Debug 빌드에서만 출력)
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(formatTag(tag), message)
        }
    }

    /**
     * Warning 로그 (Debug 빌드에서만 출력)
     */
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(formatTag(tag), message, throwable)
            } else {
                Log.w(formatTag(tag), message)
            }
        }
    }

    /**
     * Error 로그 (항상 출력)
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(formatTag(tag), message, throwable)
        } else {
            Log.e(formatTag(tag), message)
        }
    }

    /**
     * 네트워크 관련 로그 (Debug 빌드에서만 출력)
     */
    fun network(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(formatTag("Network"), message)
        }
    }

    /**
     * 태그 형식 통일
     */
    private fun formatTag(tag: String): String {
        return "$TAG_PREFIX-$tag"
    }
}
