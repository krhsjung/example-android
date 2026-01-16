package kr.hs.jung.example.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.hs.jung.example.data.remote.OAuthHelper
import kr.hs.jung.example.domain.model.SnsProvider
import kr.hs.jung.example.domain.usecase.auth.ExchangeOAuthCodeUseCase
import kr.hs.jung.example.ui.auth.login.LogInScreen
import kr.hs.jung.example.ui.auth.signup.SignUpScreen
import kr.hs.jung.example.ui.main.MainActivity
import kr.hs.jung.example.ui.navigation.Route
import kr.hs.jung.example.ui.theme.ExampleAndroidTheme
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AuthActivity"
    }

    @Inject
    lateinit var exchangeOAuthCodeUseCase: ExchangeOAuthCodeUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 딥링크로 시작된 경우 처리
        handleIntent(intent)

        setContent {
            ExampleAndroidTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = Route.LogIn.route
                ) {
                    composable(Route.LogIn.route) {
                        LogInScreen(
                            onNavigateToSignUp = {
                                Log.d(TAG, "To sign up")
                                navController.navigate(Route.SignUp.route)
                            },
                            onLogInSuccess = {
                                navigateToMain()
                            },
                            onOAuthRequest = { provider ->
                                launchOAuth(provider)
                            }
                        )
                    }

                    composable(Route.SignUp.route) {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            Log.d(TAG, "OAuth callback received: $uri")
            val code = OAuthHelper.parseOAuthCallback(uri)
            if (code != null) {
                handleOAuthCallback(code)
            }
        }
    }

    private fun launchOAuth(provider: SnsProvider) {
        OAuthHelper.launchOAuth(this, provider)
    }

    private fun handleOAuthCallback(code: String) {
        lifecycleScope.launch {
            val result = exchangeOAuthCodeUseCase(code)
            result.onSuccess {
                navigateToMain()
            }.onFailure { error ->
                Log.e(TAG, "OAuth failed: ${error.message}")
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
