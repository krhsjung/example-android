package kr.hs.jung.example.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.hs.jung.example.data.local.cache.UserCache
import javax.inject.Singleton

/**
 * 캐시 관련 의존성 주입 모듈
 *
 * 캐시 인스턴스들을 Hilt로 관리합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    /**
     * UserCache 제공
     *
     * 사용자 정보 캐싱을 담당하는 싱글톤 인스턴스입니다.
     */
    @Provides
    @Singleton
    fun provideUserCache(
        @ApplicationContext context: Context
    ): UserCache = UserCache(context)
}
