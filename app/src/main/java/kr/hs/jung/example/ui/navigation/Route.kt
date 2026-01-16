package kr.hs.jung.example.ui.navigation

sealed class Route(val route: String) {
    data object LogIn : Route("login")
    data object SignUp : Route("signup")
    data object Main : Route("main")
}
