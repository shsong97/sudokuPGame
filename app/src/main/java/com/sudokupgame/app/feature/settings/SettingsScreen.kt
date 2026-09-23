package com.sudokupgame.app.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.data.Settings
import com.sudokupgame.app.data.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.navigate_back))
                    }
                },
            )
        },
    ) { padding ->
        val current = settings ?: return@Scaffold
        SettingsContent(
            settings = current,
            onUpdate = viewModel::update,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun SettingsContent(
    settings: Settings,
    onUpdate: ((Settings) -> Settings) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SectionHeader(stringResource(R.string.settings_section_game))
        SwitchRow(
            title = stringResource(R.string.settings_highlight_same_digit),
            description = stringResource(R.string.settings_highlight_same_digit_desc),
            checked = settings.highlightSameDigit,
            onCheckedChange = { v -> onUpdate { it.copy(highlightSameDigit = v) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_auto_remove_notes),
            description = stringResource(R.string.settings_auto_remove_notes_desc),
            checked = settings.autoRemoveNotes,
            onCheckedChange = { v -> onUpdate { it.copy(autoRemoveNotes = v) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_show_timer),
            description = null,
            checked = settings.showTimer,
            onCheckedChange = { v -> onUpdate { it.copy(showTimer = v) } },
        )
        SwitchRow(
            title = stringResource(R.string.settings_vibration),
            description = stringResource(R.string.settings_vibration_desc),
            checked = settings.vibration,
            onCheckedChange = { v -> onUpdate { it.copy(vibration = v) } },
        )

        SectionHeader(stringResource(R.string.settings_section_display))
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        val options = listOf(
            ThemeMode.SYSTEM to R.string.settings_theme_system,
            ThemeMode.LIGHT to R.string.settings_theme_light,
            ThemeMode.DARK to R.string.settings_theme_dark,
        )
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            options.forEachIndexed { index, (mode, label) ->
                SegmentedButton(
                    selected = settings.themeMode == mode,
                    onClick = { onUpdate { it.copy(themeMode = mode) } },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                ) {
                    Text(stringResource(label))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        modifier = Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}
