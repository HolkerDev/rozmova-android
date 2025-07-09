package eu.rozmova.app.modules.createchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.domain.DifficultyDto
import eu.rozmova.app.modules.shared.DifficultyLabel
import org.orbitmvi.orbit.compose.collectAsState

enum class ChatType(
    val displayName: String,
    val icon: ImageVector,
) {
    SPEAKING("Speaking", Icons.Default.Mic),
    TEXTING("Texting", Icons.Default.TextFields),
}

interface CreateChatNavigation {
    fun back()

    fun toChat(
        chatId: String,
        chatType: ChatType,
    )
}

@Composable
fun CreateChatScreen(
    scenarioId: String,
    navigation: CreateChatNavigation,
    modifier: Modifier = Modifier,
    viewModel: CreateChatVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.fetchScenario(scenarioId)
    }

    Content(
        navigation = navigation,
        onChatStart = {},
        state = state,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    navigation: CreateChatNavigation,
    onChatStart: () -> Unit,
    state: CreateChatState,
    modifier: Modifier = Modifier,
) {
    var selectedChatTypes by remember { mutableStateOf(ChatType.SPEAKING) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scenario Details") },
                navigationIcon = {
                    IconButton(onClick = navigation::back) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                ) {
                    Button(
                        onClick = { TODO("something") },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start ${selectedChatTypes.displayName} Chat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (state.scenario == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            state.scenario?.let { scenario ->
                // Scenario Details Card
                ScenarioDetailsCard(
                    title = scenario.title,
                    situation = scenario.situation,
                    difficulty = scenario.difficulty,
                )

                // Chat Type Selection
                ChatTypeSelection(
                    selectedType = selectedChatTypes,
                    onTypeSelected = { selectedChatTypes = it },
                )

                // Add bottom padding to ensure content doesn't get hidden behind bottom bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ScenarioDetailsCard(
    title: String,
    situation: String,
    difficulty: DifficultyDto,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Difficulty Badge
            DifficultyLabel(difficulty = difficulty)

            // Situation
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Situation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = situation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                )
            }
        }
    }
}

@Composable
private fun ChatTypeSelection(
    selectedType: ChatType,
    onTypeSelected: (ChatType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Choose Chat Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChatType.entries.forEach { chatType ->
                ChatTypeOption(
                    chatTypes = chatType,
                    selected = selectedType == chatType,
                    onSelected = { onTypeSelected(chatType) },
                )
            }
        }
    }
}

@Composable
private fun ChatTypeOption(
    chatTypes: ChatType,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onSelected,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ),
        border =
            if (selected) {
                CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                    brush =
                        SolidColor(
                            MaterialTheme.colorScheme.primary,
                        ),
                )
            } else {
                CardDefaults.outlinedCardBorder()
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
                colors =
                    RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                    ),
            )

            Icon(
                imageVector = chatTypes.icon,
                contentDescription = null,
                tint =
                    if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(24.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatTypes.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )

                Text(
                    text =
                        when (chatTypes) {
                            ChatType.SPEAKING -> "Practice verbal communication skills"
                            ChatType.TEXTING -> "Practice written communication skills"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
            }
        }
    }
}

// @Preview(showBackground = true)
// @Composable
// private fun ScenarioDetailsScreenPreview() {
//    MaterialTheme {
//        Content(
//        )
//    }
// }
//
// @Preview(showBackground = true)
// @Composable
// private fun ScenarioDetailsScreenDarkPreview() {
//    MaterialTheme(colorScheme = darkColorScheme()) {
//        CreateChatScreen(
//            scenario =
//                ScenarioData(
//                    title = "Job Interview Scenario",
//                    situation = "You are interviewing for a software developer position at a startup. The interviewer asks you to explain a complex technical concept to someone without a technical background. You need to demonstrate both your technical knowledge and communication skills.",
//                    difficulty = "Advanced",
//                ),
//        )
//    }
// }
