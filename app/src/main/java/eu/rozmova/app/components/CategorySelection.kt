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
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Email
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
import eu.rozmova.app.domain.ScenarioDifficulty
import eu.rozmova.app.domain.ScenarioModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toDifficulty
import kotlinx.datetime.Clock

// Data models
data class CategoryItem(
    val type: ScenarioType,
    val titleResId: Int,
    val icon: ImageVector,
)

val categories =
    listOf(
        CategoryItem(
            type = ScenarioType.CONVERSATION,
            titleResId = R.string.category_conversation,
            icon = Icons.Default.RecordVoiceOver,
        ),
        CategoryItem(
            type = ScenarioType.MESSAGES,
            titleResId = R.string.category_message,
            icon = Icons.Default.Chat,
        ),
        CategoryItem(
            type = ScenarioType.EMAIL,
            titleResId = R.string.category_email,
            icon = Icons.Default.Email,
        ),
    )

@Composable
fun CategorySelection(
    scenarios: List<ScenarioModel>,
    modifier: Modifier = Modifier,
) {
    // State for selected category - shared between components
    var selectedCategory by remember { mutableStateOf(ScenarioType.CONVERSATION) }

    val gradientBackground = MaterialTheme.colorScheme.background

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(gradientBackground)
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
            )
        }
    }
}

@Composable
fun CategorySection(
    selectedCategoryType: ScenarioType,
    onCategorySelect: (ScenarioType) -> Unit,
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
    allScenarios: List<ScenarioModel>,
    selectedCategoryType: ScenarioType,
    modifier: Modifier = Modifier,
) {
    // Filter scenarios based on selected category
    val scenarios =
        remember(selectedCategoryType) {
            allScenarios.filter { it.type == selectedCategoryType }
        }

    Column(modifier = modifier.fillMaxHeight()) {
        if (scenarios.isEmpty()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No scenarios available for this category yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            // Staggered grid with scenarios
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(
                        scenario = scenario,
                        onScenarioSelect = { },
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioCard(
    scenario: ScenarioModel,
    onScenarioSelect: (ScenarioModel) -> Unit,
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
                        when (scenario.type) {
                            ScenarioType.CONVERSATION -> Icons.Default.RecordVoiceOver
                            ScenarioType.MESSAGES -> Icons.Default.Chat
                            ScenarioType.EMAIL -> Icons.Default.Email
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

@Preview(showBackground = true)
@Composable
private fun CategorySelectionPreview() {
    MaterialTheme {
        CategorySelection(
            scenarios =
                listOf(
                    ScenarioModel(
                        id = "1",
                        createdAt = Clock.System.now(),
                        title = "Scenario 1",
                        labels = emptyList(),
                        languageLevel = "A1",
                        botInstruction = "Bot instruction",
                        situation = "Situation",
                        userInstruction = "User instruction",
                        targetLanguage = "English",
                        userLanguage = "Spanish",
                        type = ScenarioType.CONVERSATION,
                        difficulty = ScenarioDifficulty.EASY,
                    ),
                ),
        )
    }
}
