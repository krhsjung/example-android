package kr.hs.jung.example.data.local.datastore

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 쿠키 데이터 모델
 */
@Serializable
data class CookieProto(
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
    @SerialName("domain") val domain: String,
    @SerialName("path") val path: String,
    @SerialName("expiresAt") val expiresAt: Long,
    @SerialName("secure") val secure: Boolean,
    @SerialName("httpOnly") val httpOnly: Boolean
)

/**
 * 쿠키 저장소 모델
 */
@Serializable
data class CookieStore(
    @SerialName("cookies") val cookies: List<CookieProto> = emptyList()
)
