package kr.hs.jung.example.ui.main

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.hs.jung.example.R
import kr.hs.jung.example.ui.component.common.ExampleButton
import kr.hs.jung.example.ui.navigation.MainTab

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val logoutSuccess by viewModel.logoutSuccess.collectAsState()

    LaunchedEffect(logoutSuccess) {
        if (logoutSuccess) {
            viewModel.resetLogoutSuccess()
            onLogout()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                        onClick = {
                            navController.navigate(tab.route) {
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
        NavHost(
            navController = navController,
            startDestination = MainTab.First.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainTab.First.route) {
                FirstTab(onLogoutClick = { viewModel.logout() })
            }
            composable(MainTab.Second.route) {
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
