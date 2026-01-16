package kr.hs.jung.example.data.repository

import android.util.Log
import com.google.gson.Gson
import kr.hs.jung.example.data.remote.NetworkException
import kr.hs.jung.example.data.remote.PersistentCookieJar
import kr.hs.jung.example.data.remote.api.AuthApi
import kr.hs.jung.example.data.remote.dto.ErrorResponseDto
import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.domain.model.User
import kr.hs.jung.example.domain.repository.AuthRepository
import retrofit2.Response
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val cookieJar: PersistentCookieJar,
    private val gson: Gson
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): Result<User> {
        val request = LoginRequestDto(
            email = email.trim(),
            password = hashPassword(password)
        )
        return safeApiCall { authApi.login(request) }.map { it.toDomain() }
    }

    override suspend fun signUp(email: String, password: String, name: String): Result<User> {
        val request = SignUpRequestDto(
            email = email.trim(),
            password = hashPassword(password),
            name = name.trim()
        )
        return safeApiCall { authApi.signUp(request) }.map { it.toDomain() }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCallUnit { authApi.logout() }
    }

    override suspend fun me(): Result<User> {
        return safeApiCall { authApi.me() }.map { it.toDomain() }
    }

    override suspend fun exchangeOAuthCode(code: String): Result<User> {
        val request = ExchangeRequestDto(code = code)
        return safeApiCall { authApi.exchange(request) }.map { it.toDomain() }
    }

    override fun clearSession() {
        cookieJar.clear()
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(NetworkException.NoData)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    errorBody?.let { gson.fromJson(it, ErrorResponseDto::class.java) }
                } catch (e: Exception) {
                    null
                }

                Log.e(TAG, "Server Error: ${errorResponse?.debugDescription}")
                Result.failure(NetworkException.ServerError(response.code(), errorResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network Error: ${e.message}")
            Result.failure(NetworkException.Unknown(e))
        }
    }

    private suspend fun <T> safeApiCallUnit(apiCall: suspend () -> Response<T>): Result<Unit> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = try {
                    errorBody?.let { gson.fromJson(it, ErrorResponseDto::class.java) }
                } catch (e: Exception) {
                    null
                }

                Log.e(TAG, "Server Error: ${errorResponse?.debugDescription}")
                Result.failure(NetworkException.ServerError(response.code(), errorResponse))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network Error: ${e.message}")
            Result.failure(NetworkException.Unknown(e))
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-512")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
