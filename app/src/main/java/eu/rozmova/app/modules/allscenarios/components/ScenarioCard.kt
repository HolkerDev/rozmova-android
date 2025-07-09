package eu.rozmova.app.modules.allscenarios.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.LangDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.modules.shared.DifficultyLabel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScenarioCard(
    scenario: ScenarioDto,
    onClick: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPassed = true

    Card(
        onClick = { onClick(scenario) },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .height(72.dp),
        // Fixed height for consistency
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isPassed) {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = scenario.situation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Difficulty badge
            DifficultyLabel(scenario.difficulty)
        }
    }
}

@Preview
@Composable
private fun ScenarioCardPreview() {
    MaterialTheme {
        ScenarioCard(
            scenario =
                ScenarioDto(
                    id = "1",
                    title = "Sample Scenario",
                    situation =
                        "This is a sample situation for the scenario." +
                            " Where the user needs to respond appropriately. The text is very very ver4y long",
                    difficulty = DifficultyDto.EASY,
                    scenarioType = ScenarioTypeDto.CONVERSATION,
                    createdAt = "",
                    userLang = LangDto.EN,
                    scenarioLang = LangDto.DE,
                    labels = listOf(),
                    helperWords = listOf(),
                    userInstructions = listOf(),
                ),
            onClick = {},
        )
    }
}
