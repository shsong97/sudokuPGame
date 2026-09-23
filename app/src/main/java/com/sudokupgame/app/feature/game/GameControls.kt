package com.sudokupgame.app.feature.game

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sudokupgame.app.R
import com.sudokupgame.app.data.GameMode
import com.sudokupgame.app.ui.spokenName
import com.sudokupgame.app.ui.symbol

@Composable
fun GameToolbar(
    canUndo: Boolean,
    notesMode: Boolean,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        ToolButton(R.drawable.ic_undo, stringResource(R.string.game_undo), onUndo, enabled = canUndo)
        ToolButton(R.drawable.ic_erase, stringResource(R.string.game_erase), onErase)
        ToolButton(
            icon = R.drawable.ic_edit,
            label = stringResource(if (notesMode) R.string.game_notes_on else R.string.game_notes),
            onClick = onToggleNotes,
            active = notesMode,
        )
        ToolButton(R.drawable.ic_hint, stringResource(R.string.game_hint), onHint)
    }
}

@Composable
private fun ToolButton(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    active: Boolean = false,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val iconContent = @Composable { Icon(painterResource(icon), contentDescription = label) }
        if (active) {
            FilledTonalIconButton(onClick = onClick, enabled = enabled, content = iconContent)
        } else {
            IconButton(
                onClick = onClick,
                enabled = enabled,
                colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                content = iconContent,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

/** 숫자 패드. [columns]가 9면 한 줄, 3이면 3×3 (가로 모드). */
@Composable
fun NumberPad(
    game: GameState,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 9,
) {
    Column(modifier.fillMaxWidth()) {
        (1..9).chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { digit -> DigitButton(game, digit, onDigit, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DigitButton(game: GameState, digit: Int, onDigit: (Int) -> Unit, modifier: Modifier) {
    val remaining = game.remainingCount(digit)
    val description = when (game.mode) {
        GameMode.NUMBER -> stringResource(R.string.game_digit_description, digit, remaining)
        GameMode.ANIMAL -> stringResource(R.string.game_animal_description, game.mode.spokenName(digit), remaining)
    }
    // 숫자는 이미 크므로 시스템 글꼴 크기에 따라 더 커지지 않게 dp 기준으로 고정한다.
    val digitSize = with(LocalDensity.current) { 28.dp.toSp() }
    TextButton(
        onClick = { onDigit(digit) },
        enabled = remaining > 0,
        shape = MaterialTheme.shapes.small,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .heightIn(min = 64.dp)
            .semantics { contentDescription = description },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clearAndSetSemantics {},
        ) {
            Text(
                text = game.mode.symbol(digit),
                fontSize = digitSize,
                fontWeight = FontWeight.Medium,
                // 이모지는 색이 바뀌지 않으므로 다 쓴 동물은 흐리게.
                modifier = Modifier.alpha(if (remaining == 0) 0.3f else 1f),
                color = when {
                    remaining == 0 -> MaterialTheme.colorScheme.outlineVariant
                    game.notesMode -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = if (remaining > 0) remaining.toString() else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
