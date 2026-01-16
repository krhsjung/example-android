package kr.hs.jung.example.domain.model

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val message: String) : ValidationResult()

    val isValid: Boolean
        get() = this is Success

    val errorMessage: String
        get() = when (this) {
            is Success -> ""
            is Failure -> message
        }
}
