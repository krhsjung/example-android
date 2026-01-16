package kr.hs.jung.example.domain.repository

import kr.hs.jung.example.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String, name: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun me(): Result<User>
    suspend fun exchangeOAuthCode(code: String): Result<User>
    fun clearSession()
}
