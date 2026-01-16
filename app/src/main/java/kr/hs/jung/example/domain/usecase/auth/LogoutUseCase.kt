package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        val result = authRepository.logout()
        // 로그아웃 성공/실패 상관없이 세션 클리어
        authRepository.clearSession()
        return result
    }
}
