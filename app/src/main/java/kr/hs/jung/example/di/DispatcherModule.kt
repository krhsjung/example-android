package kr.hs.jung.example.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * IO 작업용 Dispatcher Qualifier
 *
 * 파일 I/O, 네트워크 요청 등에 사용합니다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Coroutine Dispatcher DI 모듈
 *
 * 테스트 시 TestDispatcher로 교체할 수 있도록
 * Dispatcher들을 주입 가능하게 제공합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
