package kr.hs.jung.example.data.remote.api

import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<UserDto>

    @POST("auth/exchange")
    suspend fun exchange(@Body request: ExchangeRequestDto): Response<UserDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("user")
    suspend fun signUp(@Body request: SignUpRequestDto): Response<UserDto>

    @GET("auth/me")
    suspend fun me(): Response<UserDto>
}
