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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BugReportDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bugTitle by remember { mutableStateOf("") }
    var bugDescription by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

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
                    text = "Report a Bug",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                HorizontalDivider()

                // Title field
                TextField(
                    value = bugTitle,
                    onValueChange = { bugTitle = it },
                    label = { Text("Title") },
                    placeholder = { Text("Short description of the issue") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                TextField(
                    value = bugDescription,
                    onValueChange = { bugDescription = it },
                    label = { Text("Description") },
                    placeholder = { Text("Please provide details about what happened...") },
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
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isSubmitting = true
                            onSubmit(bugTitle, bugDescription, contactInfo)
                            onDismiss()
                        },
                        enabled =
                            bugTitle.isNotBlank() &&
                                bugDescription.isNotBlank() &&
                                !isSubmitting,
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("Send Report")
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
            onSubmit = { title, description, contact ->
                // Mock submission handler
                println("Bug submitted: $title, $description, $contact")
            },
        )
    }
}
