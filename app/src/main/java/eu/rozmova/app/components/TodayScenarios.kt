package eu.rozmova.app.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.TodayScenarioSelectionModel
import eu.rozmova.app.utils.ViewState

data class Scenario(
    val title: String,
    val description: String,
    val difficulty: Difficulty,
)

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
    state: ViewState<TodayScenarioSelectionModel>,
    onScenarioClick: (ScenarioModel) -> Unit,
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
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    text = "Today's scenario selection",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Handle different states
            when (state) {
                is ViewState.Loading -> {
                    LoadingScenarios()
                }
                is ViewState.Success -> {
                    ScenariosListCard(
                        onScenarioClick = onScenarioClick,
                        easyScenario = state.data.easyScenario,
                        mediumScenario = state.data.mediumScenario,
                        hardScenario = state.data.hardScenario,
                    )
                }
                is ViewState.Error -> {
                    ErrorState()
                }
            }
        }
    }
}

@Composable
private fun LoadingScenarios(modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading scenarios...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorState(modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No scenarios available today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScenariosListCard(
    easyScenario: ScenarioModel,
    mediumScenario: ScenarioModel,
    hardScenario: ScenarioModel,
    onScenarioClick: (ScenarioModel) -> Unit,
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
    scenario: ScenarioModel,
    level: Difficulty,
    onClick: (scenario: ScenarioModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = { onClick(scenario) },
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Title
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Difficulty Label
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
    val sampleScenarios =
        listOf(
            Scenario(
                title = "Basic UI Layout",
                description = "Learn how to create simple layouts using Jetpack Compose",
                difficulty = Difficulty.BEGINNER,
            ),
            Scenario(
                title = "State Management",
                description = "Practice handling state in a medium-complexity application",
                difficulty = Difficulty.INTERMEDIATE,
            ),
            Scenario(
                title = "Advanced Testing",
                description = "Master complex testing scenarios and best practices",
                difficulty = Difficulty.ADVANCED,
            ),
        )

    MaterialTheme {
        TodaysScenarioSelection(
            state = ViewState.Loading,
            onScenarioClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
