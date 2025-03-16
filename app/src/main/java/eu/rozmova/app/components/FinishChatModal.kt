package eu.rozmova.app.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ShouldFinishChatDialog(
    showDialog: Boolean,
    onYesClick: () -> Unit,
    onNoClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Should finish chat?",
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onYesClick()
                        onDismiss()
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onNoClick()
                        onDismiss()
                    },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text("Continue")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun YesNoDialogPreview() {
    var showDialog by remember { mutableStateOf(true) }
    ShouldFinishChatDialog(
        showDialog = showDialog,
        onYesClick = {},
        onNoClick = {},
        onDismiss = { showDialog = false },
    )
}
