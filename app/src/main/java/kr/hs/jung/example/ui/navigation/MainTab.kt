package kr.hs.jung.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    First(
        route = "first",
        icon = Icons.Default.House,
        label = "First"
    ),
    Second(
        route = "second",
        icon = Icons.Default.Person,
        label = "Second"
    ),
}
