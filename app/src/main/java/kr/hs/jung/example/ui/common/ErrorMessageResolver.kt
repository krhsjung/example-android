package kr.hs.jung.example.ui.common

import android.content.Context
import kr.hs.jung.example.R
import kr.hs.jung.example.domain.model.AppError

/**
 * AppError를 사용자에게 표시할 문자열로 변환하는 리졸버
 *
 * UI 레이어에서 사용하며, Android 리소스 접근은 이 클래스에서만 처리합니다.
 * Domain 레이어의 AppError는 플랫폼 독립적으로 유지됩니다.
 */
object ErrorMessageResolver {

    /**
     * AppError를 지역화된 에러 메시지로 변환
     *
     * @param context Android Context
     * @param error 변환할 AppError
     * @return 지역화된 에러 메시지
     */
    fun getMessage(context: Context, error: AppError): String {
        return when (error) {
            // Network errors
            is AppError.Network.NoData -> context.getString(R.string.error_no_data)
            is AppError.Network.InvalidUrl -> context.getString(R.string.error_invalid_url)
            is AppError.Network.Timeout -> context.getString(R.string.error_timeout)
            is AppError.Network.NoConnection -> context.getString(R.string.error_no_connection)
            is AppError.Network.Decoding -> context.getString(R.string.error_decoding)
            is AppError.Network.Encoding -> context.getString(R.string.error_encoding)
            is AppError.Network.Server -> resolveServerError(context, error)
            is AppError.Network.RateLimited -> resolveRateLimitedError(context, error)
            is AppError.Network.Unknown -> error.cause.message
                ?: context.getString(R.string.error_unknown)

            // Auth errors
            is AppError.Auth.Unauthorized -> context.getString(R.string.error_unauthorized)
            is AppError.Auth.Forbidden -> context.getString(R.string.error_forbidden)
            is AppError.Auth.SessionExpired -> context.getString(R.string.error_session_expired)
            is AppError.Auth.InvalidCredentials -> context.getString(R.string.server_auth_credentials_invalid)

            // Validation errors
            is AppError.Validation.EmptyEmail -> context.getString(R.string.error_empty_email)
            is AppError.Validation.InvalidEmail -> context.getString(R.string.error_invalid_email)
            is AppError.Validation.EmptyPassword -> context.getString(R.string.error_empty_password)
            is AppError.Validation.WeakPassword -> context.getString(R.string.error_weak_password)
            is AppError.Validation.PasswordMismatch -> context.getString(R.string.error_password_mismatch)
            is AppError.Validation.EmptyUsername -> context.getString(R.string.error_empty_username)
            is AppError.Validation.NameTooShort -> context.getString(R.string.error_name_min_length)
            is AppError.Validation.PasswordNoUppercase -> context.getString(R.string.error_password_no_uppercase)
            is AppError.Validation.PasswordNoLowercase -> context.getString(R.string.error_password_no_lowercase)
            is AppError.Validation.PasswordNoNumber -> context.getString(R.string.error_password_no_number)
            is AppError.Validation.PasswordNoSpecialCharacter -> context.getString(R.string.error_password_no_special_character)
            is AppError.Validation.Custom -> error.reason

            // Business errors
            is AppError.Business.UserNotFound -> context.getString(R.string.server_user_not_found)
            is AppError.Business.DuplicateUser -> context.getString(R.string.server_user_already_exists)
            is AppError.Business.Custom -> error.message
                ?: context.getString(R.string.error_unknown)

            // Generic error
            is AppError.Generic -> error.message
                ?: error.cause?.message
                ?: context.getString(R.string.error_unknown)
        }
    }

    /**
     * Rate Limit 에러 메시지 변환
     */
    private fun resolveRateLimitedError(
        context: Context,
        error: AppError.Network.RateLimited
    ): String {
        return if (error.retryAfterSeconds != null && error.retryAfterSeconds > 0) {
            context.getString(R.string.error_rate_limited_with_retry, error.retryAfterSeconds)
        } else {
            context.getString(R.string.error_rate_limited)
        }
    }

    /**
     * 서버 에러 코드에 따른 메시지 변환
     */
    private fun resolveServerError(context: Context, error: AppError.Network.Server): String {
        // 서버 메시지가 있으면 우선 사용
        if (!error.serverMessage.isNullOrBlank()) {
            return error.serverMessage
        }

        // 에러 코드에 따른 문자열 리소스 매핑
        val resId = when (error.errorCode) {
            "server_auth_credentials_invalid" -> R.string.server_auth_credentials_invalid
            "server_auth_provider_unsupported" -> R.string.server_auth_provider_unsupported
            "server_auth_session_not_found" -> R.string.server_auth_session_not_found
            "server_auth_session_invalid" -> R.string.server_auth_session_invalid
            "server_auth_code_invalid" -> R.string.server_auth_code_invalid
            "server_user_not_found" -> R.string.server_user_not_found
            "server_user_already_exists" -> R.string.server_user_already_exists
            else -> null
        }

        return if (resId != null) {
            context.getString(resId)
        } else {
            // HTTP 상태 코드에 따른 기본 메시지
            when (error.statusCode) {
                400 -> context.getString(R.string.error_bad_request)
                404 -> context.getString(R.string.error_not_found)
                500, 502, 503 -> context.getString(R.string.error_server)
                else -> context.getString(R.string.error_unknown)
            }
        }
    }
}

/**
 * AppError 확장 함수: Context를 사용하여 메시지 가져오기
 */
fun AppError.getMessage(context: Context): String =
    ErrorMessageResolver.getMessage(context, this)
