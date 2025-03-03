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
import androidx.compose.material.icons.filled.Message
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
import eu.rozmova.app.domain.toDifficulty

// Data models
data class CategoryItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
)

data class ScenarioItem(
    val id: String,
    val title: String,
    val categoryId: String,
    val difficulty: ScenarioDifficulty,
    val description: String,
)

@Composable
fun CategorySelection(modifier: Modifier = Modifier) {
    // State for selected category - shared between components
    var selectedCategoryId by remember { mutableStateOf("conversations") }

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
                selectedCategoryId = selectedCategoryId,
                onCategorySelect = { selectedCategoryId = it },
            )

            // Scenarios grid
            ScenariosGrid(selectedCategoryId = selectedCategoryId)
        }
    }
}

@Composable
fun CategorySection(
    selectedCategoryId: String,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categories =
        listOf(
            CategoryItem(
                id = "conversations",
                title = "Conversations",
                icon = Icons.Default.RecordVoiceOver,
            ),
            CategoryItem(
                id = "messages",
                title = "Messages",
                icon = Icons.Default.Chat,
            ),
            CategoryItem(
                id = "emails",
                title = "Emails",
                icon = Icons.Default.Email,
            ),
        )

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
                val isSelected = category.id == selectedCategoryId

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
                    onClick = { onCategorySelect(category.id) },
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
                                text = category.title,
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
    selectedCategoryId: String,
    modifier: Modifier = Modifier,
) {
    // Sample data
    val allScenarios =
        remember {
            listOf(
                ScenarioItem(
                    id = "1",
                    title = "At the Restaurant",
                    categoryId = "conversations",
                    difficulty = ScenarioDifficulty.EASY,
                    description = "Practice ordering food and making special requests at a restaurant.",
                ),
                ScenarioItem(
                    id = "2",
                    title = "Job Interview",
                    categoryId = "conversations",
                    difficulty = ScenarioDifficulty.HARD,
                    description = "Practice answering common interview questions and discussing your qualifications.",
                ),
                ScenarioItem(
                    id = "3",
                    title = "Vacation Planning",
                    categoryId = "messages",
                    difficulty = ScenarioDifficulty.EASY,
                    description = "Exchange messages about planning a trip with friends.",
                ),
                ScenarioItem(
                    id = "4",
                    title = "Customer Support",
                    categoryId = "emails",
                    difficulty = ScenarioDifficulty.MEDIUM,
                    description = "Write an email to resolve an issue with a product you purchased.",
                ),
                ScenarioItem(
                    id = "5",
                    title = "Making New Friends",
                    categoryId = "conversations",
                    difficulty = ScenarioDifficulty.HARD,
                    description = "Practice introducing yourself and making small talk with new acquaintances.",
                ),
                ScenarioItem(
                    id = "6",
                    title = "Birthday Invitation",
                    categoryId = "messages",
                    difficulty = ScenarioDifficulty.EASY,
                    description = "Send a message inviting a friend to your birthday celebration.",
                ),
            )
        }

    // Filter scenarios based on selected category
    val scenarios =
        remember(selectedCategoryId) {
            if (selectedCategoryId == "all") {
                allScenarios
            } else {
                allScenarios.filter { it.categoryId == selectedCategoryId }
            }
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
    scenario: ScenarioItem,
    onScenarioSelect: (ScenarioItem) -> Unit,
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
                        when (scenario.categoryId) {
                            "conversations" -> Icons.Default.RecordVoiceOver
                            "messages" -> Icons.Default.Chat
                            "emails" -> Icons.Default.Email
                            else -> Icons.Default.Message
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
                text = scenario.description,
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
private fun LanguageLearningScreenPreview() {
    MaterialTheme {
        CategorySelection()
    }
}
