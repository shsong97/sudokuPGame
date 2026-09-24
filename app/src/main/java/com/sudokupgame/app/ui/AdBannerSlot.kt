package com.sudokupgame.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sudokupgame.app.BuildConfig
import com.sudokupgame.app.R

/** 하단 배너 광고 높이. 휴대전화의 적응형 배너(약 50~60dp)가 들어갈 수 있게 잡는다. */
val AD_BANNER_HEIGHT = 60.dp

/**
 * 화면 맨 아래(내비게이션 바 위) 배너 광고 자리. Scaffold의 bottomBar로 쓰면 본문이 그만큼 위로 올라간다.
 * 아직 광고 SDK는 없으므로 디버그 빌드에서만 위치 확인용 표시를 그리고, 릴리스에서는 빈 공간이다.
 */
@Composable
fun AdBannerSlot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(AD_BANNER_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        if (BuildConfig.SHOW_AD_PLACEHOLDER) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.ad_placeholder),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
