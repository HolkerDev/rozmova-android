package eu.rozmova.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.LangDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.TodayScenarioSelection

enum class Difficulty(
    val color: Color,
    val labelId: Int,
) {
    BEGINNER(Color(0xFF4CAF50), R.string.level_easy),
    INTERMEDIATE(Color(0xFFFFA000), R.string.level_medium),
    ADVANCED(Color(0xFFE91E63), R.string.level_hard),
}

@Composable
fun TodaysScenarioSelection(
    state: TodayScenarioSelection,
    onScenarioClick: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.today_selection_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ScenariosListCard(
                onScenarioClick = onScenarioClick,
                easyScenario = state.easyScenario,
                mediumScenario = state.mediumScenario,
                hardScenario = state.hardScenario,
            )
        }
    }
}

@Composable
private fun ScenariosListCard(
    easyScenario: ScenarioDto,
    mediumScenario: ScenarioDto,
    hardScenario: ScenarioDto,
    onScenarioClick: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ScenarioItem(
                scenario = easyScenario,
                onClick = { onScenarioClick(easyScenario) },
                level = Difficulty.BEGINNER,
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ScenarioItem(
                scenario = mediumScenario,
                onClick = { onScenarioClick(mediumScenario) },
                level = Difficulty.INTERMEDIATE,
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ScenarioItem(
                scenario = hardScenario,
                onClick = { onScenarioClick(hardScenario) },
                level = Difficulty.ADVANCED,
            )
        }
    }
}

@Composable
private fun ScenarioItem(
    scenario: ScenarioDto,
    level: Difficulty,
    onClick: (scenario: ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { onClick(scenario) },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Title - add weight and constrain with overflow
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = true),
                )

                // Add a small spacer
                Spacer(modifier = Modifier.width(8.dp))

                // Difficulty Label - now won't get pushed off screen
                Surface(
                    color = level.color.copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(level.labelId),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = level.color,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = scenario.situation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodaysScenariosPreview() {
    val scenario =
        ScenarioDto(
            id = "",
            createdAt = "",
            userLang = LangDto.EN,
            scenarioLang = LangDto.DE,
            difficulty = DifficultyDto.EASY,
            scenarioType = ScenarioTypeDto.MESSAGES,
            title = "Sample Scenario",
            situation = "Sample situation",
            labels = listOf("test", "test2", "test3"),
            helperWords = listOf(),
            userInstructions = listOf(),
        )
    val sampleScenarios =
        TodayScenarioSelection(
            easyScenario = scenario,
            mediumScenario = scenario,
            hardScenario = scenario,
        )

    MaterialTheme {
        TodaysScenarioSelection(
            state = sampleScenarios,
            onScenarioClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
