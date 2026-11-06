package com.sudokupgame.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 데일리 챌린지 날짜 표시 (예: "11월 6일 (금)"). */
@Composable
fun formatDailyDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern(stringResource(R.string.daily_date_pattern), Locale.getDefault()))
