package kr.hs.jung.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

/**
 * 메인 화면 탭 네비게이션 경로
 *
 * Type-Safe Navigation을 사용하여 컴파일 타임에 경로 검증
 */
sealed interface MainRoute {
    /** 첫 번째 탭 */
    @Serializable
    data object First : MainRoute

    /** 두 번째 탭 */
    @Serializable
    data object Second : MainRoute
}

/**
 * 메인 화면 하단 탭 정의
 *
 * @property route 탭의 네비게이션 경로 (KClass)
 * @property icon 탭 아이콘
 * @property label 탭 라벨
 */
enum class MainTab(
    val route: KClass<out MainRoute>,
    val icon: ImageVector,
    val label: String
) {
    First(
        route = MainRoute.First::class,
        icon = Icons.Default.House,
        label = "First"
    ),
    Second(
        route = MainRoute.Second::class,
        icon = Icons.Default.Person,
        label = "Second"
    );

    companion object {
        /**
         * KClass로 해당 탭 찾기
         */
        fun fromRoute(route: KClass<*>?): MainTab? {
            return entries.find { it.route == route }
        }
    }
}
