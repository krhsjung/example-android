package kr.hs.jung.example.ui.feature.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.hs.jung.example.ui.common.state.AuthManager
import kr.hs.jung.example.domain.usecase.auth.MeUseCase
import kr.hs.jung.example.ui.feature.auth.AuthActivity
import kr.hs.jung.example.ui.feature.main.MainActivity
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import javax.inject.Inject

/**
 * 앱 시작 시 세션 체크를 담당하는 Splash Activity
 *
 * 주요 역할:
 * - 앱 진입점 (LAUNCHER)
 * - 세션 유효성 검사 후 적절한 화면으로 분기
 * - 로딩 중 프로그레스 표시
 *
 * 흐름:
 * - 세션 유효 → MainActivity로 이동
 * - 세션 무효 → AuthActivity로 이동
 *
 * 백스택 관리:
 * - finish()를 호출하여 백스택에서 제거
 * - 백그라운드 → 포그라운드 복귀 시 SplashActivity가 다시 실행되지 않음
 */
@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var meUseCase: MeUseCase

    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExampleAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        checkSessionAndNavigate()
    }

    /**
     * 세션 체크 후 적절한 화면으로 이동
     */
    private fun checkSessionAndNavigate() {
        lifecycleScope.launch {
            meUseCase()
                .onSuccess { user ->
                    authManager.setUser(user)
                    navigateTo(MainActivity::class.java)
                }
                .onFailure {
                    authManager.clear()
                    navigateTo(AuthActivity::class.java)
                }
        }
    }

    private fun navigateTo(activityClass: Class<out ComponentActivity>) {
        startActivity(Intent(this, activityClass))
        finish()
    }
}
