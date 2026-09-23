package com.sudokupgame.app.feature.game

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokupgame.app.R

@Composable
fun GameToolbar(
    canUndo: Boolean,
    notesMode: Boolean,
    onUndo: () -> Unit,
    onErase: () -> Unit,
    onToggleNotes: () -> Unit,
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

@Composable
fun NumberPad(
    game: GameState,
    onDigit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth()) {
        for (digit in 1..9) {
            val remaining = game.remainingCount(digit)
            val description = stringResource(R.string.game_digit_description, digit, remaining)
            TextButton(
                onClick = { onDigit(digit) },
                enabled = remaining > 0,
                shape = MaterialTheme.shapes.small,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .semantics { contentDescription = description },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clearAndSetSemantics {},
                ) {
                    Text(
                        text = digit.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium,
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
    }
}
