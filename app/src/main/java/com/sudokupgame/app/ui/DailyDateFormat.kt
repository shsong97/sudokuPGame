package com.sudokupgame.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 데일리 챌린지 날짜 표시 (예: "11월 6일 (금)").
 * 요일은 기기 언어가 아니라 날짜 패턴과 같은 언어(문자열 리소스)를 따른다.
 */
@Composable
fun formatDailyDate(date: LocalDate): String {
    val locale = Locale.forLanguageTag(stringResource(R.string.daily_date_locale))
    return date.format(DateTimeFormatter.ofPattern(stringResource(R.string.daily_date_pattern), locale))
}
