package eu.rozmova.app.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R
import eu.rozmova.app.components.weeklyscenarios.LoadingCard
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.domain.LangDto
import eu.rozmova.app.domain.ScenarioDto
import eu.rozmova.app.domain.ScenarioTypeDto
import eu.rozmova.app.domain.toDifficulty

// Data models
data class CategoryItem(
    val type: ScenarioTypeDto,
    val titleResId: Int,
    val icon: ImageVector,
)

val categories =
    listOf(
        CategoryItem(
            type = ScenarioTypeDto.MESSAGES,
            titleResId = R.string.category_message,
            icon = Icons.Default.Chat,
        ),
        CategoryItem(
            type = ScenarioTypeDto.CONVERSATION,
            titleResId = R.string.category_conversation,
            icon = Icons.Default.RecordVoiceOver,
        ),
    )

@Composable
fun CategorySelection(
    scenarios: List<ScenarioDto>,
    onScenarioSelect: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    // State for selected category - shared between components
    var selectedCategory by remember { mutableStateOf(ScenarioTypeDto.MESSAGES) }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.categories),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Categories section
            CategorySection(
                selectedCategoryType = selectedCategory,
                onCategorySelect = { selectedCategory = it },
            )

            // Scenarios grid
            ScenariosGrid(
                selectedCategoryType = selectedCategory,
                allScenarios = scenarios,
                onScenarioSelect = onScenarioSelect,
                isLoading = isLoading,
            )
        }
    }
}

@Composable
fun CategorySection(
    selectedCategoryType: ScenarioTypeDto,
    onCategorySelect: (ScenarioTypeDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Category Chips
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { category ->
                val isSelected = category.type == selectedCategoryType

                // Animate color changes
                val backgroundColor by animateColorAsState(
                    targetValue =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    label = "chipBackgroundColor",
                )

                val textColor by animateColorAsState(
                    targetValue =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    label = "chipTextColor",
                )

                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category.type) },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = textColor,
                            )
                            Text(
                                text = stringResource(category.titleResId),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    },
                    colors =
                        FilterChipDefaults.elevatedFilterChipColors(
                            containerColor = backgroundColor,
                            labelColor = textColor,
                            selectedContainerColor = backgroundColor,
                            selectedLabelColor = textColor,
                        ),
                    elevation =
                        FilterChipDefaults.elevatedFilterChipElevation(
                            elevation = 2.dp,
                            pressedElevation = 4.dp,
                        ),
                )
            }
        }
    }
}

@Composable
fun ScenariosGrid(
    allScenarios: List<ScenarioDto>,
    onScenarioSelect: (ScenarioDto) -> Unit,
    selectedCategoryType: ScenarioTypeDto,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    // Filter scenarios based on selected category
    val scenarios =
        allScenarios.filter { it.scenarioType == selectedCategoryType }

    Column(modifier = modifier.fillMaxHeight()) {
        if (isLoading) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LoadingCard(selectedCategoryType)

                Spacer(modifier = Modifier.weight(1f))
            }
            return@Column
        }
        if (scenarios.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_scenarios),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Column
        }
        scenarios.chunked(2).forEach { rowItems ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowItems.forEach { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onScenarioSelect = onScenarioSelect,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: ScenarioDto,
    onScenarioSelect: (ScenarioDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier =
            modifier
                .fillMaxWidth(),
        elevation =
            CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 8.dp,
            ),
        onClick = { onScenarioSelect(scenario) },
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Category icon and difficulty indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Category icon with background
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    val icon =
                        when (scenario.scenarioType) {
                            ScenarioTypeDto.CONVERSATION -> Icons.Default.RecordVoiceOver
                            ScenarioTypeDto.MESSAGES -> Icons.Default.Chat
                        }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Difficulty Label
                Surface(
                    color =
                        scenario.difficulty
                            .toDifficulty()
                            .color
                            .copy(alpha = 0.12f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = stringResource(scenario.difficulty.toDifficulty().labelId),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = scenario.difficulty.toDifficulty().color,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Title
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // Description
            Text(
                text = scenario.situation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun CategorySelectionLoadingPreview() {
    MaterialTheme {
        CategorySelection(
            scenarios = emptyList(),
            onScenarioSelect = {},
            isLoading = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategorySelectionContentPreview() {
    MaterialTheme {
        CategorySelection(
            scenarios =
                listOf(
                    ScenarioDto(
                        id = "1",
                        title = "Sample Conversation",
                        situation = "A casual conversation with a friend about weekend plans",
                        difficulty = DifficultyDto.EASY,
                        scenarioType = ScenarioTypeDto.CONVERSATION,
                        createdAt = "",
                        userLang = LangDto.EN,
                        scenarioLang = LangDto.EN,
                        labels = emptyList(),
                        helperWords = emptyList(),
                        userInstructions = emptyList(),
                    ),
                    ScenarioDto(
                        id = "2",
                        title = "Sample Messages",
                        situation = "Texting with a colleague about a project",
                        difficulty = DifficultyDto.MEDIUM,
                        scenarioType = ScenarioTypeDto.MESSAGES,
                        createdAt = "",
                        userLang = LangDto.EN,
                        scenarioLang = LangDto.EN,
                        labels = emptyList(),
                        helperWords = emptyList(),
                        userInstructions = emptyList(),
                    ),
                ),
            onScenarioSelect = {},
            isLoading = true,
        )
    }
}
