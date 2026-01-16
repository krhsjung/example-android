package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

class LogInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}
