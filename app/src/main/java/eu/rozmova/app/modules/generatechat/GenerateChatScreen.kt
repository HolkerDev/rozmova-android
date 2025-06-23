package eu.rozmova.app.modules.generatechat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.ScenarioTypeDto

typealias ChatId = String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateChatScreen(
    onChatReady: (ChatId, ScenarioTypeDto) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GenerateChatVM = hiltViewModel(),
) {
    var description by remember { mutableStateOf("") }
    var selectedScenarioType by remember { mutableStateOf(ScenarioType.MESSAGES) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Generate Chat",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Description Field
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                        placeholder = {
                            Text("Describe the chat scenario you want to generate...")
                        },
                        maxLines = 6,
                        enabled = !isLoading,
                    )
                }
            }

            // Scenario Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Scenario Type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        ScenarioType.values().forEach { scenarioType ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedScenarioType == scenarioType,
                                            onClick = { selectedScenarioType = scenarioType },
                                            role = Role.RadioButton,
                                            enabled = !isLoading,
                                        ).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedScenarioType == scenarioType,
                                    onClick = null,
                                    enabled = !isLoading,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text =
                                            when (scenarioType) {
                                                ScenarioType.MESSAGES -> "Messages"
                                                ScenarioType.CONVERSATION -> "Conversation"
                                            },
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text =
                                            when (scenarioType) {
                                                ScenarioType.MESSAGES -> "Short message exchanges"
                                                ScenarioType.CONVERSATION -> "Longer conversational practice"
                                            },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Difficulty Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Difficulty.values().forEach { difficulty ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedDifficulty == difficulty,
                                            onClick = { selectedDifficulty = difficulty },
                                            role = Role.RadioButton,
                                            enabled = !isLoading,
                                        ).padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = selectedDifficulty == difficulty,
                                    onClick = null,
                                    enabled = !isLoading,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = difficulty.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = difficulty.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Generate Button
            Button(
                onClick = {
                    isLoading = true
                    // TODO: Add generation logic here
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                enabled = !isLoading && description.isNotBlank(),
            ) {
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text("Generating...")
                    }
                } else {
                    Text(
                        text = "Generate Chat",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

enum class Difficulty(
    val displayName: String,
    val description: String,
) {
    EASY("Easy", "Basic vocabulary and simple sentences"),
    MEDIUM("Medium", "Intermediate vocabulary and complex structures"),
    HARD("Hard", "Advanced vocabulary and native-like expressions"),
}
