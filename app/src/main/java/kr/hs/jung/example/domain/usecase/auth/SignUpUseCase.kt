package kr.hs.jung.example.domain.usecase.auth

import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): Result<User> {
        return authRepository.signUp(email, password, name)
    }
}
