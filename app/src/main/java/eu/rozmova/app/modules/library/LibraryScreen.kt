package eu.rozmova.app.modules.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.rozmova.app.modules.library.components.ChecklistButton
import eu.rozmova.app.modules.library.components.CompletedScenariosButton
import eu.rozmova.app.modules.library.components.ExploreAllScenariosButton
import eu.rozmova.app.modules.library.components.GenerateScenarioButton
import eu.rozmova.app.modules.library.components.TeacherIntegrationButton

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
    Content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Library") },
            )
        },
        content = { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
            ) {
                ExploreAllScenariosButton(
                    onClick = { /* TODO: Navigate to scenarios */ },
                )
                Spacer(Modifier.padding(vertical = 8.dp))
                GenerateScenarioButton(onClick = { /* TODO: Navigate to scenario generation */ })
                Spacer(Modifier.padding(vertical = 8.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ChecklistButton(onClick = {
                        // TODO: Navigate to checklist
                    }, modifier = Modifier.weight(1f).fillMaxHeight())
                    CompletedScenariosButton(onClick = {
                        // TODO: Navigate to completed scenarios
                    }, modifier = Modifier.weight(1f).fillMaxHeight())
                }
                Spacer(Modifier.padding(vertical = 8.dp))
                TeacherIntegrationButton(onClick = {})
            }
        },
    )
}

@Preview
@Composable
private fun LibraryScreenPreview() {
    Content()
}
