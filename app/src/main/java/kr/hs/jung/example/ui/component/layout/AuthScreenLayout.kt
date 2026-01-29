package kr.hs.jung.example.ui.component.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kr.hs.jung.example.ui.theme.Dimensions

/**
 * 인증 화면 공통 레이아웃
 *
 * 로그인/회원가입 화면에서 사용하는 공통 레이아웃 구조를 제공합니다.
 * Header, Content, Footer 영역으로 구성됩니다.
 *
 * @param header 상단 헤더 영역 (앱 이름, 타이틀 등)
 * @param footer 하단 푸터 영역 (네비게이션 링크 등)
 * @param content 중앙 컨텐츠 영역 (폼, 버튼 등)
 */
@Composable
fun AuthScreenLayout(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimensions.Screen.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SpacingXXLarge, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimensions.Screen.FooterHeight),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            footer()
        }
    }
}
