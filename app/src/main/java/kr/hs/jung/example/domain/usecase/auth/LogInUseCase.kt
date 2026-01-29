package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.domain.service.PasswordHasher
import kr.hs.jung.example.domain.state.AuthStateHolder
import javax.inject.Inject

/**
 * 로그인 UseCase
 *
 * 로그인 비즈니스 로직을 담당:
 * - 요청 DTO 생성
 * - 비밀번호 해싱
 * - 로그인 API 호출
 * - 로그인 성공 시 사용자 정보 저장
 */
class LogInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    authStateHolder: AuthStateHolder,
    private val passwordHasher: PasswordHasher
) : BaseAuthUseCase(authStateHolder) {

    suspend operator fun invoke(email: String, password: String): Result<User> {
        val request = LoginRequestDto(
            email = email.trim(),
            password = passwordHasher.hash(password)
        )

        return authRepository.login(request).withAuthUpdate()
    }
}
