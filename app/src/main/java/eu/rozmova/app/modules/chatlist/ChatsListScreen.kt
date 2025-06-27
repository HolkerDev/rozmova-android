package eu.rozmova.app.modules.chatlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import eu.rozmova.app.domain.ScenarioType
import eu.rozmova.app.domain.toScenarioType
import eu.rozmova.app.modules.chatlist.components.ChatItem
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsListScreen(
    onChatSelect: (String, ScenarioType) -> Unit,
    onChatCreateClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatsListViewModel = hiltViewModel(),
) {
    val state = viewModel.collectAsState().value

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chats_screen_title)) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        PaddingValues(
                            top = paddingValues.calculateTopPadding(),
                            start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                            end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                            bottom = 4.dp,
                        ),
                    ),
        ) {
            Card(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                shape = MaterialTheme.shapes.medium,
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    state.chats.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.padding(vertical = 8.dp),
                        ) {
                            items(state.chats) { chat ->
                                ChatItem(chat, onChatClick = {
                                    onChatSelect(
                                        chat.id,
                                        chat.scenario.scenarioType.toScenarioType(),
                                    )
                                }, onChatDelete = {
                                    viewModel.deleteChat(chat.id)
                                })
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.chats_screen_no_chats),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp),
                        )
                    }
                }
            }

            Button(
                onClick = onChatCreateClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.generate_scenario),
                )
                Text(
                    text = stringResource(R.string.generate_scenario),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
