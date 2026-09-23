package com.sudokupgame.app.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sudokupgame.app.R
import com.sudokupgame.app.ui.formatTime
import com.sudokupgame.app.ui.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.navigate_back))
                    }
                },
            )
        },
    ) { padding ->
        val list = stats ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            list.forEach { DifficultyCard(it, Modifier.widthIn(max = 560.dp)) }
        }
    }
}

@Composable
private fun DifficultyCard(stats: DifficultyStats, modifier: Modifier = Modifier) {
    val none = stringResource(R.string.stats_none)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    stats.difficulty.label(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    stringResource(R.string.stats_cleared_value, stats.clearedPuzzles, stats.totalPuzzles),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (stats.totalPuzzles == 0) 0f else stats.clearedPuzzles.toFloat() / stats.totalPuzzles },
                trackColor = MaterialTheme.colorScheme.outlineVariant,
                drawStopIndicator = {},
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Row {
                StatValue(stringResource(R.string.stats_played), stats.played.toString(), Modifier.weight(1f))
                StatValue(
                    stringResource(R.string.stats_win_rate),
                    stats.winRatePercent?.let { stringResource(R.string.stats_percent, it) } ?: none,
                    Modifier.weight(1f),
                )
                StatValue(stringResource(R.string.stats_hints), stats.hintsUsed.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row {
                StatValue(
                    stringResource(R.string.stats_best_time),
                    stats.bestTimeSeconds?.let(::formatTime) ?: none,
                    Modifier.weight(1f),
                )
                StatValue(
                    stringResource(R.string.stats_average_time),
                    stats.averageTimeSeconds?.let(::formatTime) ?: none,
                    Modifier.weight(1f),
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.semantics(mergeDescendants = false) { contentDescription = "$label $value" }) {
        Column(Modifier.clearAndSetSemantics {}) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"))
        }
    }
}
