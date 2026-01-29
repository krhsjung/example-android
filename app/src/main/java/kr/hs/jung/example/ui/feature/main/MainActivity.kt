package kr.hs.jung.example.ui.feature.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import kr.hs.jung.example.util.logger.AppLogger
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import kr.hs.jung.example.ui.common.state.AuthManager
import kr.hs.jung.example.ui.feature.auth.AuthActivity
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import javax.inject.Inject

/**
 * 메인 화면을 담당하는 Activity
 *
 * 주요 역할:
 * - 인증된 사용자의 메인 화면 표시
 * - 로그아웃 시 AuthActivity로 이동
 * - 하단 탭 네비게이션 관리
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** 현재 사용자 상태 관리 */
    @Inject
    lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppLogger.d("MainActivity", "Current user: ${authManager.currentUser}")

        enableEdgeToEdge()

        setContent {
            ExampleAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onLogout = {
                            navigateToAuth()
                        }
                    )
                }
            }
        }
    }

    /**
     * 로그아웃 후 AuthActivity로 이동
     *
     * 기존 Activity 스택을 모두 제거하고 AuthActivity를 시작합니다.
     */
    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
