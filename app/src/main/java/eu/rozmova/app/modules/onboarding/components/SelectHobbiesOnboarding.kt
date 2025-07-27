package eu.rozmova.app.modules.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Hobby(
    val id: String,
    val name: String,
    val category: HobbyCategory,
)

enum class HobbyCategory(
    val displayName: String,
    val icon: ImageVector,
) {
    SPORTS_FITNESS("Sports & Fitness", Icons.Default.FitnessCenter),
    CREATIVE_ARTS("Creative & Arts", Icons.Default.Palette),
    SOCIAL_CULTURAL("Social & Cultural", Icons.Default.Groups),
    OUTDOOR_NATURE("Outdoor & Nature", Icons.Default.Nature),
    TECHNICAL_INTELLECTUAL("Technical & Intellectual", Icons.Default.Psychology),
}

val allHobbies =
    listOf(
        // Sports & Fitness
        Hobby("football", "Football/Soccer", HobbyCategory.SPORTS_FITNESS),
        Hobby("running", "Running/Jogging", HobbyCategory.SPORTS_FITNESS),
        Hobby("cycling", "Cycling/Biking", HobbyCategory.SPORTS_FITNESS),
        Hobby("swimming", "Swimming", HobbyCategory.SPORTS_FITNESS),
        Hobby("hiking", "Hiking/Trekking", HobbyCategory.SPORTS_FITNESS),
        Hobby("rock_climbing", "Rock climbing", HobbyCategory.SPORTS_FITNESS),
        Hobby("tennis", "Tennis", HobbyCategory.SPORTS_FITNESS),
        Hobby("basketball", "Basketball", HobbyCategory.SPORTS_FITNESS),
        Hobby("volleyball", "Volleyball", HobbyCategory.SPORTS_FITNESS),
        Hobby("martial_arts", "Martial arts", HobbyCategory.SPORTS_FITNESS),
        Hobby("yoga", "Yoga/Pilates", HobbyCategory.SPORTS_FITNESS),
        Hobby("gym", "Gym/Weight training", HobbyCategory.SPORTS_FITNESS),
        Hobby("skiing", "Skiing/Snowboarding", HobbyCategory.SPORTS_FITNESS),
        // Creative & Arts
        Hobby("photography", "Photography", HobbyCategory.CREATIVE_ARTS),
        Hobby("painting", "Painting/Drawing", HobbyCategory.CREATIVE_ARTS),
        Hobby("music", "Music", HobbyCategory.CREATIVE_ARTS),
        Hobby("singing", "Singing", HobbyCategory.CREATIVE_ARTS),
        Hobby("writing", "Writing/Blogging", HobbyCategory.CREATIVE_ARTS),
        Hobby("crafting", "Crafting/DIY projects", HobbyCategory.CREATIVE_ARTS),
        Hobby("pottery", "Pottery/Ceramics", HobbyCategory.CREATIVE_ARTS),
        Hobby("knitting", "Knitting/Sewing", HobbyCategory.CREATIVE_ARTS),
        Hobby("dance", "Dance", HobbyCategory.CREATIVE_ARTS),
        Hobby("theater", "Theater/Acting", HobbyCategory.CREATIVE_ARTS),
        // Social & Cultural
        Hobby("cooking", "Cooking/Baking", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("wine_tasting", "Wine tasting", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("board_games", "Board games", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("video_gaming", "Video gaming", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("reading", "Reading", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("language_learning", "Language learning", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("volunteering", "Volunteering", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("traveling", "Traveling", HobbyCategory.SOCIAL_CULTURAL),
        Hobby("cultural_events", "Cultural events/Museums", HobbyCategory.SOCIAL_CULTURAL),
        // Outdoor & Nature
        Hobby("gardening", "Gardening", HobbyCategory.OUTDOOR_NATURE),
        Hobby("camping", "Camping", HobbyCategory.OUTDOOR_NATURE),
        Hobby("fishing", "Fishing", HobbyCategory.OUTDOOR_NATURE),
        Hobby("bird_watching", "Bird watching", HobbyCategory.OUTDOOR_NATURE),
        Hobby("nature_photography", "Nature photography", HobbyCategory.OUTDOOR_NATURE),
        // Technical & Intellectual
        Hobby("programming", "Programming/Coding", HobbyCategory.TECHNICAL_INTELLECTUAL),
        Hobby("chess", "Chess", HobbyCategory.TECHNICAL_INTELLECTUAL),
        Hobby("puzzle_solving", "Puzzle solving", HobbyCategory.TECHNICAL_INTELLECTUAL),
        Hobby("model_building", "Model building", HobbyCategory.TECHNICAL_INTELLECTUAL),
        Hobby("electronics", "Electronics/Tinkering", HobbyCategory.TECHNICAL_INTELLECTUAL),
    )

@Composable
fun SelectHobbiesOnboarding(
    selectedHobbies: Set<String> = emptySet(),
    onHobbyToggle: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Content(
        selectedHobbies = selectedHobbies,
        onHobbyToggle = onHobbyToggle,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Content(
    selectedHobbies: Set<String>,
    onHobbyToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = "What are your hobbies?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "Select your interests to help us personalize your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Selected hobbies count
        if (selectedHobbies.isNotEmpty()) {
            Text(
                text = "${selectedHobbies.size} hobby${if (selectedHobbies.size == 1) "" else "ies"} selected",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
        }

        // Hobbies List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val groupedHobbies = allHobbies.groupBy { it.category }

            items(groupedHobbies.entries.toList()) { (category, hobbies) ->
                HobbyCategorySection(
                    category = category,
                    hobbies = hobbies,
                    selectedHobbies = selectedHobbies,
                    onHobbyToggle = onHobbyToggle,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HobbyCategorySection(
    category: HobbyCategory,
    hobbies: List<Hobby>,
    selectedHobbies: Set<String>,
    onHobbyToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Category Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                }

                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Hobbies Grid
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                hobbies.forEach { hobby ->
                    HobbyChip(
                        hobby = hobby,
                        isSelected = hobby.id in selectedHobbies,
                        onToggle = { onHobbyToggle(hobby.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HobbyChip(
    hobby: Hobby,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        label = "hobbyChipBackground",
    )

    val textColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        label = "hobbyChipText",
    )

    ElevatedFilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = {
            Text(
                text = hobby.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
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
                elevation = if (isSelected) 4.dp else 2.dp,
                pressedElevation = 6.dp,
            ),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewSelectHobbiesOnboarding() {
    var selectedHobbies by remember { mutableStateOf(emptySet<String>()) }

    MaterialTheme {
        SelectHobbiesOnboarding(
            selectedHobbies = selectedHobbies,
            onHobbyToggle = { hobbyId ->
                selectedHobbies =
                    if (hobbyId in selectedHobbies) {
                        selectedHobbies - hobbyId
                    } else {
                        selectedHobbies + hobbyId
                    }
            },
        )
    }
}
