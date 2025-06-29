package eu.rozmova.app.modules.scenarioinfo

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R

@Composable
fun ScenarioInfoScreen(
    modifier: Modifier = Modifier,
    viewModel: ScenarioInfoVM = hiltViewModel(),
) {
    Content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chats_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(innerPadding),
        ) {
        }
    }
}

@Composable
@Preview
private fun ScenarioScreenPreview() {
    Content()
}
