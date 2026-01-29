package kr.hs.jung.example.domain.repository

import kr.hs.jung.example.data.remote.dto.ExchangeRequestDto
import kr.hs.jung.example.data.remote.dto.LoginRequestDto
import kr.hs.jung.example.data.remote.dto.SignUpRequestDto
import kr.hs.jung.example.domain.model.User

/**
 * 인증 레포지토리 인터페이스
 *
 * 인증 관련 데이터 작업을 추상화합니다.
 * 구현체는 Data 레이어의 AuthRepositoryImpl입니다.
 */
interface AuthRepository {
    /** 이메일/비밀번호 로그인 */
    suspend fun login(request: LoginRequestDto): Result<User>

    /** 이메일 회원가입 */
    suspend fun signUp(request: SignUpRequestDto): Result<User>

    /** 로그아웃 */
    suspend fun logout(): Result<Unit>

    /** 현재 로그인된 사용자 정보 조회 */
    suspend fun me(): Result<User>

    /** OAuth 인증 코드를 사용자 정보로 교환 */
    suspend fun exchangeOAuthCode(request: ExchangeRequestDto): Result<User>

    /** 로컬 세션 정보 삭제 (쿠키, 캐시 등) */
    suspend fun clearSession()

    /**
     * 캐시된 사용자 정보를 가져옵니다.
     * 캐시가 없거나 만료된 경우 null을 반환합니다.
     *
     * @return 캐시된 사용자 정보 또는 null
     */
    suspend fun getCachedUser(): User?

    /**
     * 캐시된 사용자 정보를 가져오거나, 캐시 미스 시 서버에서 조회합니다.
     *
     * @param forceRefresh true면 캐시를 무시하고 서버에서 조회
     * @return 사용자 정보 Result
     */
    suspend fun getUser(forceRefresh: Boolean = false): Result<User>
}
