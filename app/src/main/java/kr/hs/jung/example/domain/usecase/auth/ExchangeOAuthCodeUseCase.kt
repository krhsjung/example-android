package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import kr.hs.jung.example.domain.state.AuthStateHolder
import javax.inject.Inject

/**
 * OAuth 코드 교환 UseCase
 *
 * OAuth 인증 코드를 사용자 정보로 교환:
 * - 요청 DTO 생성
 * - OAuth 코드 교환 API 호출
 * - 성공 시 사용자 정보 저장
 */
class ExchangeOAuthCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    authStateHolder: AuthStateHolder
) : BaseAuthUseCase(authStateHolder) {

    suspend operator fun invoke(code: String): Result<User> {
        val request = ExchangeRequestDto(code = code)

        return authRepository.exchangeOAuthCode(request).withAuthUpdate()
    }
}
