package kr.hs.jung.example.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule

/**
 * ViewModel 테스트를 위한 기본 클래스
 *
 * 공통 설정:
 * - MainDispatcherRule 자동 적용
 * - TestScope 제공
 * - TestDispatcher 제공 (ViewModel 생성 시 ioDispatcher로 주입)
 * - 테스트 유틸리티 메서드 제공
 *
 * 사용법:
 * ```
 * class MyViewModelTest : BaseViewModelTest() {
 *     private lateinit var viewModel: MyViewModel
 *
 *     override fun setup() {
 *         super.setup()
 *         viewModel = MyViewModel(mockUseCase, testDispatcher)
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val testScope: TestScope
        get() = TestScope(mainDispatcherRule.testDispatcher)

    /**
     * ViewModel 생성 시 ioDispatcher로 주입할 TestDispatcher
     */
    protected val testDispatcher: CoroutineDispatcher
        get() = mainDispatcherRule.testDispatcher

    @Before
    open fun setup() {
        // 서브클래스에서 오버라이드하여 추가 설정
    }

    /**
     * 모든 대기 중인 코루틴을 완료합니다.
     */
    protected fun advanceUntilIdle() {
        testScope.advanceUntilIdle()
    }
}
