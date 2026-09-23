package com.sudokupgame.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode

/** 숫자 / 동물 모드 선택. */
@Composable
fun GameModeSelector(
    selected: GameMode,
    onSelect: (GameMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf(
        GameMode.NUMBER to R.string.mode_number_sample,
        GameMode.ANIMAL to R.string.mode_animal_sample,
    )
    SingleChoiceSegmentedButtonRow(modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (mode, sample) ->
            SegmentedButton(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text("${mode.label()} ${stringResource(sample)}")
            }
        }
    }
}
