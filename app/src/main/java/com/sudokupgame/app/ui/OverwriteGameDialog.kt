package com.sudokupgame.app.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sudokupgame.app.R

/** 진행 중인 게임이 있을 때 다른 퍼즐을 시작하기 전 확인. */
@Composable
fun OverwriteGameDialog(
    savedPuzzleId: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.overwrite_title)) },
        text = { Text(stringResource(R.string.overwrite_message, savedPuzzleId)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.overwrite_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
