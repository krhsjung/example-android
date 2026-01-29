package kr.hs.jung.example.domain.model

/**
 * 검증 결과를 표현하는 sealed class
 *
 * Domain 레이어에 위치하며 플랫폼 독립적입니다.
 * 실패 시 AppError.Validation 타입을 포함합니다.
 */
sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Failure(val error: AppError.Validation) : ValidationResult()

    val isValid: Boolean
        get() = this is Success

    /**
     * 실패 시 AppError를 반환
     */
    fun errorOrNull(): AppError.Validation? = (this as? Failure)?.error
}

/**
 * 여러 검증 결과 중 첫 번째 실패를 반환하거나 모두 성공이면 Success 반환
 *
 * 지연 평가로 성능 최적화 - 첫 실패 발견 시 즉시 반환
 */
fun validateAll(vararg validations: () -> ValidationResult): ValidationResult {
    for (validation in validations) {
        val result = validation()
        if (!result.isValid) return result
    }
    return ValidationResult.Success
}

/**
 * ValidationResult를 Result<Unit>으로 변환
 */
fun ValidationResult.toResult(): Result<Unit> = when (this) {
    is ValidationResult.Success -> Result.success(Unit)
    is ValidationResult.Failure -> Result.failure(AppException(error))
}
