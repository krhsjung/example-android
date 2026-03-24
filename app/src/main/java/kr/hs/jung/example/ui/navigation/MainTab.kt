package kr.hs.jung.example.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kr.hs.jung.example.R
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
 * @property labelResId 탭 라벨 문자열 리소스 ID
 */
enum class MainTab(
    val route: KClass<out MainRoute>,
    val icon: ImageVector,
    @StringRes val labelResId: Int
) {
    First(
        route = MainRoute.First::class,
        icon = Icons.Default.House,
        labelResId = R.string.tab_first
    ),
    Second(
        route = MainRoute.Second::class,
        icon = Icons.Default.Person,
        labelResId = R.string.tab_second
    );
}
