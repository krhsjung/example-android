package kr.hs.jung.example

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Hilt를 사용하는 Instrumented 테스트용 커스텀 테스트 러너
 *
 * AndroidManifest에서 testInstrumentationRunner로 등록하여 사용합니다.
 * HiltTestApplication을 사용하여 테스트 시 Hilt DI를 지원합니다.
 *
 * 설정:
 * ```
 * android {
 *     defaultConfig {
 *         testInstrumentationRunner = "kr.hs.jung.example.HiltTestRunner"
 *     }
 * }
 * ```
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
