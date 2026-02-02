package kr.hs.jung.example.data.remote.api

import kr.hs.jung.example.data.remote.dto.AuthResponseDto
import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.RefreshTokenRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 인증 관련 API 인터페이스
 *
 * Retrofit을 사용한 인증 API 정의입니다.
 * 모든 응답은 Response로 래핑하여 SafeApiCall에서 에러 처리가 가능하도록 합니다.
 *
 * 인증 흐름:
 * - login/signUp/exchange → AuthResponseDto (accessToken + refreshToken + user)
 * - me/logout → 기존 응답 유지 (Authorization 헤더는 AuthInterceptor가 자동 추가)
 * - refresh → AuthResponseDto (AuthInterceptor에서 OkHttp로 직접 호출)
 */
interface AuthApi {

    /** 이메일/비밀번호 로그인 (모바일 JWT 응답) */
    @POST("auth/login/mobile")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>

    /** OAuth 인증 코드 교환 */
    @POST("auth/exchange")
    suspend fun exchange(@Body request: ExchangeRequestDto): Response<AuthResponseDto>

    /** 로그아웃 */
    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    /** 이메일 회원가입 (모바일 JWT 응답) */
    @POST("auth/register/mobile")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<AuthResponseDto>

    /** 현재 로그인된 사용자 정보 조회 */
    @GET("auth/me")
    suspend fun me(): Response<UserDto>

    /** 토큰 갱신 */
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): Response<AuthResponseDto>
}
