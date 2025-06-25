package eu.rozmova.app.components.bugreport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import eu.rozmova.app.R
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun BugReportDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BugReportViewModel = hiltViewModel(),
) {
    var bugTitle by remember { mutableStateOf("") }
    var bugDescription by remember { mutableStateOf("") }

    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { event ->
        when (event) {
            BugReportEvents.BugReportSent -> onDismiss()
            BugReportEvents.Error -> onDismiss() // TODO: Handle error somehow
        }
    }

    fun onSubmit(
        title: String,
        description: String,
    ) {
        viewModel.sendBugReport(
            title = title,
            description = description,
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
    ) {
        Surface(
            modifier = modifier.fillMaxWidth().wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Text(
                    text = stringResource(R.string.propose_idea_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                HorizontalDivider()

                // Title field
                TextField(
                    value = bugTitle,
                    onValueChange = { bugTitle = it },
                    label = { Text(stringResource(R.string.title)) },
                    placeholder = { Text(stringResource(R.string.title_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                TextField(
                    value = bugDescription,
                    onValueChange = { bugDescription = it },
                    label = { Text(stringResource(R.string.description)) },
                    placeholder = { Text(stringResource(R.string.description_description)) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.cancel))
                    }

                    Button(
                        onClick = {
                            onSubmit(bugTitle, bugDescription)
                        },
                        enabled =
                            bugTitle.isNotBlank() &&
                                bugDescription.isNotBlank() &&
                                !state.isSubmitting,
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(stringResource(R.string.send))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BugReportDialogPreview() {
    var show by remember { mutableStateOf(true) }

    if (show) {
        BugReportDialog(
            onDismiss = { show = false },
        )
    }
}
