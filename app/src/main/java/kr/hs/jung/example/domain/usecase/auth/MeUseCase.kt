package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * 현재 사용자 정보 조회 UseCase
 *
 * 서버에서 현재 로그인된 사용자 정보를 조회합니다.
 * 주로 앱 시작 시 세션 유효성 검증에 사용됩니다.
 */
class MeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.me()
    }
}
