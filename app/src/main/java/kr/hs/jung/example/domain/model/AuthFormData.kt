package kr.hs.jung.example.domain.model

data class LogInFormData(
    val email: String,
    val password: String
) {
    fun validateEmail(): ValidationResult = AuthValidator.validateEmail(email)

    fun validatePassword(): ValidationResult = AuthValidator.validatePassword(password)

    fun validateAll(): ValidationResult {
        val validations = listOf(
            validateEmail(),
            validatePassword()
        )

        return validations.firstOrNull { !it.isValid } ?: ValidationResult.Success
    }
}

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

    fun validateAll(): ValidationResult {
        val validations = listOf(
            validateEmail(),
            validatePassword(),
            validateConfirmPassword(),
            validateName()
        )

        return validations.firstOrNull { !it.isValid } ?: ValidationResult.Success
    }
}

object AuthValidator {
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        return when {
            trimmedEmail.isEmpty() -> ValidationResult.Failure("이메일을 입력해주세요")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ->
                ValidationResult.Failure("올바른 이메일 형식이 아닙니다")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Failure("비밀번호를 입력해주세요")
            password.length < 8 -> ValidationResult.Failure("비밀번호는 8자 이상이어야 합니다")
            !password.any { it.isDigit() } -> ValidationResult.Failure("비밀번호에 숫자가 포함되어야 합니다")
            !password.any { it.isLetter() } -> ValidationResult.Failure("비밀번호에 문자가 포함되어야 합니다")
            !password.any { !it.isLetterOrDigit() } -> ValidationResult.Failure("비밀번호에 특수문자가 포함되어야 합니다")
            else -> ValidationResult.Success
        }
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isEmpty() -> ValidationResult.Failure("비밀번호 확인을 입력해주세요")
            password != confirmPassword -> ValidationResult.Failure("비밀번호가 일치하지 않습니다")
            else -> ValidationResult.Success
        }
    }

    fun validateName(name: String): ValidationResult {
        val trimmedName = name.trim()
        return when {
            trimmedName.isEmpty() -> ValidationResult.Failure("이름을 입력해주세요")
            trimmedName.length < 2 -> ValidationResult.Failure("이름은 2자 이상이어야 합니다")
            else -> ValidationResult.Success
        }
    }
}
