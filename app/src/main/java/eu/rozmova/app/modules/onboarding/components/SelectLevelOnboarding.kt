package eu.rozmova.app.modules.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.R
import eu.rozmova.app.domain.Level

private data class LevelInfo(
    val level: Level,
    val nameResId: Int,
    val descriptionResId: Int,
    val isAvailable: Boolean = true,
)

@Composable
fun SelectLevelOnboarding(
    onLevelSelect: (level: Level) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val levels =
        listOf(
            LevelInfo(Level.A1, R.string.level_a1_name, R.string.level_a1_description, true),
            LevelInfo(Level.A2, R.string.level_a2_name, R.string.level_a2_description, false),
            LevelInfo(Level.B1, R.string.level_b1_name, R.string.level_b1_description, false),
            LevelInfo(Level.B2, R.string.level_b2_name, R.string.level_b2_description, false),
            LevelInfo(Level.C1, R.string.level_c1_name, R.string.level_c1_description, false),
            LevelInfo(Level.C2, R.string.level_c2_name, R.string.level_c2_description, false),
        )

    var selectedLevel by remember { mutableStateOf(Level.A1) }

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
            ) {
                // Header Section
                Column(
                    modifier =
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.onboarding_level_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.onboarding_level_description),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Level List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(levels) { levelInfo ->
                        LevelListItem(
                            levelInfo = levelInfo,
                            isSelected = selectedLevel == levelInfo.level,
                            onLevelSelect = {
                                if (levelInfo.isAvailable) {
                                    selectedLevel = levelInfo.level
                                    onLevelSelect(levelInfo.level)
                                }
                            },
                        )
                    }
                }
            }

            // Back button positioned at bottom left
            FloatingActionButton(
                onClick = onBack,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }

            // Next button positioned at bottom right
            FloatingActionButton(
                onClick = onNext,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Next",
                )
            }
        }
    }
}

@Composable
private fun LevelListItem(
    levelInfo: LevelInfo,
    isSelected: Boolean,
    onLevelSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAvailable = levelInfo.isAvailable

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .selectable(
                    selected = isSelected && isAvailable,
                    onClick = onLevelSelect,
                    role = Role.RadioButton,
                    enabled = isAvailable,
                ),
        color =
            when {
                isSelected && isAvailable -> MaterialTheme.colorScheme.primaryContainer
                !isAvailable -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            },
        tonalElevation = if (isSelected && isAvailable) 3.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Level info
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(levelInfo.nameResId),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color =
                            when {
                                isSelected && isAvailable -> MaterialTheme.colorScheme.onPrimaryContainer
                                !isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                    )

                    if (!isAvailable) {
                        Text(
                            text = stringResource(R.string.coming_soon),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = stringResource(levelInfo.descriptionResId),
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        when {
                            isSelected && isAvailable -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            !isAvailable -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }

            RadioButton(
                selected = isSelected && isAvailable,
                onClick = null,
                enabled = isAvailable,
            )
        }
    }
}

@Composable
@Preview
private fun SelectLevelOnboardingPreview() {
    SelectLevelOnboarding(
        onLevelSelect = {},
        onNext = {},
        onBack = {},
    )
}
