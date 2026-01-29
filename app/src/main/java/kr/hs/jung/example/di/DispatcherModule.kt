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
 * Main 스레드용 Dispatcher Qualifier
 *
 * UI 업데이트에 사용합니다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

/**
 * CPU 집약적 작업용 Dispatcher Qualifier
 *
 * 계산, 정렬 등 CPU를 많이 사용하는 작업에 사용합니다.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

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

    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
