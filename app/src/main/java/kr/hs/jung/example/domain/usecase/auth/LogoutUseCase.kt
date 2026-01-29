package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 로그아웃 UseCase
 *
 * 로그아웃 처리를 수행합니다:
 * - 서버에 로그아웃 요청
 * - 로컬 세션 정보 삭제 (성공/실패 무관)
 */
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
