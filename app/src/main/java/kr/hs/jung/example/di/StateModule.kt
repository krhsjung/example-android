package kr.hs.jung.example.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.hs.jung.example.domain.state.AuthStateHolder
import kr.hs.jung.example.ui.common.state.AuthState
import javax.inject.Singleton

/**
 * 상태 관리 DI 모듈
 *
 * Domain 계층의 인터페이스와 UI 계층의 구현체를 바인딩합니다.
 * 의존성 역전 원칙을 적용하여 Domain → UI 의존성을 제거합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StateModule {

    /**
     * AuthStateHolder 인터페이스를 AuthState 구현체에 바인딩
     *
     * Domain 계층의 UseCase들이 AuthStateHolder 인터페이스를 통해
     * 사용자 상태에 접근할 수 있도록 합니다.
     */
    @Binds
    @Singleton
    abstract fun bindAuthStateHolder(
        authState: AuthState
    ): AuthStateHolder
}
