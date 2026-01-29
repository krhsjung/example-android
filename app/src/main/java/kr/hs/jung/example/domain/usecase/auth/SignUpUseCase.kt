package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.domain.service.PasswordHasher
import kr.hs.jung.example.domain.state.AuthStateHolder
import javax.inject.Inject

/**
 * 회원가입 UseCase
 *
 * 회원가입 비즈니스 로직을 담당:
 * - 요청 DTO 생성
 * - 비밀번호 해싱
 * - 회원가입 API 호출
 * - 회원가입 성공 시 사용자 정보 저장 (자동 로그인)
 */
class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    authStateHolder: AuthStateHolder,
    private val passwordHasher: PasswordHasher
) : BaseAuthUseCase(authStateHolder) {

    suspend operator fun invoke(email: String, password: String, name: String): Result<User> {
        val request = SignUpRequestDto(
            email = email.trim(),
            password = passwordHasher.hash(password),
            name = name.trim()
        )

        return authRepository.signUp(request).withAuthUpdate()
    }
}
