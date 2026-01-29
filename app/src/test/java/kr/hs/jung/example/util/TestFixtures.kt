package kr.hs.jung.example.util

import kr.hs.jung.example.domain.model.User

/**
 * 테스트용 객체 생성 팩토리
 */
object TestFixtures {

    /**
     * 테스트용 User 객체 생성
     */
    fun createUser(
        id: String = "test-user-id",
        name: String = "Test User",
        email: String = "test@test.com"
    ) = User(
        id = id,
        name = name,
        email = email
    )

    /**
     * 유효한 이메일 목록
     */
    val validEmails = listOf(
        "test@test.com",
        "user@example.org",
        "name.surname@domain.co.kr"
    )

    /**
     * 유효하지 않은 이메일 목록
     */
    val invalidEmails = listOf(
        "",
        "invalid",
        "no@domain",
        "@nodomain.com",
        "spaces in@email.com"
    )

    /**
     * 유효한 비밀번호 목록
     */
    val validPasswords = listOf(
        "Test1234!",
        "Password123@",
        "Secure#Pass1"
    )

    /**
     * 유효하지 않은 비밀번호 목록
     */
    val invalidPasswords = listOf(
        "",
        "short",
        "nouppercase1!",
        "NOLOWERCASE1!",
        "NoNumbers!",
        "NoSpecial123"
    )
}
