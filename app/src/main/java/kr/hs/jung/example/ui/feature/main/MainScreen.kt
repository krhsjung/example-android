package kr.hs.jung.example.ui.feature.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.hs.jung.example.R
import kr.hs.jung.example.ui.common.collectAsEvent
import kr.hs.jung.example.ui.component.button.ExampleButton
import kr.hs.jung.example.ui.navigation.MainRoute
import kr.hs.jung.example.ui.navigation.MainTab

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Event 기반 일회성 이벤트 처리
    viewModel.event.collectAsEvent { event ->
        when (event) {
            is MainEvent.LogoutSuccess -> onLogout()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hasRoute(tab.route) == true,
                        onClick = {
                            navController.navigate(
                                when (tab) {
                                    MainTab.First -> MainRoute.First
                                    MainTab.Second -> MainRoute.Second
                                }
                            ) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Predictive Back 지원을 위한 트랜지션 애니메이션
        val animationDuration = 300
        NavHost(
            navController = navController,
            startDestination = MainRoute.First,
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
            composable<MainRoute.First> {
                FirstTab(onLogoutClick = { viewModel.logout() })
            }
            composable<MainRoute.Second> {
                SecondTab()
            }
        }
    }
}

@Composable
private fun FirstTab(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "First Tab",
            style = MaterialTheme.typography.headlineMedium
        )

        ExampleButton(
            title = stringResource(R.string.logout),
            onClick = onLogoutClick
        )
    }
}

@Composable
private fun SecondTab() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Second Tab",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
