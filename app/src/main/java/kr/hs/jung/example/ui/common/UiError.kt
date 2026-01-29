package kr.hs.jung.example.ui.common

import android.content.Context
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.AppException

/**
 * UI 에러 상태를 표현하는 sealed class
 *
 * ViewModel에서 Context 없이 에러를 표현하고,
 * UI에서 Context를 사용하여 문자열로 변환합니다.
 */
sealed class UiError {

    /**
     * Context를 사용하여 에러 메시지를 반환
     */
    abstract fun getMessage(context: Context): String

    /**
     * AppError 기반 에러
     *
     * Domain 레이어의 AppError를 래핑합니다.
     */
    data class App(val error: AppError) : UiError() {
        override fun getMessage(context: Context): String =
            ErrorMessageResolver.getMessage(context, error)
    }

    /**
     * 단순 문자열 에러 (이미 지역화된 메시지)
     */
    data class Text(val message: String) : UiError() {
        override fun getMessage(context: Context): String = message
    }

    companion object {
        /**
         * Throwable을 UiError로 변환
         */
        fun from(error: Throwable): UiError {
            return when (error) {
                is AppException -> App(error.appError)
                else -> App(AppError.from(error))
            }
        }

        /**
         * AppError를 UiError로 변환
         */
        fun from(error: AppError): UiError = App(error)
    }
}
