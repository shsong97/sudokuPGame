package com.sudokupgame.app.feature.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sudokupgame.app.R
import com.sudokupgame.engine.Grid

@Composable
fun SudokuBoard(
    game: GameState,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    highlightSameDigit: Boolean = true,
) {
    val colors = MaterialTheme.colorScheme
    val selected = game.selected
    val selectedValue = selected?.let { game.cells[it].value }?.takeIf { it != 0 && highlightSameDigit }
    val hint = game.hint
    val hintFocus = (hint as? Hint.Placement)?.focusCells.orEmpty()
    val thickLine = colors.onSurface
    val thinLine = colors.outline.copy(alpha = 0.5f)

    BoxWithConstraints(modifier.aspectRatio(1f).background(colors.surface)) {
        val cellSize = maxWidth / Grid.SIZE
        Column(Modifier.fillMaxSize()) {
            for (row in 0 until Grid.SIZE) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    for (col in 0 until Grid.SIZE) {
                        val index = Grid.index(row, col)
                        val cell = game.cells[index]
                        val background = when {
                            hint?.target == index -> colors.secondaryContainer
                            index in hintFocus -> colors.secondaryContainer.copy(alpha = 0.45f)
                            index == selected -> colors.primary.copy(alpha = 0.28f)
                            cell.isError -> colors.error.copy(alpha = 0.14f)
                            selectedValue != null && cell.value == selectedValue -> colors.primary.copy(alpha = 0.16f)
                            selected != null && isRelated(index, selected) -> colors.surfaceVariant
                            else -> Color.Transparent
                        }
                        SudokuCell(
                            cell = cell,
                            row = row,
                            col = col,
                            cellSize = cellSize,
                            highlightNote = selectedValue,
                            isSelected = index == selected,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(background)
                                .clickable { onCellClick(index) },
                        )
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            val step = size.width / Grid.SIZE
            for (k in 0..Grid.SIZE) {
                val thick = k % Grid.BOX == 0
                val color = if (thick) thickLine else thinLine
                val width = if (thick) 2.dp.toPx() else 1.dp.toPx()
                val pos = (k * step).coerceIn(width / 2, size.width - width / 2)
                drawLine(color, Offset(pos, 0f), Offset(pos, size.height), width)
                drawLine(color, Offset(0f, pos), Offset(size.width, pos), width)
            }
        }
    }
}

@Composable
private fun SudokuCell(
    cell: CellState,
    row: Int,
    col: Int,
    cellSize: Dp,
    highlightNote: Int?,
    isSelected: Boolean,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val stateText = when {
        cell.isEmpty -> stringResource(R.string.game_cell_empty)
        cell.isError -> "${cell.value} ${stringResource(R.string.game_cell_error)}"
        else -> cell.value.toString()
    }
    val description = stringResource(R.string.game_cell_description, row + 1, col + 1, stateText)

    Box(
        modifier = modifier.semantics {
            contentDescription = description
            selected = isSelected
        },
        contentAlignment = Alignment.Center,
    ) {
        if (!cell.isEmpty) {
            Text(
                text = cell.value.toString(),
                fontSize = with(density) { (cellSize * 0.6f).toSp() },
                fontWeight = if (cell.isGiven) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    cell.isError -> colors.error
                    cell.isGiven -> colors.onSurface
                    else -> colors.primary
                },
            )
        } else if (cell.notes.isNotEmpty()) {
            val noteSize = with(density) { (cellSize * 0.26f).toSp() }
            Column(Modifier.fillMaxSize()) {
                for (r in 0 until 3) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        for (c in 0 until 3) {
                            val digit = r * 3 + c + 1
                            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                                if (digit in cell.notes) {
                                    Text(
                                        text = digit.toString(),
                                        fontSize = noteSize,
                                        lineHeight = noteSize,
                                        fontWeight = if (digit == highlightNote) FontWeight.Bold else FontWeight.Normal,
                                        color = if (digit == highlightNote) colors.primary else colors.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isRelated(a: Int, b: Int): Boolean =
    Grid.row(a) == Grid.row(b) || Grid.col(a) == Grid.col(b) || Grid.box(a) == Grid.box(b)
