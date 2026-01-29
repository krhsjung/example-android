package kr.hs.jung.example.ui.feature.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kr.hs.jung.example.R
import kr.hs.jung.example.data.remote.OAuthHelper
import kr.hs.jung.example.domain.model.AppError
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.model.appError
import kr.hs.jung.example.domain.usecase.auth.ExchangeOAuthCodeUseCase
import kr.hs.jung.example.ui.feature.auth.login.LogInScreen
import kr.hs.jung.example.ui.feature.auth.signup.SignUpScreen
import kr.hs.jung.example.ui.common.ErrorMessageResolver
import kr.hs.jung.example.ui.feature.main.MainActivity
import kr.hs.jung.example.ui.navigation.AuthRoute
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import kr.hs.jung.example.util.logger.AppLogger
import kr.hs.jung.example.util.deeplink.DeepLinkHandler
import kr.hs.jung.example.util.deeplink.DeepLinkResult
import javax.inject.Inject

/**
 * 인증 관련 화면을 담당하는 Activity
 *
 * 주요 역할:
 * - 로그인/회원가입 화면 네비게이션 관리
 * - OAuth 인증 플로우 처리 (딥링크 콜백)
 * - 인증 성공 시 MainActivity로 이동
 * - Activity 레벨 에러 표시 (OAuth 에러 등)
 */
@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    /** OAuth 코드 교환을 위한 UseCase */
    @Inject
    lateinit var exchangeOAuthCodeUseCase: ExchangeOAuthCodeUseCase

    /** Activity 레벨 에러 이벤트 */
    private val _errorEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val errorEvent = _errorEvent.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 딥링크로 시작된 경우 처리
        if (handleDeepLink(intent)) {
            return
        }

        setContent {
            ExampleAndroidTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                // Activity 레벨 에러 이벤트 수신
                LaunchedEffect(Unit) {
                    errorEvent.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(snackbarData = data)
                        }
                    }
                ) { paddingValues ->
                    // Predictive Back 지원을 위한 트랜지션 애니메이션
                    val animationDuration = 300
                    NavHost(
                        navController = navController,
                        startDestination = AuthRoute.LogIn,
                        modifier = Modifier.padding(paddingValues),
                        enterTransition = {
                            fadeIn(animationSpec = tween(animationDuration)) +
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(animationDuration)
                                )
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(animationDuration)) +
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                                    animationSpec = tween(animationDuration)
                                )
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(animationDuration)) +
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(animationDuration)
                                )
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(animationDuration)) +
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                                    animationSpec = tween(animationDuration)
                                )
                        }
                    ) {
                        composable<AuthRoute.LogIn> {
                            LogInScreen(
                                onNavigateToSignUp = {
                                    navController.navigate(AuthRoute.SignUp)
                                },
                                onLogInSuccess = {
                                    navigateToMain()
                                },
                                onOAuthRequest = { provider ->
                                    launchOAuth(provider)
                                }
                            )
                        }

                        composable<AuthRoute.SignUp> {
                            SignUpScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onSignUpSuccess = {
                                    navigateToMain()
                                },
                                onOAuthRequest = { provider ->
                                    launchOAuth(provider)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 딥링크로 Activity가 재시작될 때 호출
     *
     * OAuth 콜백 처리를 위해 새 Intent를 처리
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * Deep Link 처리
     *
     * DeepLinkHandler를 통해 Intent를 파싱하고 적절한 처리를 수행합니다.
     *
     * @param intent 처리할 Intent
     * @return Deep Link가 처리되었으면 true, 아니면 false
     */
    private fun handleDeepLink(intent: Intent?): Boolean {
        return when (val result = DeepLinkHandler.handle(intent)) {
            is DeepLinkResult.OAuthCallback -> {
                AppLogger.d("AuthActivity", "OAuth callback received with code")
                exchangeOAuthCode(result.code)
                true
            }
            is DeepLinkResult.OAuthError -> {
                AppLogger.e("AuthActivity", "OAuth callback failed: ${result.error}")
                showError(getString(R.string.error_oauth_callback_failed))
                false
            }
            is DeepLinkResult.Unknown -> {
                AppLogger.w("AuthActivity", "Unknown deep link: ${result.uri}")
                false
            }
            DeepLinkResult.NotDeepLink -> false
        }
    }

    /**
     * OAuth 인증 시작
     *
     * Chrome Custom Tab을 통해 OAuth 로그인 페이지로 이동
     */
    private fun launchOAuth(provider: SnsProvider) {
        OAuthHelper.launchOAuth(this, provider)
    }

    /**
     * OAuth 코드 교환
     *
     * 받은 인증 코드를 서버에 전송하여 토큰 교환
     */
    private fun exchangeOAuthCode(code: String) {
        lifecycleScope.launch {
            exchangeOAuthCodeUseCase(code)
                .onSuccess {
                    navigateToMain()
                }
                .onFailure { error ->
                    AppLogger.e("AuthActivity", "OAuth code exchange failed: ${error.message}")
                    val appError = error.appError()
                    showError(ErrorMessageResolver.getMessage(this@AuthActivity, appError))
                }
        }
    }

    /**
     * MainActivity로 이동
     *
     * 인증 성공 후 호출되어 메인 화면으로 전환
     * 기존 Activity 스택을 모두 제거
     */
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * 에러 메시지 표시
     *
     * Snackbar를 통해 사용자에게 에러 메시지를 표시합니다.
     *
     * @param message 표시할 에러 메시지
     */
    private fun showError(message: String) {
        _errorEvent.tryEmit(message)
    }
}
