@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package kr.hs.jung.example.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineDispatcher
import kr.hs.jung.example.data.remote.NetworkConstants
import kr.hs.jung.example.data.remote.PersistentCookieJar
import kr.hs.jung.example.data.remote.RetryInterceptor
import kr.hs.jung.example.data.remote.SSLPinningConfig
import kr.hs.jung.example.data.remote.SanitizingLoggingInterceptor
import kr.hs.jung.example.data.remote.api.AuthApi
import kr.hs.jung.example.util.config.AppConfig
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 관련 DI 모듈
 *
 * OkHttp, Retrofit, JSON 직렬화 등 네트워크 통신에 필요한
 * 의존성들을 제공합니다.
 *
 * 주요 구성요소:
 * - Json: kotlinx.serialization JSON 인스턴스
 * - OkHttpClient: 타임아웃, 재시도, 쿠키 관리 설정
 * - Retrofit: API 인터페이스 구현체 생성
 * - AuthApi: 인증 API 인터페이스
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): SanitizingLoggingInterceptor = SanitizingLoggingInterceptor()

    @Provides
    @Singleton
    fun provideRetryInterceptor(
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): RetryInterceptor = RetryInterceptor(dispatcher = dispatcher)

    @Provides
    @Singleton
    fun provideConnectionPool(): ConnectionPool = ConnectionPool(
        maxIdleConnections = NetworkConstants.CONNECTION_POOL_SIZE,
        keepAliveDuration = NetworkConstants.CONNECTION_KEEP_ALIVE_MINUTES,
        timeUnit = TimeUnit.MINUTES
    )

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: SanitizingLoggingInterceptor,
        retryInterceptor: RetryInterceptor,
        connectionPool: ConnectionPool,
        cookieJar: PersistentCookieJar
    ): OkHttpClient = OkHttpClient.Builder()
        // 타임아웃 설정 (연결은 짧게, 읽기/쓰기는 길게)
        .connectTimeout(NetworkConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(NetworkConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(NetworkConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        // 연결 풀 설정 (연결 재사용으로 성능 향상)
        .connectionPool(connectionPool)
        // 재시도 인터셉터 (네트워크 인터셉터로 추가)
        .addInterceptor(retryInterceptor)
        // 로깅 인터셉터 (Debug 빌드에서만)
        .apply {
            if (AppConfig.isLoggingEnabled) {
                addInterceptor(loggingInterceptor as Interceptor)
            }
        }
        // SSL 인증서 피닝 (핀이 비어있으면 기본 SSL 검증)
        .certificatePinner(SSLPinningConfig.createCertificatePinner())
        // 쿠키 관리
        .cookieJar(cookieJar)
        // 리다이렉트 자동 처리
        .followRedirects(true)
        .followSslRedirects(true)
        // 실패 시 재연결 허용
        .retryOnConnectionFailure(true)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(AppConfig.baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)
}
