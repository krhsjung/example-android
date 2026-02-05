package kr.hs.jung.example.domain.model

/**
 * 로그인 폼 데이터
 */
data class LogInFormData(
    val email: String,
    val password: String
) {
    fun validateEmail(): ValidationResult = AuthValidator.validateEmail(email)

    fun validatePassword(): ValidationResult = AuthValidator.validatePassword(password)

    fun validateAll(): ValidationResult = validateAll(
        ::validateEmail,
        ::validatePassword
    )
}

/**
 * 회원가입 폼 데이터
 */
data class SignUpFormData(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val name: String,
    val isAgreeToTerms: Boolean
) {
    fun validateEmail(): ValidationResult = AuthValidator.validateEmail(email)

    fun validatePassword(): ValidationResult = AuthValidator.validatePassword(password)

    fun validateConfirmPassword(): ValidationResult =
        AuthValidator.validatePasswordConfirmation(password, confirmPassword)

    fun validateName(): ValidationResult = AuthValidator.validateName(name)

    fun validateAll(): ValidationResult = validateAll(
        ::validateEmail,
        ::validatePassword,
        ::validateConfirmPassword,
        ::validateName
    )
}

/**
 * 인증 관련 검증 로직
 *
 * Domain 레이어에 위치하며 플랫폼 독립적입니다.
 * 검증 실패 시 AppError.Validation 타입을 반환합니다.
 */
object AuthValidator {
    private const val PASSWORD_MIN_LENGTH = 8
    private const val NAME_MIN_LENGTH = 2
    private const val SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~"

    // Android-independent email regex pattern
    private val EMAIL_PATTERN = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        return when {
            trimmedEmail.isEmpty() -> ValidationResult.Failure(AppError.Validation.EmptyEmail)
            !EMAIL_PATTERN.matches(trimmedEmail) -> ValidationResult.Failure(AppError.Validation.InvalidEmail)
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Failure(AppError.Validation.EmptyPassword)
            password.length < PASSWORD_MIN_LENGTH -> ValidationResult.Failure(AppError.Validation.WeakPassword)
            !password.any { it.isDigit() } -> ValidationResult.Failure(AppError.Validation.PasswordNoNumber)
            !password.any { it.isUpperCase() } -> ValidationResult.Failure(AppError.Validation.PasswordNoUppercase)
            !password.any { it.isLowerCase() } -> ValidationResult.Failure(AppError.Validation.PasswordNoLowercase)
            !password.any { it in SPECIAL_CHARACTERS } -> ValidationResult.Failure(AppError.Validation.PasswordNoSpecialCharacter)
            else -> ValidationResult.Success
        }
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Failure(AppError.Validation.EmptyPassword)
            password != confirmPassword -> ValidationResult.Failure(AppError.Validation.PasswordMismatch)
            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String): ValidationResult {
        val trimmedName = name.trim()
        return when {
            trimmedName.isEmpty() -> ValidationResult.Failure(AppError.Validation.EmptyUsername)
            trimmedName.length < NAME_MIN_LENGTH -> ValidationResult.Failure(AppError.Validation.NameTooShort)
            else -> ValidationResult.Success
        }
    }
}
