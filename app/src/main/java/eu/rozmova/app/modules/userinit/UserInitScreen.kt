package eu.rozmova.app.modules.userinit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.domain.Level
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun UserInitScreen(
    hobbies: Set<String>,
    job: String?,
    pronoun: String,
    level: Level,
    toLearn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserInitVM = hiltViewModel(),
) {
    val state by viewModel.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.initUser(hobbies, job, pronoun, level)
    }

    Content(
        isReady = state.isReady,
        onStartLearning = {
            toLearn()
        },
        modifier = modifier,
    )
}

@Composable
private fun Content(
    isReady: Boolean,
    onStartLearning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isReady) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Preparing your learning profile...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Profile ready!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You're all set to begin learning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Button(
                onClick = onStartLearning,
                enabled = isReady,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Start Learning")
            }
        }
    }
}

@Preview
@Composable
private fun UserInitScreenPreview() {
    MaterialTheme {
        Content(isReady = false, onStartLearning = {})
    }
}
