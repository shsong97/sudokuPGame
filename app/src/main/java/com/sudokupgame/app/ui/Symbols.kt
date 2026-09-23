package com.sudokupgame.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode

private val ANIMAL_EMOJI = listOf("🐶", "🐱", "🦁", "🐵", "🐰", "🐻", "🐼", "🐘", "🐧")

private val ANIMAL_NAMES = listOf(
    R.string.animal_1, R.string.animal_2, R.string.animal_3,
    R.string.animal_4, R.string.animal_5, R.string.animal_6,
    R.string.animal_7, R.string.animal_8, R.string.animal_9,
)

/** 칸·숫자 패드에 그릴 기호. 동물 모드에서는 이모지. */
fun GameMode.symbol(digit: Int): String = when (this) {
    GameMode.NUMBER -> digit.toString()
    GameMode.ANIMAL -> ANIMAL_EMOJI[digit - 1]
}

/** 화면 낭독기가 읽을 이름. 동물 모드에서는 "사자" 같은 동물 이름. */
@Composable
fun GameMode.spokenName(digit: Int): String = when (this) {
    GameMode.NUMBER -> digit.toString()
    GameMode.ANIMAL -> stringResource(ANIMAL_NAMES[digit - 1])
}

@Composable
fun GameMode.label(): String = stringResource(
    when (this) {
        GameMode.NUMBER -> R.string.mode_number
        GameMode.ANIMAL -> R.string.mode_animal
    },
)
