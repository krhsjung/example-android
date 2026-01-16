package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

class MeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.me()
    }
}
